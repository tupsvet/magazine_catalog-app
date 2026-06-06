package com.magazines.catalog.presentation.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magazines.catalog.domain.model.Issue
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.MagazineStatus
import com.magazines.catalog.domain.model.Review
import com.magazines.catalog.presentation.components.ErrorMessage
import com.magazines.catalog.presentation.components.MagazineCoverImage
import com.magazines.catalog.presentation.components.RatingBar
import com.magazines.catalog.presentation.components.ReviewCard
import com.magazines.catalog.presentation.theme.CreamBackground
import com.magazines.catalog.presentation.theme.OrangePrimary
import com.magazines.catalog.presentation.theme.TextPrimary
import com.magazines.catalog.presentation.theme.TextSecondary

private val MiniCoverShape = RoundedCornerShape(8.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagazineDetailScreen(
    onNavigateBack: () -> Unit,
    onUploadIssue: (magazineId: String, magazineTitle: String) -> Unit,
    onIssueClick: (pdfUrl: String, issueTitle: String) -> Unit,
    viewModel: MagazineDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reviewToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.error, uiState.magazine) {
        val error = uiState.error ?: return@LaunchedEffect
        if (uiState.magazine != null) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить отзыв?") },
            text = { Text("Это действие нельзя отменить") },
            confirmButton = {
                TextButton(
                    onClick = {
                        reviewToDelete?.let { viewModel.deleteReview(it) }
                        showDeleteDialog = false
                        reviewToDelete = null
                    },
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        reviewToDelete = null
                    },
                ) {
                    Text("Отмена")
                }
            },
        )
    }

    Scaffold(
        containerColor = CreamBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.magazine == null -> {
                DetailLoadingContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                )
            }
            uiState.magazine == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorMessage(
                        message = uiState.error ?: "Ошибка загрузки",
                        onRetry = { viewModel.retry() },
                    )
                }
            }
            else -> {
                MagazineDetailContent(
                    magazine = uiState.magazine!!,
                    issues = uiState.issues,
                    reviews = uiState.reviews,
                    isFavorite = uiState.isFavorite,
                    isLoggedIn = uiState.currentUserId != null,
                    isOwnerOrAdmin = uiState.isCurrentUserOwner || uiState.isAdmin,
                    currentUserId = uiState.currentUserId,
                    isAdmin = uiState.isAdmin,
                    currentUserReview = uiState.currentUserReview,
                    isSubmittingReview = uiState.isSubmittingReview,
                    isTogglingFavorite = uiState.isTogglingFavorite,
                    onNavigateBack = onNavigateBack,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onUploadIssue = {
                        val mag = uiState.magazine!!
                        onUploadIssue(mag.id, mag.title)
                    },
                    onIssueClick = onIssueClick,
                    onCreateReview = viewModel::createReview,
                    onRequestDeleteReview = { reviewId ->
                        reviewToDelete = reviewId
                        showDeleteDialog = true
                    },
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun MagazineDetailContent(
    magazine: Magazine,
    issues: List<Issue>,
    reviews: List<Review>,
    isFavorite: Boolean,
    isLoggedIn: Boolean,
    isOwnerOrAdmin: Boolean,
    currentUserId: String?,
    isAdmin: Boolean,
    currentUserReview: Review?,
    isSubmittingReview: Boolean,
    isTogglingFavorite: Boolean,
    onNavigateBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onUploadIssue: () -> Unit,
    onIssueClick: (pdfUrl: String, issueTitle: String) -> Unit,
    onCreateReview: (Int, String?) -> Unit,
    onRequestDeleteReview: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val latestIssue = issues.firstOrNull()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            DetailHero(
                coverUrl = magazine.coverUrl,
                title = magazine.title,
                onNavigateBack = onNavigateBack,
                showUploadMenu = isOwnerOrAdmin,
                onUploadIssue = onUploadIssue,
            )
        }

        item {
            InfoCard(
                magazine = magazine,
                issuesCount = issues.size,
                modifier = Modifier.offset(y = (-20).dp),
            )
        }

        item {
            ActionsRow(
                hasIssues = latestIssue != null,
                isFavorite = isFavorite,
                isLoggedIn = isLoggedIn,
                isTogglingFavorite = isTogglingFavorite,
                onReadLatest = {
                    latestIssue?.let {
                        onIssueClick(it.pdfUrl, "Выпуск № ${it.issueNumber}")
                    }
                },
                onToggleFavorite = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleFavorite()
                },
                modifier = Modifier.offset(y = (-12).dp),
            )
        }

        magazine.description?.takeIf { it.isNotBlank() }?.let { description ->
            item {
                DescriptionSection(
                    description = description,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        item {
            SectionHeader(
                title = "Выпуски",
                trailing = if (issues.isNotEmpty()) "Все ${issues.size}" else null,
            )
        }

        if (issues.isEmpty()) {
            item {
                EmptySectionText(text = "Выпусков пока нет")
            }
        } else {
            items(issues, key = { it.id }) { issue ->
                IssueRow(
                    issue = issue,
                    onRead = {
                        onIssueClick(issue.pdfUrl, "Выпуск № ${issue.issueNumber}")
                    },
                )
            }
        }

        item {
            SectionHeader(
                title = "Отзывы",
                trailing = "%.1f · %d".format(magazine.averageRating, magazine.reviewsCount),
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        if (isLoggedIn && currentUserReview == null) {
            item {
                ReviewForm(
                    isSubmitting = isSubmittingReview,
                    onSubmit = onCreateReview,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        items(reviews, key = { it.id }) { review ->
            val canDelete = currentUserId != null &&
                (review.userId == currentUserId || isAdmin)
            ReviewCard(
                review = review,
                canDelete = canDelete,
                onDelete = { onRequestDeleteReview(review.id) },
            )
        }

        if (reviews.isEmpty() && (currentUserReview != null || !isLoggedIn)) {
            item {
                EmptySectionText(text = "Пока нет отзывов")
            }
        }
    }
}

@Composable
private fun DetailHero(
    coverUrl: String?,
    title: String,
    onNavigateBack: () -> Unit,
    showUploadMenu: Boolean,
    onUploadIssue: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
    ) {
        MagazineCoverImage(
            coverUrl = coverUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f),
                        ),
                    ),
                ),
        )

        CircleIconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = Color.White,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        ) {
            CircleIconButton(
                onClick = { if (showUploadMenu) menuExpanded = true },
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Меню",
                    tint = Color.White,
                )
            }
            if (showUploadMenu) {
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Загрузить выпуск") },
                        onClick = {
                            menuExpanded = false
                            onUploadIssue()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun InfoCard(
    magazine: Magazine,
    issuesCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = CreamBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Card(
                modifier = Modifier.size(width = 70.dp, height = 90.dp),
                shape = MiniCoverShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                MagazineCoverImage(
                    coverUrl = magazine.coverUrl,
                    contentDescription = magazine.title,
                    modifier = Modifier.fillMaxSize(),
                    shape = MiniCoverShape,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                if (magazine.status == MagazineStatus.APPROVED) {
                    StatusBadgeApproved()
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = magazine.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 26.sp,
                )
                Text(
                    text = magazine.publisher,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val rating = magazine.averageRating.toInt().coerceIn(0, 5)
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < rating) OrangePrimary else Color(0xFFE0E0E0),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "%.1f".format(magazine.averageRating),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Text(
                        text = " · ${magazine.categoryName}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                Text(
                    text = "$issuesCount выпусков",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun StatusBadgeApproved() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFE8F5E9))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = "● ОДОБРЕНО",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
private fun ActionsRow(
    hasIssues: Boolean,
    isFavorite: Boolean,
    isLoggedIn: Boolean,
    isTogglingFavorite: Boolean,
    onReadLatest: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onReadLatest,
            enabled = hasIssues,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                contentColor = Color.White,
                disabledContainerColor = OrangePrimary.copy(alpha = 0.4f),
                disabledContentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Читать выпуск",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }

        if (isLoggedIn) {
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = onToggleFavorite,
                enabled = !isTogglingFavorite,
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, OrangePrimary),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = if (isFavorite) "Убрать из избранного" else "В избранное",
                    tint = OrangePrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun DescriptionSection(
    description: String,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
    ) {
        Text(
            text = description,
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (expanded) "Свернуть" else "Читать далее",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = OrangePrimary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    trailing: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) {
            Text(
                text = trailing,
                fontSize = 13.sp,
                color = OrangePrimary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EmptySectionText(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = TextSecondary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun IssueRow(
    issue: Issue,
    onRead: () -> Unit,
) {
    val shortNumber = issue.issueNumber.filter { it.isDigit() }.takeLast(2).ifEmpty { "01" }
    val pages = issue.pagesCount?.let { "$it стр." } ?: "—"
    val subtitle = "${issue.publicationDate.orEmpty()} · $pages"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangePrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = shortNumber,
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = issue.issueNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
            }

            OutlinedButton(
                onClick = onRead,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, OrangePrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Читать",
                    color = OrangePrimary,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewForm(
    isSubmitting: Boolean,
    onSubmit: (Int, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var rating by rememberSaveable { mutableIntStateOf(0) }
    var comment by rememberSaveable { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Оставить отзыв",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            RatingBar(
                rating = rating,
                onRatingChange = { rating = it },
            )
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Комментарий") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    cursorColor = OrangePrimary,
                    focusedLabelColor = OrangePrimary,
                ),
            )
            Button(
                onClick = { onSubmit(rating, comment) },
                enabled = rating > 0 && !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor = Color.White,
                ),
                modifier = Modifier.align(Alignment.End),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                } else {
                    Text("Отправить")
                }
            }
        }
    }
}
