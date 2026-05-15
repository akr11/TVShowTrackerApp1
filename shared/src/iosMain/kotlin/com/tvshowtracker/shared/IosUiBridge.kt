package com.tvshowtracker.shared

import com.tvshowtracker.shared.interop.FavoriteLibraryRows
import com.tvshowtracker.shared.interop.FlowWrapper
import com.tvshowtracker.shared.presentation.DetailUiState
import com.tvshowtracker.shared.presentation.DetailViewModel
import com.tvshowtracker.shared.presentation.EpisodesUiState
import com.tvshowtracker.shared.presentation.EpisodesViewModel
import com.tvshowtracker.shared.presentation.LibraryViewModel
import com.tvshowtracker.shared.presentation.SearchUiState
import com.tvshowtracker.shared.presentation.SearchViewModel
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Stable iOS-facing API: resolve Koin factories and expose [StateFlow] as [FlowWrapper] for SwiftUI.
 */
object IosUiBridge : KoinComponent {
    fun koinSearchViewModel(): SearchViewModel = get()

    fun koinDetailViewModel(): DetailViewModel = get()

    fun koinEpisodesViewModel(): EpisodesViewModel = get()

    fun koinLibraryViewModel(): LibraryViewModel = get()

    fun bindSearchState(vm: SearchViewModel, onEach: (SearchUiState) -> Unit): FlowWrapper<SearchUiState> =
        FlowWrapper(vm.state).also { it.subscribe(onEach) }

    fun bindDetailState(vm: DetailViewModel, onEach: (DetailUiState) -> Unit): FlowWrapper<DetailUiState> =
        FlowWrapper(vm.state).also { it.subscribe(onEach) }

    fun bindEpisodesState(vm: EpisodesViewModel, onEach: (EpisodesUiState) -> Unit): FlowWrapper<EpisodesUiState> =
        FlowWrapper(vm.state).also { it.subscribe(onEach) }

    fun bindLibraryRows(vm: LibraryViewModel, onEach: (FavoriteLibraryRows) -> Unit): FlowWrapper<FavoriteLibraryRows> =
        FlowWrapper(vm.items.map { FavoriteLibraryRows(it) }).also { it.subscribe(onEach) }
}
