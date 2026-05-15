package com.tvshowtracker.shared.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.tvshowtracker.shared.data.db.entity.CachedEpisodeEntity
import com.tvshowtracker.shared.data.db.entity.FavoriteShowEntity
import com.tvshowtracker.shared.data.db.entity.WatchedEpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TvShowDao {

    @Query("SELECT * FROM favorite_shows ORDER BY addedAtEpochMs DESC")
    fun observeFavorites(): Flow<List<FavoriteShowEntity>>

    @Query("SELECT * FROM favorite_shows WHERE showId = :showId")
    suspend fun getFavorite(showId: Long): FavoriteShowEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_shows WHERE showId = :showId)")
    suspend fun isFavorite(showId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorite(item: FavoriteShowEntity)

    @Query("DELETE FROM favorite_shows WHERE showId = :showId")
    suspend fun deleteFavorite(showId: Long)

    @Query("SELECT * FROM cached_episodes WHERE showId = :showId ORDER BY season ASC, number ASC")
    suspend fun getEpisodesForShow(showId: Long): List<CachedEpisodeEntity>

    @Query("SELECT * FROM cached_episodes WHERE showId = :showId")
    fun observeEpisodesForShow(showId: Long): Flow<List<CachedEpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEpisodes(items: List<CachedEpisodeEntity>)

    @Query("DELETE FROM cached_episodes WHERE showId = :showId")
    suspend fun deleteEpisodesForShow(showId: Long)

    @Query("SELECT episodeId FROM watched_episodes WHERE showId = :showId")
    suspend fun watchedEpisodeIds(showId: Long): List<Long>

    @Query("SELECT episodeId FROM watched_episodes WHERE showId = :showId")
    fun observeWatchedEpisodeIds(showId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatched(row: WatchedEpisodeEntity)

    @Query("DELETE FROM watched_episodes WHERE showId = :showId AND episodeId = :episodeId")
    suspend fun deleteWatched(showId: Long, episodeId: Long)

    @Query("DELETE FROM watched_episodes WHERE showId = :showId")
    suspend fun clearWatchedForShow(showId: Long)

    @Query("SELECT * FROM cached_episodes")
    fun observeCachedEpisodes(): Flow<List<CachedEpisodeEntity>>

    @Query("SELECT * FROM watched_episodes")
    fun observeWatchedRows(): Flow<List<WatchedEpisodeEntity>>

    @Transaction
    suspend fun replaceEpisodesForShow(showId: Long, episodes: List<CachedEpisodeEntity>) {
        deleteEpisodesForShow(showId)
        if (episodes.isNotEmpty()) upsertEpisodes(episodes)
    }
}
