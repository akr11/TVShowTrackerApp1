package com.tvshowtracker.shared.data.db.mapper

import com.tvshowtracker.shared.data.db.entity.CachedEpisodeEntity
import com.tvshowtracker.shared.data.db.entity.FavoriteShowEntity
import com.tvshowtracker.shared.domain.model.Episode
import com.tvshowtracker.shared.domain.model.ShowDetail
import com.tvshowtracker.shared.domain.model.ShowSummary

fun FavoriteShowEntity.toShowSummary(): ShowSummary =
    ShowSummary(
        id = showId,
        name = name,
        posterUrl = posterUrl,
        summaryPlain = summaryPlain,
        genres = genresCsv.split(',').map { it.trim() }.filter { it.isNotEmpty() },
        ratingAverage = ratingAverage,
        premiered = premiered,
        status = status
    )

fun FavoriteShowEntity.toShowDetail(): ShowDetail =
    ShowDetail(
        id = showId,
        name = name,
        posterUrl = posterUrl,
        summaryPlain = summaryPlain,
        genres = genresCsv.split(',').map { it.trim() }.filter { it.isNotEmpty() },
        ratingAverage = ratingAverage,
        runtimeMinutes = runtimeMinutes,
        language = language,
        status = status,
        premiered = premiered,
        networkName = networkName,
        averageRuntime = runtimeMinutes
    )

fun CachedEpisodeEntity.toEpisode(): Episode =
    Episode(
        id = episodeId,
        showId = showId,
        season = season,
        number = number,
        name = name,
        airdate = airdate,
        runtimeMinutes = runtimeMinutes,
        mediumImageUrl = mediumImageUrl,
        summaryPlain = summaryPlain
    )

fun ShowDetail.toFavoriteEntity(addedAtEpochMs: Long): FavoriteShowEntity =
    FavoriteShowEntity(
        showId = id,
        name = name,
        posterUrl = posterUrl,
        summaryPlain = summaryPlain,
        genresCsv = genres.joinToString(","),
        ratingAverage = ratingAverage,
        runtimeMinutes = runtimeMinutes ?: averageRuntime,
        language = language,
        status = status,
        premiered = premiered,
        networkName = networkName,
        addedAtEpochMs = addedAtEpochMs
    )

fun Episode.toCachedEntity(): CachedEpisodeEntity =
    CachedEpisodeEntity(
        episodeId = id,
        showId = showId,
        season = season,
        number = number,
        name = name,
        airdate = airdate,
        runtimeMinutes = runtimeMinutes,
        mediumImageUrl = mediumImageUrl,
        summaryPlain = summaryPlain
    )
