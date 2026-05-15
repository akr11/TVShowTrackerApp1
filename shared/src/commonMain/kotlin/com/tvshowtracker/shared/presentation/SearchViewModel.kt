package com.tvshowtracker.shared.presentation

import com.tvshowtracker.shared.data.repository.TvShowRepository
import com.tvshowtracker.shared.domain.model.ShowSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<ShowSummary> = emptyList(),
    val page: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val canLoadMore: Boolean = false
)

class SearchViewModel(
    private val repository: TvShowRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    fun onQueryChange(q: String) {
        _state.update { it.copy(query = q, error = null) }
    }

    fun search(resetPage: Boolean = true) {
        scope.launch {
            val st = _state.value
            val nextPage = if (resetPage) 0 else st.page
            _state.update { it.copy(isLoading = true, error = null) }
            val result = repository.searchShows(st.query, nextPage)
            result.fold(
                onSuccess = { pageItems ->
                    _state.update {
                        val merged = if (resetPage) pageItems else it.results + pageItems
                        it.copy(
                            results = merged,
                            page = if (resetPage) 1 else nextPage + 1,
                            isLoading = false,
                            canLoadMore = pageItems.size >= PAGE_SIZE,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message ?: "Network error")
                    }
                }
            )
        }
    }

    fun loadMore() {
        if (_state.value.isLoading || !_state.value.canLoadMore) return
        search(resetPage = false)
    }

    fun retry() = search(resetPage = true)

    companion object {
        private const val PAGE_SIZE = 20
    }
}
