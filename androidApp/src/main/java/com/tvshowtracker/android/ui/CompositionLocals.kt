package com.tvshowtracker.android.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.tvshowtracker.shared.navigation.AppNavigator
import org.koin.core.Koin

/**
 * Koin dependency graph reachable from Compose without traversing Globals explicitly.
 * Mirrors the project rule: CompositionLocal carries Koin-managed graph into the subtree.
 */
val LocalKoinGraph = staticCompositionLocalOf<Koin> {
    error("Koin is not attached — wrap with CompositionLocalProvider(LocalKoinGraph provides koin)")
}

val LocalAppNavigator = staticCompositionLocalOf<AppNavigator> {
    error("AppNavigator is not attached — provide ComposeAppNavigator at the navigation root.")
}
