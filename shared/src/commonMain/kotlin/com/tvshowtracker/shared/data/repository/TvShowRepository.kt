package com.tvshowtracker.shared.data.repository

import com.tvshowtracker.shared.data.db.TvShowDao
import com.tvshowtracker.shared.data.db.entity.WatchedEpisodeEntity
import com.tvshowtracker.shared.data.db.mapper.toCachedEntity
import com.tvshowtracker.shared.data.db.mapper.toEpisode
import com.tvshowtracker.shared.data.db.mapper.toFavoriteEntity
import com.tvshowtracker.shared.data.db.mapper.toShowDetail
import com.tvshowtracker.shared.data.network.TvShowsRemoteApi
import com.tvshowtracker.shared.data.remote.mapper.toDetail
import com.tvshowtracker.shared.data.remote.mapper.toDomain
import com.tvshowtracker.shared.data.remote.mapper.toSummary
import com.tvshowtracker.shared.data.db.mapper.toShowSummary
import com.tvshowtracker.shared.domain.episodesBySeason
import com.tvshowtracker.shared.domain.model.Episode
import com.tvshowtracker.shared.domain.model.SeasonBlock
import com.tvshowtracker.shared.domain.model.ShowDetail
import com.tvshowtracker.shared.domain.model.ShowSummary
import com.tvshowtracker.shared.domain.nextUnwatchedEpisode
import com.tvshowtracker.shared.domain.progressPercent
import com.tvshowtracker.shared.domain.sortedEpisodes
import com.tvshowtracker.shared.domain.watchedProgress
import com.tvshowtracker.shared.platform.currentEpochMillis
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class FavoriteListItem(
    val show: ShowSummary,
    val progressPercent: Int,
    val nextEpisode: Episode?
)

class TvShowRepository(
    private val remoteApi: TvShowsRemoteApi,
    private val dao: TvShowDao,
    private val session: SessionCache
) {
    private val pageSize = 20

    suspend fun searchShows(query: String, page: Int): Result<List<ShowSummary>> {
        val q = query.trim()
        if (q.isEmpty()) return Result.success(emptyList())
        return runCatching {
            remoteApi.searchShows(q).map { it.toSummary() }.let { all ->
                val from = page * pageSize
                if (from >= all.size) emptyList()
                else all.subList(from, kotlin.math.min(from + pageSize, all.size))
            }
        }.onFailure { Napier.e(it) { "searchShows failed" } }
    }

    suspend fun getShowDetail(showId: Long, refreshRemote: Boolean = true): Result<ShowDetail> {
        val favorite = dao.getFavorite(showId)
        return runCatching {
            if (refreshRemote) {
                try {
                    val dto = remoteApi.getShow(showId)
                    val detail = dto.toDetail()
                    session.putShow(detail)
                    if (favorite != null) {
                        dao.upsertFavorite(detail.toFavoriteEntity(favorite.addedAtEpochMs))
                    }
                    return@runCatching detail
                } catch (e: Throwable) {
                    Napier.w(e) { "remote show failed, try cache" }
                    if (favorite != null) return@runCatching favorite.toShowDetail()
                    session.getShow(showId)?.let { return@runCatching it }
                    throw e
                }
            } else {
                favorite?.toShowDetail()
                    ?: session.getShow(showId)
                    ?: error("Show not available offline")
            }
        }
    }

    suspend fun getEpisodes(showId: Long, refreshRemote: Boolean = true): Result<List<Episode>> {
        val favorite = dao.isFavorite(showId)
        return runCatching {
            if (refreshRemote) {
                try {
                    val remote = remoteApi.getEpisodes(showId).map { it.toDomain(showId) }.sortedEpisodes()
                    session.putEpisodes(showId, remote)
                    if (favorite) {
                        dao.replaceEpisodesForShow(showId, remote.map { it.toCachedEntity() })
                    }
                    return@runCatching remote
                } catch (e: Throwable) {
                    Napier.w(e) { "remote episodes failed" }
                    if (favorite) {
                        val local = dao.getEpisodesForShow(showId).map { it.toEpisode() }.sortedEpisodes()
                        if (local.isNotEmpty()) return@runCatching local
                    }
                    session.getEpisodes(showId)?.let { return@runCatching it }
                    throw e
                }
            } else {
                if (favorite) {
                    val local = dao.getEpisodesForShow(showId).map { it.toEpisode() }.sortedEpisodes()
                    if (local.isNotEmpty()) return@runCatching local
                }
                session.getEpisodes(showId)
                    ?: error("Episodes not available offline")
            }
        }
    }

    fun observeLibrary(): Flow<List<FavoriteListItem>> {
        return combine(
            dao.observeFavorites(),
            dao.observeCachedEpisodes(),
            dao.observeWatchedRows()
        ) { favs, eps, watchedRows ->
            val epsByShow = eps.groupBy { it.showId }.mapValues { (_, v) -> v.map { it.toEpisode() } }
            val watchedByShow = watchedRows.groupBy { it.showId }
                .mapValues { (_, rows) -> rows.map { it.episodeId }.toSet() }
            favs.map { fav ->
                val list = epsByShow[fav.showId].orEmpty()
                val watched = watchedByShow[fav.showId].orEmpty()
                val (w, t) = watchedProgress(list, watched)
                FavoriteListItem(
                    show = fav.toShowSummary(),
                    progressPercent = progressPercent(w, t),
                    nextEpisode = nextUnwatchedEpisode(list, watched)
                )
            }
        }
    }

    suspend fun isFavorite(showId: Long): Boolean = dao.isFavorite(showId)

    suspend fun addFavorite(showId: Long): Result<Unit> =
        runCatching {
            val detailDto = remoteApi.getShow(showId)
            val detail = detailDto.toDetail()
            val eps = remoteApi.getEpisodes(showId).map { it.toDomain(showId) }
            val now = currentEpochMillis()
            dao.upsertFavorite(detail.toFavoriteEntity(now))
            dao.replaceEpisodesForShow(showId, eps.map { it.toCachedEntity() })
            session.putShow(detail)
            session.putEpisodes(showId, eps.sortedEpisodes())
        }.onFailure { Napier.e(it) { "addFavorite" } }

    suspend fun removeFavorite(showId: Long) {
        dao.deleteFavorite(showId)
        session.clearShow(showId)
    }

    suspend fun setEpisodeWatched(showId: Long, episodeId: Long, watched: Boolean) {
        if (watched) dao.insertWatched(WatchedEpisodeEntity(showId, episodeId))
        else dao.deleteWatched(showId, episodeId)
    }

    suspend fun setSeasonWatched(showId: Long, season: Int, watched: Boolean) {
        val eps = dao.getEpisodesForShow(showId).map { it.toEpisode() }.filter { it.season == season }
        if (watched) {
            eps.forEach { dao.insertWatched(WatchedEpisodeEntity(showId, it.id)) }
        } else {
            eps.forEach { dao.deleteWatched(showId, it.id) }
        }
    }

    suspend fun watchedIds(showId: Long): Set<Long> =
        dao.watchedEpisodeIds(showId).toSet()

    fun seasons(episodes: List<Episode>): List<SeasonBlock> = episodesBySeason(episodes)
}
