package com.tvshowtracker.shared.presentation

import com.tvshowtracker.shared.data.repository.TvShowRepository
import com.tvshowtracker.shared.domain.model.Episode
import com.tvshowtracker.shared.domain.model.SeasonBlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EpisodesUiState(
    val isLoading: Boolean = true,
    val seasons: List<SeasonBlock> = emptyList(),
    val watchedIds: Set<Long> = emptySet(),
    val error: String? = null
)

class EpisodesViewModel(
    private val repository: TvShowRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow(EpisodesUiState())
    val state: StateFlow<EpisodesUiState> = _state.asStateFlow()

    fun load(showId: Long) {
        scope.launch {
            _state.update { EpisodesUiState(isLoading = true) }
            val eps = repository.getEpisodes(showId)
            eps.fold(
                onSuccess = { list ->
                    val watched = repository.watchedIds(showId)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            seasons = repository.seasons(list),
                            watchedIds = watched,
                            error = null
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message ?: "Failed to load episodes")
                    }
                }
            )
        }
    }

    fun toggleWatched(showId: Long, episodeId: Long, watched: Boolean) {
        scope.launch {
            repository.setEpisodeWatched(showId, episodeId, watched)
            _state.update { it.copy(watchedIds = repository.watchedIds(showId)) }
        }
    }

    fun setSeasonWatched(showId: Long, season: Int, watched: Boolean) {
        scope.launch {
            repository.setSeasonWatched(showId, season, watched)
            _state.update { it.copy(watchedIds = repository.watchedIds(showId)) }
        }
    }

    fun isWatched(episode: Episode): Boolean = episode.id in _state.value.watchedIds
}
