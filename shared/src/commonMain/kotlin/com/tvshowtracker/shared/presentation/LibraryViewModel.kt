package com.tvshowtracker.shared.presentation

import com.tvshowtracker.shared.data.repository.FavoriteListItem
import com.tvshowtracker.shared.data.repository.TvShowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val repository: TvShowRepository
) : BaseViewModel() {

    private val _items = MutableStateFlow<List<FavoriteListItem>>(emptyList())
    val items: StateFlow<List<FavoriteListItem>> = _items.asStateFlow()

    init {
        scope.launch {
            repository.observeLibrary().collectLatest { _items.value = it }
        }
    }

    fun remove(showId: Long) {
        scope.launch { repository.removeFavorite(showId) }
    }
}
