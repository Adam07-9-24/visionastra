package com.tecsup.visionastra.mobile.ui.resources

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tecsup.visionastra.mobile.ui.resources.components.ResourceCard
import com.tecsup.visionastra.mobile.ui.resources.components.ResourceEmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceListScreen(
    state: ResourceListUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onTypeSelected: (String?) -> Unit,
    onUploadImageClick: () -> Unit,
    onCreateCopyClick: () -> Unit,
    onResourceClick: (Int, String) -> Unit,
    onSnackbarShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onSnackbarShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(state.campaignName ?: "Recursos") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) { Text("Atrás") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateSheet = true }) {
                Text("+")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                resourceTypeFilters.forEach { filter ->
                    FilterChip(
                        selected = state.selectedType == filter.value,
                        onClick = { onTypeSelected(filter.value) },
                        label = { Text(filter.label) }
                    )
                }
            }
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.errorMessage != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetryClick, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Reintentar")
                    }
                }
                state.resources.isEmpty() -> ResourceEmptyState()
                state.filteredResources.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay recursos para este filtro")
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredResources, key = { it.idRecurso }) { resource ->
                        ResourceCard(
                            resource = resource,
                            onClick = { onResourceClick(resource.idRecurso, resource.tipo) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        ModalBottomSheet(onDismissRequest = { showCreateSheet = false }) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        showCreateSheet = false
                        onUploadImageClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Subir imagen")
                }
                OutlinedButton(
                    onClick = {
                        showCreateSheet = false
                        onCreateCopyClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear copy")
                }
            }
        }
    }
}
