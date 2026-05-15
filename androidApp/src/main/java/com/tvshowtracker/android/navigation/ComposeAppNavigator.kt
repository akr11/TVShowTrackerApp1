package com.tvshowtracker.android.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptionsBuilder
import com.tvshowtracker.shared.navigation.AppNavigator

internal object NavRoutes {
    const val Search = "search"
    const val Library = "library"
    const val Detail = "detail/{showId}"
    const val Episodes = "episodes/{showId}"

    fun detail(showId: Long) = "detail/$showId"
    fun episodes(showId: Long) = "episodes/$showId"
}

/**
 * Compose [NavHost] implementation used with [CompositionLocal][androidx.compose.runtime.CompositionLocal].
 */
class ComposeAppNavigator(
    private val navController: NavController
) : AppNavigator {

    override fun openSearchTab() {
        navController.navigate(NavRoutes.Search) {
            popUpToTabRoot()
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun openLibraryTab() {
        navController.navigate(NavRoutes.Library) {
            popUpToTabRoot()
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun openShowDetails(showId: Long) {
        navController.navigate(NavRoutes.detail(showId))
    }

    override fun openEpisodes(showId: Long) {
        navController.navigate(NavRoutes.episodes(showId))
    }

    override fun back() {
        if (!navController.popBackStack()) {
            navController.navigate(NavRoutes.Search) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    }

    private fun NavOptionsBuilder.popUpToTabRoot() {
        navController.graph.findStartDestination().id.also { startId ->
            popUpTo(startId) { saveState = true }
        }
    }
}
