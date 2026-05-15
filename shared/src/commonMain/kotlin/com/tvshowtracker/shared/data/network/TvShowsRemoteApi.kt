package com.tvshowtracker.shared.data.network

import com.tvshowtracker.shared.data.remote.dto.EpisodeDto
import com.tvshowtracker.shared.data.remote.dto.SearchResultDto
import com.tvshowtracker.shared.data.remote.dto.ShowEmbeddedDto

/** TVmaze-backed remote catalog — implemented by [TvmazeClient]; mocked in tests via Mokkery / fakes. */
interface TvShowsRemoteApi {
    suspend fun searchShows(query: String): List<SearchResultDto>
    suspend fun getShow(showId: Long): ShowEmbeddedDto
    suspend fun getEpisodes(showId: Long): List<EpisodeDto>
}
