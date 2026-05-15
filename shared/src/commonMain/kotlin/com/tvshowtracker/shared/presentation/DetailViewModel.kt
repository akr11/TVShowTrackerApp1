package com.tvshowtracker.shared.presentation

import com.tvshowtracker.shared.data.repository.TvShowRepository
import com.tvshowtracker.shared.domain.model.ShowDetail
import com.tvshowtracker.shared.domain.nextUnwatchedEpisode
import com.tvshowtracker.shared.domain.watchedProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val show: ShowDetail? = null,
    val isFavorite: Boolean = false,
    val progressLine: String? = null,
    val nextEpisodeLine: String? = null,
    val error: String? = null
)

class DetailViewModel(
    private val repository: TvShowRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    fun load(showId: Long) {
        scope.launch {
            _state.update { DetailUiState(isLoading = true) }
            val fav = repository.isFavorite(showId)
            val detail = repository.getShowDetail(showId)
            val eps = repository.getEpisodes(showId)
            if (detail.isFailure) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = detail.exceptionOrNull()?.message ?: "Unable to load show"
                    )
                }
                return@launch
            }
            val d = detail.getOrNull()!!
            val list = eps.getOrNull().orEmpty()
            val watched = repository.watchedIds(showId)
            val (w, t) = watchedProgress(list, watched)
            val next = nextUnwatchedEpisode(list, watched)
            _state.update {
                it.copy(
                    isLoading = false,
                    show = d,
                    isFavorite = fav,
                    progressLine = if (t > 0) "Watched $w of $t episodes" else null,
                    nextEpisodeLine = next?.let { ep ->
                        "Next: S${ep.season}E${ep.number} · ${ep.name.orEmpty()}".trim()
                    },
                    error = eps.exceptionOrNull()?.takeIf { list.isEmpty() }?.message
                )
            }
        }
    }

    fun toggleFavorite(showId: Long) {
        scope.launch {
            val st = _state.value
            if (st.isFavorite) {
                repository.removeFavorite(showId)
            } else {
                repository.addFavorite(showId).onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
            }
            load(showId)
        }
    }
}
