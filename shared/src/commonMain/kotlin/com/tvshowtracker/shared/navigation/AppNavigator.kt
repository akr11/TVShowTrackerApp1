package com.tvshowtracker.shared.navigation

/**
 * Platform-agnostic navigation contract. Native layers (Compose / SwiftUI)
 * supply an implementation while shared presentation can depend on this interface where needed.
 */
interface AppNavigator {
    /** Main catalog search surface */
    fun openSearchTab()

    /** Saved favorites / offline library */
    fun openLibraryTab()

    fun openShowDetails(showId: Long)

    fun openEpisodes(showId: Long)

    fun back()
}
