package com.tvshowtracker.shared.domain

import com.tvshowtracker.shared.domain.model.Episode

/**
 * Order: season asc, episode number asc. Treat missing number as 0, specials (often S0) first within season bucket.
 */
fun compareEpisodes(a: Episode, b: Episode): Int {
    val s = a.season.compareTo(b.season)
    if (s != 0) return s
    return a.number.compareTo(b.number)
}

fun Iterable<Episode>.sortedEpisodes(): List<Episode> = sortedWith(::compareEpisodes)
