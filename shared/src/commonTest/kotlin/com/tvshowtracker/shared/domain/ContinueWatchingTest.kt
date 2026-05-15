package com.tvshowtracker.shared.domain

import com.tvshowtracker.shared.domain.model.Episode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ContinueWatchingTest {

    @Test
    fun nextUnwatched_skipsWatchedInOrder() {
        val eps = listOf(
            Episode(1, 10, 1, 1, "a", null, null, null, null),
            Episode(2, 10, 1, 2, "b", null, null, null, null),
            Episode(3, 10, 1, 3, "c", null, null, null, null)
        )
        val watched = setOf<Long>(1)
        val next = nextUnwatchedEpisode(eps, watched)
        assertEquals(2, next?.id)
    }

    @Test
    fun nextUnwatched_returnsNullWhenAllWatched() {
        val eps = listOf(Episode(1, 10, 1, 1, "a", null, null, null, null))
        assertNull(nextUnwatchedEpisode(eps, setOf(1)))
    }

    @Test
    fun progressPercent_handlesZeroTotal() {
        assertEquals(0, progressPercent(0, 0))
    }

    @Test
    fun watchedProgress_countsOnlyKnownEpisodes() {
        val eps = listOf(
            Episode(1, 10, 1, 1, "a", null, null, null, null),
            Episode(2, 10, 1, 2, "b", null, null, null, null)
        )
        val (w, t) = watchedProgress(eps, setOf(1))
        assertEquals(1, w)
        assertEquals(2, t)
    }
}
