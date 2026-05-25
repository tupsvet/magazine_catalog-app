package com.magazines.catalog.presentation.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magazines.catalog.domain.model.Issue
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.domain.model.Review
import com.magazines.catalog.presentation.components.ErrorMessage
import com.magazines.catalog.presentation.components.MagazineCoverImage
import com.magazines.catalog.presentation.components.RatingBar
import com.magazines.catalog.presentation.components.ReviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagazineDetailScreen(
    onNavigateBack: () -> Unit,
    onUploadIssue: (String) -> Unit,
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.magazine?.title ?: "Журнал") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.magazine == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
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
                    onToggleFavorite = viewModel::toggleFavorite,
                    onUploadIssue = { onUploadIssue(uiState.magazine!!.id) },
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
    onToggleFavorite: () -> Unit,
    onUploadIssue: () -> Unit,
    onIssueClick: (pdfUrl: String, issueTitle: String) -> Unit,
    onCreateReview: (Int, String?) -> Unit,
    onRequestDeleteReview: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            CoverHero(coverUrl = magazine.coverUrl, title = magazine.title)
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = magazine.title,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = magazine.publisher,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Основан: ${magazine.yearFounded} · ${magazine.categoryName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "⭐ ${"%.1f".format(magazine.averageRating)} · ${magazine.reviewsCount} отзывов",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isLoggedIn) {
                        OutlinedButton(
                            onClick = onToggleFavorite,
                            enabled = !isTogglingFavorite,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = if (isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                                contentDescription = if (isFavorite) {
                                    "Убрать из избранного"
                                } else {
                                    "Добавить в избранное"
                                },
                                tint = if (isFavorite) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isFavorite) "В избранном" else "В избранное")
                        }
                    }
                    if (isOwnerOrAdmin) {
                        Button(
                            onClick = onUploadIssue,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Загрузить выпуск")
                        }
                    }
                }
            }
        }

        magazine.description?.takeIf { it.isNotBlank() }?.let { description ->
            item {
                ExpandableDescription(
                    description = description,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        item {
            IssuesSection(
                issues = issues,
                onReadIssue = onIssueClick,
            )
        }

        item {
            Text(
                text = "Отзывы",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp),
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }

        if (reviews.isEmpty() && (currentUserReview != null || !isLoggedIn)) {
            item {
                Text(
                    text = "Пока нет отзывов",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun CoverHero(
    coverUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
    ) {
        MagazineCoverImage(
            coverUrl = coverUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ExpandableDescription(
    description: String,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val maxCollapsedLines = 3

    Column(
        modifier = modifier
            .animateContentSize()
            .clickable { expanded = !expanded },
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expanded) Int.MAX_VALUE else maxCollapsedLines,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = if (expanded) "Свернуть" else "Читать далее",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun IssuesSection(
    issues: List<Issue>,
    onReadIssue: (pdfUrl: String, issueTitle: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Выпуски",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        if (issues.isEmpty()) {
            Text(
                text = "Выпусков пока нет",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(issues, key = { it.id }) { issue ->
                    IssueCard(
                        issue = issue,
                        onReadClick = {
                            onReadIssue(
                                issue.pdfUrl,
                                "Выпуск № ${issue.issueNumber}",
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun IssueCard(
    issue: Issue,
    onReadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.width(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "№ ${issue.issueNumber}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = issue.publicationDate ?: "Дата не указана",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            issue.pagesCount?.let { pages ->
                Text(
                    text = "$pages стр.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(
                onClick = onReadClick,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Читать")
            }
        }
    }
}

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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Оставить отзыв",
                style = MaterialTheme.typography.titleSmall,
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
            )
            Button(
                onClick = { onSubmit(rating, comment) },
                enabled = rating > 0 && !isSubmitting,
                modifier = Modifier.align(Alignment.End),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Отправить")
                }
            }
        }
    }
}
