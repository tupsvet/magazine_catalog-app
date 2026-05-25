package com.magazines.catalog.presentation.mymagazines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.presentation.components.EmptyState
import com.magazines.catalog.presentation.components.ErrorMessage
import com.magazines.catalog.presentation.components.LoadingIndicator
import com.magazines.catalog.presentation.components.MagazineCard

private val tabTitles = listOf("Одобренные", "На модерации", "Отклонённые")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMagazinesScreen(
    onNavigateToUpload: () -> Unit,
    onMagazineClick: (String) -> Unit,
    refreshRequested: Boolean = false,
    onRefreshHandled: () -> Unit = {},
    viewModel: MyMagazinesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(refreshRequested) {
        if (refreshRequested) {
            viewModel.refresh()
            onRefreshHandled()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    val magazinesForTab = when (uiState.selectedTab) {
        0 -> uiState.approved
        1 -> uiState.pending
        else -> uiState.rejected
    }
    val showRejectionReason = uiState.selectedTab == 2

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Мои журналы") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToUpload) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Загрузить журнал",
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title) },
                    )
                }
            }

            when {
                uiState.isLoading && magazinesForTab.isEmpty() && uiState.approved.isEmpty() &&
                    uiState.pending.isEmpty() && uiState.rejected.isEmpty() -> {
                    LoadingIndicator(modifier = Modifier.fillMaxSize())
                }
                uiState.error != null && magazinesForTab.isEmpty() &&
                    uiState.approved.isEmpty() && uiState.pending.isEmpty() && uiState.rejected.isEmpty() -> {
                    ErrorMessage(
                        message = uiState.error ?: "Ошибка загрузки",
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                magazinesForTab.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.CollectionsBookmark,
                        title = "Нет журналов",
                        subtitle = "В этой категории пока ничего нет",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = magazinesForTab,
                            key = { it.id },
                        ) { magazine ->
                            MyMagazineListItem(
                                magazine = magazine,
                                currentUserId = uiState.currentUserId,
                                showRejectionReason = showRejectionReason,
                                onClick = { onMagazineClick(magazine.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyMagazineListItem(
    magazine: Magazine,
    currentUserId: String?,
    showRejectionReason: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MagazineCard(
            magazine = magazine,
            currentUserId = currentUserId,
            onClick = onClick,
        )
        if (showRejectionReason && !magazine.rejectionReason.isNullOrBlank()) {
            Text(
                text = "Причина отклонения: ${magazine.rejectionReason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
