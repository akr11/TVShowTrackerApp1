package com.tvshowtracker.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tvshowtracker.shared.domain.model.ShowSummary
import com.tvshowtracker.shared.presentation.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SearchRoute() {
    val koin = LocalKoinGraph.current
    val viewModel = remember { koin.get<SearchViewModel>() }
    val navigator = LocalAppNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var field by remember { mutableStateOf("") }

    LaunchedEffect(field) {
        viewModel.onQueryChange(field)
        delay(450)
        if (field.trim().length >= 2) {
            viewModel.search(resetPage = true)
        }
    }

    LaunchedEffect(listState, state.results.size, state.canLoadMore, state.isLoading) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            last to total
        }
            .distinctUntilChanged()
            .collect { (lastVis, total) ->
                if (total > 0 && lastVis >= total - 3 && state.canLoadMore && !state.isLoading) {
                    viewModel.loadMore()
                }
            }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Search TV shows", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = field,
            onValueChange = { field = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Title") },
            placeholder = { Text("Type at least 2 characters") }
        )
        Button(
            onClick = { viewModel.search(resetPage = true) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Search now")
        }

        when {
            state.isLoading && state.results.isEmpty() -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null && state.results.isEmpty() -> {
                Column(
                    Modifier.padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(state.error ?: "Error", color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.retry() }) { Text("Retry") }
                }
            }

            state.results.isEmpty() && field.trim().length >= 2 && !state.isLoading -> {
                Text(
                    "No results",
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            else -> {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.results, key = { it.id }) { show ->
                        SearchResultCard(item = show, onClick = { navigator.openShowDetails(show.id) })
                    }
                    if (state.isLoading && state.results.isNotEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    item: ShowSummary,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .weight(0.35f),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.weight(0.65f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                item.summaryPlain?.let {
                    Text(
                        it,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
