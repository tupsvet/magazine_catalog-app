package com.magazines.catalog.presentation.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magazines.catalog.domain.model.Category
import com.magazines.catalog.presentation.components.EmptyState
import com.magazines.catalog.presentation.components.ErrorMessage
import com.magazines.catalog.presentation.components.MagazineCard
import com.magazines.catalog.presentation.components.ShimmerMagazineCard
import com.magazines.catalog.presentation.theme.CreamBackground
import com.magazines.catalog.presentation.theme.FieldBorder
import com.magazines.catalog.presentation.theme.OrangePrimary
import com.magazines.catalog.presentation.theme.TextPrimary
import com.magazines.catalog.presentation.theme.TextSecondary
import kotlinx.coroutines.flow.distinctUntilChanged

private val ChipInactiveBackground = Color(0xFFEFECE8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onMagazineClick: (String) -> Unit,
    onAddMagazine: () -> Unit = {},
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var searchText by rememberSaveable { mutableStateOf("") }
    val gridState = rememberLazyGridState()

    LaunchedEffect(uiState.error, uiState.magazines.size) {
        val error = uiState.error ?: return@LaunchedEffect
        if (uiState.magazines.isNotEmpty()) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.selectedCategoryId, uiState.searchQuery) {
        gridState.scrollToItem(0)
    }

    LaunchedEffect(gridState, uiState.magazines.size, uiState.totalPages, uiState.isLoading) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalItems = layoutInfo.totalItemsCount
            lastVisibleIndex to totalItems
        }
            .distinctUntilChanged()
            .collect { (lastVisibleIndex, totalItems) ->
                val state = viewModel.uiState.value
                if (
                    !state.isLoading &&
                    !state.isLoadingMore &&
                    totalItems > 0 &&
                    lastVisibleIndex >= totalItems - 1
                ) {
                    viewModel.loadNextPage()
                }
            }
    }

    Scaffold(
        containerColor = CreamBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = { AddMagazineFab(onClick = onAddMagazine) },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.loadMagazines(refresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CatalogTopBar()

                SearchField(
                    value = searchText,
                    onValueChange = { value ->
                        searchText = value
                        viewModel.search(value)
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                CategoryChipsRow(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onCategorySelected = viewModel::filterByCategory,
                )

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    uiState.isLoading && uiState.magazines.isEmpty() -> {
                        CatalogShimmerGrid(modifier = Modifier.weight(1f))
                    }
                    !uiState.isLoading && uiState.magazines.isEmpty() && uiState.error != null -> {
                        ErrorMessage(
                            message = uiState.error ?: "Ошибка загрузки",
                            onRetry = { viewModel.retry() },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    !uiState.isLoading && uiState.magazines.isEmpty() -> {
                        EmptyState(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            title = "Журналы не найдены",
                            subtitle = "Попробуйте изменить поиск или категорию",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = gridState,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 4.dp,
                                bottom = 96.dp,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(
                                items = uiState.magazines,
                                key = { it.id },
                            ) { magazine ->
                                MagazineCard(
                                    magazine = magazine,
                                    currentUserId = uiState.currentUserId,
                                    onClick = { onMagazineClick(magazine.id) },
                                )
                            }

                            if (uiState.isLoadingMore) {
                                item(span = { GridItemSpan(2) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(color = OrangePrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "СВЕЖИЕ ВЫПУСКИ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OrangePrimary,
                letterSpacing = 2.sp,
            )
            Text(
                text = "Каталог",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Уведомления",
                tint = TextPrimary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(50.dp),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary,
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Очистить",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
        placeholder = {
            Text(
                text = "Поиск журналов",
                color = TextSecondary,
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = FieldBorder,
            focusedBorderColor = OrangePrimary,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            cursorColor = OrangePrimary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChipsRow(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            CategoryChip(
                label = "Все",
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) },
            )
        }
        items(categories, key = { it.id }) { category ->
            CategoryChip(
                label = category.name,
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = ChipInactiveBackground,
            labelColor = TextPrimary,
            selectedContainerColor = OrangePrimary,
            selectedLabelColor = Color.White,
        ),
        label = {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

@Composable
private fun AddMagazineFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = OrangePrimary,
        contentColor = Color.White,
        shape = RoundedCornerShape(50.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Журнал",
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CatalogShimmerGrid(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 4.dp,
            bottom = 96.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
    ) {
        items(count = 6) {
            ShimmerMagazineCard()
        }
    }
}
