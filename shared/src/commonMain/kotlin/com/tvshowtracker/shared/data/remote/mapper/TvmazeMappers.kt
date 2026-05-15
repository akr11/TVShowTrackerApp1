package com.tvshowtracker.shared.data.remote.mapper

import com.tvshowtracker.shared.data.remote.dto.EpisodeDto
import com.tvshowtracker.shared.data.remote.dto.SearchResultDto
import com.tvshowtracker.shared.data.remote.dto.ShowEmbeddedDto
import com.tvshowtracker.shared.domain.model.Episode
import com.tvshowtracker.shared.domain.stripHtml
import com.tvshowtracker.shared.domain.model.ShowDetail
import com.tvshowtracker.shared.domain.model.ShowSummary

fun SearchResultDto.toSummary(): ShowSummary {
    val s = show
    return ShowSummary(
        id = s.id,
        name = s.name,
        posterUrl = s.image?.medium ?: s.image?.original,
        summaryPlain = stripHtml(s.summary),
        genres = s.genres,
        ratingAverage = s.rating?.average,
        premiered = s.premiered,
        status = s.status
    )
}

fun ShowEmbeddedDto.toDetail(): ShowDetail {
    val network = network?.name ?: webChannel?.name
    return ShowDetail(
        id = id,
        name = name,
        posterUrl = image?.medium ?: image?.original,
        summaryPlain = stripHtml(summary),
        genres = genres,
        ratingAverage = rating?.average,
        runtimeMinutes = runtime ?: averageRuntime,
        language = language,
        status = status,
        premiered = premiered,
        networkName = network,
        averageRuntime = averageRuntime ?: runtime
    )
}

fun EpisodeDto.toDomain(showId: Long): Episode {
    return Episode(
        id = id,
        showId = showId,
        season = season ?: 0,
        number = number ?: 0,
        name = name,
        airdate = airdate,
        runtimeMinutes = runtime,
        mediumImageUrl = image?.medium ?: image?.original,
        summaryPlain = stripHtml(summary)
    )
}

