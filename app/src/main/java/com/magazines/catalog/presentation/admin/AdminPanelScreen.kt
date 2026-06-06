package com.magazines.catalog.presentation.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magazines.catalog.domain.model.Magazine
import com.magazines.catalog.presentation.components.ErrorMessage
import com.magazines.catalog.presentation.components.LoadingIndicator
import com.magazines.catalog.presentation.components.MagazineCoverImage
import com.magazines.catalog.presentation.theme.CreamBackground
import com.magazines.catalog.presentation.theme.FieldBorder
import com.magazines.catalog.presentation.theme.OrangePrimary
import com.magazines.catalog.presentation.theme.TextPrimary
import com.magazines.catalog.presentation.theme.TextSecondary

private val ApproveGreen = Color(0xFF4CAF50)
private val RejectRed = Color(0xFFE53935)
private val RejectBackground = Color(0xFFFFF0F0)
private val BadgeBackground = Color(0xFFFFF3E8)
private val CoverShape = RoundedCornerShape(10.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminPanelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var rejectTarget by remember { mutableStateOf<Magazine?>(null) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = CreamBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            AdminTopBar(
                pendingCount = uiState.pendingMagazines.size,
                onNavigateBack = onNavigateBack,
            )

            when {
                uiState.isLoading && uiState.pendingMagazines.isEmpty() -> {
                    LoadingIndicator(modifier = Modifier.fillMaxSize())
                }
                uiState.error != null && uiState.pendingMagazines.isEmpty() -> {
                    ErrorMessage(
                        message = uiState.error ?: "Ошибка загрузки",
                        onRetry = { viewModel.load() },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                uiState.pendingMagazines.isEmpty() -> {
                    EmptyQueue(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = uiState.pendingMagazines,
                            key = { it.id },
                        ) { magazine ->
                            PendingMagazineCard(
                                magazine = magazine,
                                isProcessing = magazine.id in uiState.processingIds,
                                onApprove = { viewModel.approve(magazine.id) },
                                onReject = { rejectTarget = magazine },
                            )
                        }
                    }
                }
            }
        }
    }

    rejectTarget?.let { target ->
        RejectDialog(
            onDismiss = { rejectTarget = null },
            onConfirm = { reason ->
                viewModel.reject(target.id, reason)
                rejectTarget = null
            },
        )
    }
}

@Composable
private fun AdminTopBar(
    pendingCount: Int,
    onNavigateBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = TextPrimary,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "МОДЕРАЦИЯ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OrangePrimary,
                letterSpacing = 2.sp,
            )
            Text(
                text = "Панель админа",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
        }
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BadgeBackground)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = "$pendingCount В ОЧЕРЕДИ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OrangePrimary,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@Composable
private fun PendingMagazineCard(
    magazine: Magazine,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoverWithCategory(
                    coverUrl = magazine.coverUrl,
                    title = magazine.title,
                    category = magazine.categoryName,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = magazine.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${magazine.publisher} · ${magazine.categoryName}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitleLine(magazine),
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                WaitingBadge()
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = FieldBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onApprove,
                    enabled = !isProcessing,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ApproveGreen,
                        contentColor = Color.White,
                        disabledContainerColor = ApproveGreen.copy(alpha = 0.4f),
                        disabledContentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Одобрить",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                OutlinedButton(
                    onClick = onReject,
                    enabled = !isProcessing,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, RejectRed),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = RejectBackground,
                        contentColor = RejectRed,
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = RejectRed,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Отклонить",
                        color = RejectRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (isProcessing) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = OrangePrimary,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CoverWithCategory(
    coverUrl: String?,
    title: String,
    category: String,
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CoverShape),
    ) {
        MagazineCoverImage(
            coverUrl = coverUrl,
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            shape = CoverShape,
        )
        if (category.isNotBlank()) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Text(
                    text = category.uppercase(),
                    fontSize = 8.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun WaitingBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BadgeBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "ЖДЁТ",
                fontSize = 10.sp,
                color = OrangePrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RejectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Причина отклонения",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
            )
        },
        text = {
            Column {
                Text(
                    text = "Укажите причину для автора журнала",
                    fontSize = 14.sp,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3,
                    maxLines = 5,
                    placeholder = {
                        Text(
                            text = "Опишите причину...",
                            color = TextSecondary,
                        )
                    },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = FieldBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = OrangePrimary,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(reason.trim().takeIf { it.isNotEmpty() })
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RejectRed,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "Отклонить",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Отмена", color = TextSecondary)
            }
        },
    )
}

@Composable
private fun EmptyQueue(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = ApproveGreen.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Всё проверено!",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Нет журналов на модерации",
            fontSize = 14.sp,
            color = TextSecondary,
        )
    }
}

private fun subtitleLine(magazine: Magazine): String {
    val date = formatShortDate(magazine.createdAt)
    val issues = "${magazine.issuesCount} ${pluralIssues(magazine.issuesCount)}"
    return if (date != null) "Отправлен $date · $issues" else issues
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

private fun pluralIssues(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "выпуск"
        mod10 in 2..4 && mod100 !in 12..14 -> "выпуска"
        else -> "выпусков"
    }
}
