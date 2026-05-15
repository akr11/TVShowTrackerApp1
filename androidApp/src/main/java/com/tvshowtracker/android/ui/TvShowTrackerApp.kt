package com.tvshowtracker.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tvshowtracker.android.navigation.ComposeAppNavigator
import com.tvshowtracker.android.navigation.NavRoutes
import org.koin.core.context.GlobalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowTrackerApp() {
    val navController = rememberNavController()
    val navigator = remember(navController) { ComposeAppNavigator(navController) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomVisible = when (currentDestination?.route) {
        NavRoutes.Search, NavRoutes.Library -> true
        else -> false
    }

    CompositionLocalProvider(
        LocalAppNavigator provides navigator,
        LocalKoinGraph provides GlobalContext.get()
    ) {
        Scaffold(
            bottomBar = {
                if (bottomVisible) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Search } == true,
                            onClick = { navigator.openSearchTab() },
                            icon = { Icon(Icons.Default.Search, contentDescription = null) },
                            label = { Text("Search") }
                        )
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == NavRoutes.Library } == true,
                            onClick = { navigator.openLibraryTab() },
                            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                            label = { Text("My Shows") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavRoutes.Search,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(NavRoutes.Search) { SearchRoute() }
                composable(NavRoutes.Library) { LibraryRoute() }
                composable(
                    route = NavRoutes.Detail,
                    arguments = listOf(navArgument("showId") { type = NavType.LongType })
                ) { entry ->
                    val showId = entry.arguments?.getLong("showId") ?: return@composable
                    DetailRoute(showId = showId)
                }
                composable(
                    route = NavRoutes.Episodes,
                    arguments = listOf(navArgument("showId") { type = NavType.LongType })
                ) { entry ->
                    val showId = entry.arguments?.getLong("showId") ?: return@composable
                    EpisodesRoute(showId = showId)
                }
            }
        }
    }
}
