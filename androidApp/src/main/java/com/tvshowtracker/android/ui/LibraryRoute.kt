package com.tvshowtracker.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tvshowtracker.shared.data.repository.FavoriteListItem
import com.tvshowtracker.shared.presentation.LibraryViewModel

@Composable
fun LibraryRoute() {
    val koin = LocalKoinGraph.current
    val viewModel = remember { koin.get<LibraryViewModel>() }
    val navigator = LocalAppNavigator.current
    val favorites by viewModel.items.collectAsStateWithLifecycle()
    when {
        favorites.isEmpty() -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No saved shows yet.\nSearch and add favorites for offline access.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        else -> {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites, key = { it.show.id }) { row ->
                    LibraryRow(
                        row = row,
                        onOpen = { navigator.openShowDetails(row.show.id) },
                        onRemove = { viewModel.remove(row.show.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(
    row: FavoriteListItem,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    Card(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = row.show.posterUrl,
                contentDescription = row.show.name,
                modifier = Modifier
                    .weight(0.3f),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.weight(0.6f)) {
                Text(row.show.name, style = MaterialTheme.typography.titleMedium)
                Text("${row.progressPercent}% watched", style = MaterialTheme.typography.bodySmall)
                row.nextEpisode?.let {
                    Text(
                        "Next: S${it.season}E${it.number}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
}
