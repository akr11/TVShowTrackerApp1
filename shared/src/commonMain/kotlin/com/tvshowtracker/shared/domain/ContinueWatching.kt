package com.tvshowtracker.shared.domain

import com.tvshowtracker.shared.domain.model.Episode

/**
 * Shared business rule: first unwatched episode in global watch order.
 * Handles specials (S0), gaps in numbering, and empty watched sets.
 */
fun nextUnwatchedEpisode(episodes: List<Episode>, watchedIds: Set<Long>): Episode? {
    val order = episodes.sortedEpisodes()
    if (order.isEmpty()) return null
    return order.firstOrNull { it.id !in watchedIds }
}

fun watchedProgress(episodes: List<Episode>, watchedIds: Set<Long>): Pair<Int, Int> {
    val total = episodes.size
    if (total == 0) return 0 to 0
    val watched = episodes.count { it.id in watchedIds }
    return watched to total
}

fun progressPercent(watched: Int, total: Int): Int {
    if (total <= 0) return 0
    return ((watched * 100.0) / total).toInt().coerceIn(0, 100)
}
