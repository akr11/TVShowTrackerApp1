package com.tvshowtracker.shared.data.repository

import com.tvshowtracker.shared.domain.model.Episode
import com.tvshowtracker.shared.domain.model.ShowDetail

/**
 * Short-lived in-memory cache to avoid repeat network calls in a session (non-favorited shows).
 */
class SessionCache {
    private val shows = mutableMapOf<Long, ShowDetail>()
    private val episodes = mutableMapOf<Long, List<Episode>>()

    fun putShow(detail: ShowDetail) {
        shows[detail.id] = detail
    }

    fun getShow(showId: Long): ShowDetail? = shows[showId]

    fun putEpisodes(showId: Long, list: List<Episode>) {
        episodes[showId] = list
    }

    fun getEpisodes(showId: Long): List<Episode>? = episodes[showId]

    fun clearShow(showId: Long) {
        shows.remove(showId)
        episodes.remove(showId)
    }
}
