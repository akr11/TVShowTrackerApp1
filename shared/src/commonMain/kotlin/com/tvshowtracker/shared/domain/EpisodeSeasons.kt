package com.tvshowtracker.shared.domain

import com.tvshowtracker.shared.domain.model.Episode
import com.tvshowtracker.shared.domain.model.SeasonBlock

fun episodesBySeason(episodes: List<Episode>): List<SeasonBlock> =
    episodes.groupBy { it.season }
        .entries
        .sortedBy { it.key }
        .map { SeasonBlock(season = it.key, episodes = it.value.sortedEpisodes()) }
