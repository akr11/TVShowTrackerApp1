package com.tvshowtracker.shared.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResultDto(
    val score: Double? = null,
    val show: ShowEmbeddedDto
)

@Serializable
data class ShowEmbeddedDto(
    val id: Long,
    val name: String,
    val genres: List<String> = emptyList(),
    val summary: String? = null,
    val premiered: String? = null,
    val image: ImageDto? = null,
    val rating: RatingDto? = null,
    val status: String? = null,
    val runtime: Int? = null,
    val averageRuntime: Int? = null,
    val language: String? = null,
    val network: ChannelDto? = null,
    @SerialName("webChannel") val webChannel: ChannelDto? = null
)

@Serializable
data class RatingDto(
    val average: Double? = null
)

@Serializable
data class ImageDto(
    val medium: String? = null,
    val original: String? = null
)

@Serializable
data class ChannelDto(
    val name: String? = null
)

@Serializable
data class EpisodeDto(
    val id: Long,
    val name: String? = null,
    val season: Int? = null,
    val number: Int? = null,
    val airdate: String? = null,
    val runtime: Int? = null,
    val summary: String? = null,
    val image: ImageDto? = null
)
