package com.tvshowtracker.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HtmlTextTest {

    @Test
    fun stripHtml_removesTags() {
        assertEquals("Hello", stripHtml("<p>Hello</p>"))
    }

    @Test
    fun stripHtml_nullYieldsNull() {
        assertNull(stripHtml(null))
    }
}
