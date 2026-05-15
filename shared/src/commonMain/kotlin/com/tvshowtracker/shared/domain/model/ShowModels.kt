package com.tvshowtracker.shared.domain.model

data class ShowSummary(
    val id: Long,
    val name: String,
    val posterUrl: String?,
    val summaryPlain: String?,
    val genres: List<String>,
    val ratingAverage: Double?,
    val premiered: String?,
    val status: String?
)

data class ShowDetail(
    val id: Long,
    val name: String,
    val posterUrl: String?,
    val summaryPlain: String?,
    val genres: List<String>,
    val ratingAverage: Double?,
    val runtimeMinutes: Int?,
    val language: String?,
    val status: String?,
    val premiered: String?,
    val networkName: String?,
    val averageRuntime: Int?
)

data class Episode(
    val id: Long,
    val showId: Long,
    val season: Int,
    val number: Int,
    val name: String?,
    val airdate: String?,
    val runtimeMinutes: Int?,
    val mediumImageUrl: String?,
    val summaryPlain: String?
)

data class SeasonBlock(
    val season: Int,
    val episodes: List<Episode>
)

data class ShowProgress(
    val watchedCount: Int,
    val totalCount: Int,
    val percent: Int
)
