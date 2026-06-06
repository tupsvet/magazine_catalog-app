package com.magazines.catalog.presentation.mymagazines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.MagazineStatus
import com.magazines.catalog.presentation.components.EmptyState
import com.magazines.catalog.presentation.components.ErrorMessage
import com.magazines.catalog.presentation.components.LoadingIndicator
import com.magazines.catalog.presentation.components.MagazineCoverImage
import com.magazines.catalog.presentation.theme.OrangePrimary
import com.magazines.catalog.presentation.theme.TextPrimary
import com.magazines.catalog.presentation.theme.TextSecondary

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "ИЗДАТЕЛЮ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary,
                    letterSpacing = 2.sp,
                )
                Text(
                    text = "Мои журналы",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToUpload,
                containerColor = OrangePrimary,
                shape = RoundedCornerShape(50.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Журнал",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            val tabs = listOf(
                "Одобренные ${uiState.approved.size}",
                "На модерации ${uiState.pending.size}",
                "Отклонённые ${uiState.rejected.size}",
            )
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = Color.Transparent,
                contentColor = OrangePrimary,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        color = OrangePrimary,
                        modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                    )
                },
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.selectedTab == index) OrangePrimary else TextSecondary,
                            )
                        },
                    )
                }
            }

            when {
                uiState.isLoading && uiState.approved.isEmpty() &&
                    uiState.pending.isEmpty() && uiState.rejected.isEmpty() -> {
                    LoadingIndicator(modifier = Modifier.fillMaxSize())
                }
                uiState.error != null && uiState.approved.isEmpty() &&
                    uiState.pending.isEmpty() && uiState.rejected.isEmpty() -> {
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
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = magazinesForTab,
                            key = { it.id },
                        ) { magazine ->
                            MyMagazineListItem(
                                magazine = magazine,
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
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp)),
            ) {
                MagazineCoverImage(
                    coverUrl = magazine.coverUrl,
                    contentDescription = magazine.title,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent),
                            ),
                        )
                        .padding(4.dp),
                ) {
                    Text(
                        text = magazine.categoryName.take(6),
                        fontSize = 7.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = magazine.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                )
                Text(
                    text = buildSubtitle(magazine),
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                when (magazine.status) {
                    MagazineStatus.APPROVED -> StatusBadge(
                        text = "● ОДОБРЕНО",
                        textColor = Color(0xFF2E7D32),
                        bgColor = Color(0xFFE8F5E9),
                    )
                    MagazineStatus.PENDING -> StatusBadge(
                        text = "● НА МОДЕРАЦИИ",
                        textColor = OrangePrimary,
                        bgColor = Color(0xFFFFF3E8),
                    )
                    MagazineStatus.REJECTED -> StatusBadge(
                        text = "● ОТКЛОНЕНО",
                        textColor = Color(0xFFE53935),
                        bgColor = Color(0xFFFFF0F0),
                    )
                }
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    textColor: Color,
    bgColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = textColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun buildSubtitle(magazine: Magazine): String {
    val issueText = "№${magazine.issuesCount}"
    val date = formatShortDate(magazine.createdAt) ?: return issueText
    return "$issueText · обновлён $date"
}

private fun formatShortDate(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    val datePart = iso.substringBefore('T').split('-')
    if (datePart.size != 3) return null
    val year = datePart[0].toIntOrNull() ?: return null
    val month = datePart[1].toIntOrNull() ?: return null
    val day = datePart[2].toIntOrNull() ?: return null
    if (month !in 1..12 || day !in 1..31 || year < 1900) return null
    val months = listOf(
        "янв", "фев", "мар", "апр", "мая", "июн",
        "июл", "авг", "сен", "окт", "ноя", "дек",
    )
    return "$day ${months[month - 1]}"
}
