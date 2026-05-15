package com.tvshowtracker.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tvshowtracker.shared.domain.model.Episode
import com.tvshowtracker.shared.presentation.EpisodesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodesRoute(
    showId: Long
) {
    val koin = LocalKoinGraph.current
    val viewModel = remember(showId) { koin.get<EpisodesViewModel>() }
    val navigator = LocalAppNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(showId) {
        viewModel.load(showId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Episodes") },
                navigationIcon = {
                    IconButton(onClick = { navigator.back() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->
        when {
            state.isLoading -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(pad),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.load(showId) }) { Text("Retry") }
                }
            }

            else -> {
                LazyColumn(
                    Modifier
                        .padding(pad)
                        .fillMaxSize()
                ) {
                    items(state.seasons, key = { it.season }) { block ->
                        SeasonSection(
                            showId = showId,
                            block = block,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeasonSection(
    showId: Long,
    block: com.tvshowtracker.shared.domain.model.SeasonBlock,
    viewModel: EpisodesViewModel
) {
    var expanded by remember { mutableStateOf(true) }
    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Season ${block.season}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = false,
                onClick = {
                    val allWatched = block.episodes.all { viewModel.isWatched(it) }
                    viewModel.setSeasonWatched(showId, block.season, watched = !allWatched)
                },
                label = { Text(if (block.episodes.all { viewModel.isWatched(it) }) "Unwatch" else "Watch all") }
            )
        }
        if (expanded) {
            block.episodes.forEach { ep ->
                EpisodeRow(showId, ep, viewModel)
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    showId: Long,
    episode: Episode,
    viewModel: EpisodesViewModel
) {
    val watched = viewModel.isWatched(episode)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = watched,
            onCheckedChange = { viewModel.toggleWatched(showId, episode.id, it) }
        )
        AsyncImage(
            model = episode.mediumImageUrl,
            contentDescription = null,
            modifier = Modifier
                .weight(0.2f),
            contentScale = ContentScale.Crop
        )
        Column(Modifier.weight(0.8f)) {
            Text(
                "S${episode.season}E${episode.number} · ${episode.name ?: ""}",
                style = MaterialTheme.typography.bodyMedium
            )
            episode.airdate?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
