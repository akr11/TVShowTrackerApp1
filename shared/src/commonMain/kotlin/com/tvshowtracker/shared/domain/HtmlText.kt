package com.tvshowtracker.shared.domain

/**
 * Removes basic HTML tags for API summaries (TVmaze returns HTML snippets).
 */
fun stripHtml(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    var s = raw
    val tagRegex = Regex("<[^>]+>")
    s = tagRegex.replace(s, "")
    s = s.replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
    return s.trim().ifBlank { null }
}
