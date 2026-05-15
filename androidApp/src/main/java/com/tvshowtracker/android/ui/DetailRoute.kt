package com.tvshowtracker.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tvshowtracker.shared.presentation.DetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailRoute(
    showId: Long
) {
    val koin = LocalKoinGraph.current
    val viewModel = remember(showId) { koin.get<DetailViewModel>() }
    val navigator = LocalAppNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(showId) {
        viewModel.load(showId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.show?.name ?: "Show") },
                navigationIcon = {
                    IconButton(onClick = { navigator.back() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.toggleFavorite(showId) }) {
                Text(if (state.isFavorite) "\u2665" else "\u2661")
            }
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

            state.error != null && state.show == null -> {
                Column(
                    Modifier
                        .padding(pad)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.load(showId) }) { Text("Retry") }
                }
            }

            state.show != null -> {
                val s = state.show!!
                Column(
                    Modifier
                        .padding(pad)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = s.posterUrl,
                        contentDescription = s.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(s.name, style = MaterialTheme.typography.headlineSmall)
                    s.genres.takeIf { it.isNotEmpty() }?.joinToString()?.let {
                        Text("Genres: $it")
                    }
                    s.ratingAverage?.let { Text("Rating: $it") }
                    (s.runtimeMinutes ?: s.averageRuntime)?.let { Text("Runtime: $it min (avg)") }
                    s.language?.let { Text("Language: $it") }
                    s.status?.let { Text("Status: $it") }
                    s.premiered?.let { Text("Premiered: $it") }
                    s.networkName?.let { Text("Network / channel: $it") }
                    s.summaryPlain?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                    state.progressLine?.let { Text(it) }
                    state.nextEpisodeLine?.let { Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { navigator.openEpisodes(showId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Episodes & seasons")
                    }
                }
            }
        }
    }
}
