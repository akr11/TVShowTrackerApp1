package com.tvshowtracker.shared.data.network

import com.tvshowtracker.shared.data.remote.dto.EpisodeDto
import com.tvshowtracker.shared.data.remote.dto.SearchResultDto
import com.tvshowtracker.shared.data.remote.dto.ShowEmbeddedDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val API = "https://api.tvmaze.com"

class TvmazeClient(
    private val httpClient: HttpClient
) : TvShowsRemoteApi {

    override suspend fun searchShows(query: String): List<SearchResultDto> {
        return httpClient.get("$API/search/shows") {
            parameter("q", query)
        }.body()
    }

    override suspend fun getShow(showId: Long): ShowEmbeddedDto {
        return httpClient.get("$API/shows/$showId").body()
    }

    override suspend fun getEpisodes(showId: Long): List<EpisodeDto> {
        return httpClient.get("$API/shows/$showId/episodes").body()
    }
}

fun createTvmazeHttpClient(json: Json = Json { ignoreUnknownKeys = true; isLenient = true }): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 25_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 25_000
        }
        expectSuccess = false
    }
}
