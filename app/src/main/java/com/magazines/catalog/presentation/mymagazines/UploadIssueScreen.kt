package com.magazines.catalog.presentation.mymagazines

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.magazines.catalog.presentation.theme.CreamBackground
import com.magazines.catalog.presentation.theme.FieldBorder
import com.magazines.catalog.presentation.theme.OrangePrimary
import com.magazines.catalog.presentation.theme.TextPrimary
import com.magazines.catalog.presentation.theme.TextSecondary

private val FieldShape = RoundedCornerShape(10.dp)
private val PdfHighlight = Color(0xFFFFF3E8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadIssueScreen(
    onNavigateBack: () -> Unit,
    onUploadSuccess: () -> Unit,
    magazineTitle: String? = null,
    viewModel: UploadIssueViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var issueNumberText by rememberSaveable { mutableStateOf("") }
    var publicationDate by rememberSaveable { mutableStateOf("") }
    var issueTitle by rememberSaveable { mutableStateOf("") }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let(viewModel::pickPdf)
    }

    val pdfSizeMb = remember(uiState.selectedPdfUri) {
        uiState.selectedPdfUri?.let { uri -> queryFileSizeMb(context, uri) }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onUploadSuccess()
        }
    }

    Scaffold(
        containerColor = CreamBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                IssueTopBar(
                    magazineTitle = magazineTitle,
                    onNavigateBack = onNavigateBack,
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    NumberAndDateRow(
                        issueNumber = issueNumberText,
                        onIssueNumberChange = { value ->
                            issueNumberText = value.filter { it.isDigit() }
                        },
                        publicationDate = publicationDate,
                        onPublicationDateChange = { publicationDate = it },
                    )

                    LabeledField(label = "Заголовок выпуска") {
                        IssueOutlinedField(
                            value = issueTitle,
                            onValueChange = { issueTitle = it },
                            placeholder = "Зимние специи",
                            singleLine = true,
                        )
                    }

                    LabeledField(label = "PDF-файл") {
                        PdfSelector(
                            fileName = uiState.selectedPdfName,
                            sizeMb = pdfSizeMb,
                            hasPdf = uiState.selectedPdfUri != null,
                            onPick = { pdfPicker.launch("application/pdf") },
                        )
                    }
                }

                PublishButton(
                    enabled = !uiState.isSubmitting &&
                        issueNumberText.isNotBlank() &&
                        uiState.selectedPdfUri != null,
                    isLoading = uiState.isSubmitting,
                    onClick = {
                        val number = issueNumberText.toIntOrNull() ?: return@PublishButton
                        viewModel.submit(
                            issueNumber = number,
                            publicationDate = publicationDate,
                        )
                    },
                )
            }

            if (uiState.isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
            }
        }
    }
}

@Composable
private fun IssueTopBar(
    magazineTitle: String?,
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
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = "Новый выпуск",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = if (!magazineTitle.isNullOrBlank()) {
                    "Журнал «$magazineTitle»"
                } else {
                    "Загрузка нового выпуска"
                },
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun NumberAndDateRow(
    issueNumber: String,
    onIssueNumberChange: (String) -> Unit,
    publicationDate: String,
    onPublicationDateChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.width(80.dp)) {
            FieldLabel("Номер")
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = issueNumber,
                onValueChange = onIssueNumberChange,
                modifier = Modifier.fillMaxWidth(),
                shape = FieldShape,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = TextPrimary,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = FieldBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = OrangePrimary,
                ),
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            FieldLabel("Дата выхода")
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = publicationDate,
                onValueChange = onPublicationDateChange,
                modifier = Modifier.fillMaxWidth(),
                shape = FieldShape,
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Декабрь 2025",
                        color = TextSecondary,
                        fontSize = 14.sp,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp),
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
    }
}

@Composable
private fun LabeledField(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FieldLabel(label)
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = TextSecondary,
        fontWeight = FontWeight.Medium,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IssueOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = FieldShape,
        singleLine = singleLine,
        placeholder = {
            Text(text = placeholder, color = TextSecondary, fontSize = 14.sp)
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

@Composable
private fun PdfSelector(
    fileName: String?,
    sizeMb: String?,
    hasPdf: Boolean,
    onPick: () -> Unit,
) {
    if (hasPdf && fileName != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onPick),
            shape = FieldShape,
            border = BorderStroke(1.5.dp, OrangePrimary),
            colors = CardDefaults.cardColors(containerColor = PdfHighlight),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OrangePrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (sizeMb != null) "$sizeMb МБ · PDF" else "PDF файл",
                        fontSize = 11.sp,
                        color = TextSecondary,
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(FieldShape)
                .background(Color.White)
                .border(1.5.dp, FieldBorder, FieldShape)
                .clickable(onClick = onPick),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Выбрать файл",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
                Text(
                    text = "до 50 МБ",
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun PublishButton(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.padding(16.dp)) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                contentColor = Color.White,
                disabledContainerColor = OrangePrimary.copy(alpha = 0.4f),
                disabledContentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Опубликовать выпуск",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
    }
}

private fun queryFileSizeMb(context: android.content.Context, uri: Uri): String? {
    if (uri == Uri.EMPTY) return null
    val bytes = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else null
            }
    }.getOrNull() ?: return null
    return "%.1f".format(bytes / 1_048_576.0)
}
