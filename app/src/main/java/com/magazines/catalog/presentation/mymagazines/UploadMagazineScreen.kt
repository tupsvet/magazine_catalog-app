package com.magazines.catalog.presentation.mymagazines

import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.magazines.catalog.domain.model.Category
import com.magazines.catalog.presentation.theme.CreamBackground
import com.magazines.catalog.presentation.theme.FieldBorder
import com.magazines.catalog.presentation.theme.OrangePrimary
import com.magazines.catalog.presentation.theme.TextPrimary
import com.magazines.catalog.presentation.theme.TextSecondary

private val CoverPreviewBackground = Color(0xFFF0EDE8)
private val FieldShape = RoundedCornerShape(10.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadMagazineScreen(
    onNavigateBack: () -> Unit,
    onUploadSuccess: () -> Unit,
    viewModel: UploadMagazineViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val createdMagazineId by viewModel.createdMagazineId.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var title by rememberSaveable { mutableStateOf("") }
    var publisher by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let(viewModel::pickImage)
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

    LaunchedEffect(uiState.categories) {
        if (selectedCategory == null && uiState.categories.isNotEmpty()) {
            selectedCategory = uiState.categories.first()
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
                UploadTopBar(
                    currentStep = currentStep,
                    onNavigateBack = onNavigateBack,
                )
                StepIndicator(currentStep = currentStep)

                when (currentStep) {
                    1 -> Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CoverBlock(
                                selectedImageUri = uiState.selectedImageUri,
                                context = context,
                                onPickImage = { imagePicker.launch("image/*") },
                            )

                            FormSection(
                                title = title,
                                onTitleChange = { title = it },
                                publisher = publisher,
                                onPublisherChange = { publisher = it },
                                description = description,
                                onDescriptionChange = { description = it },
                                categories = uiState.categories,
                                selectedCategory = selectedCategory,
                                categoryExpanded = categoryExpanded,
                                isLoadingCategories = uiState.isLoadingCategories,
                                onCategoryExpandedChange = { categoryExpanded = it },
                                onCategorySelected = {
                                    selectedCategory = it
                                    categoryExpanded = false
                                },
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        ContinueButton(
                            enabled = !uiState.isSubmitting && selectedCategory != null,
                            onClick = {
                                val category = selectedCategory ?: return@ContinueButton
                                viewModel.submit(
                                    title = title,
                                    publisher = publisher,
                                    yearFounded = null,
                                    categoryId = category.id,
                                    description = description,
                                )
                            },
                        )
                    }
                    else -> UploadIssueStep(
                        magazineId = createdMagazineId.orEmpty(),
                        magazineTitle = title,
                        onSkip = viewModel::onSkipIssue,
                        onSuccess = onUploadSuccess,
                        viewModel = viewModel,
                    )
                }
            }

            if (currentStep == 1 && uiState.isSubmitting) {
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
private fun UploadTopBar(
    currentStep: Int,
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
                text = "Новый журнал",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = if (currentStep == 1) "Шаг 1 из 2 · описание" else "Шаг 2 из 2 · выпуск",
                fontSize = 12.sp,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepCircle(number = 1, isActive = currentStep >= 1, isDone = currentStep > 1)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(if (currentStep > 1) OrangePrimary else FieldBorder),
            )
            StepCircle(number = 2, isActive = currentStep >= 2, isDone = false)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            Text(
                text = "Описание",
                fontSize = 11.sp,
                color = if (currentStep >= 1) OrangePrimary else TextSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Выпуск",
                fontSize = 11.sp,
                color = if (currentStep >= 2) OrangePrimary else TextSecondary,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun StepCircle(number: Int, isActive: Boolean, isDone: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isActive) OrangePrimary else FieldBorder),
        contentAlignment = Alignment.Center,
    ) {
        if (isDone) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        } else {
            Text(
                text = number.toString(),
                color = if (isActive) Color.White else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadIssueStep(
    magazineId: String,
    magazineTitle: String,
    onSkip: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: UploadMagazineViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var issueNumberText by rememberSaveable { mutableStateOf("") }
    var publicationDate by rememberSaveable { mutableStateOf("") }
    var issueTitleText by rememberSaveable { mutableStateOf("") }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let(viewModel::pickPdf)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            IssueStepTopBar(magazineTitle = magazineTitle)

            IssueNumberAndDateRow(
                issueNumber = issueNumberText,
                onIssueNumberChange = { value ->
                    issueNumberText = value.filter { it.isDigit() }
                },
                publicationDate = publicationDate,
                onPublicationDateChange = { publicationDate = it },
            )

            Spacer(modifier = Modifier.height(12.dp))

            IssueTitleField(
                value = issueTitleText,
                onValueChange = { issueTitleText = it },
            )

            Spacer(modifier = Modifier.height(12.dp))

            IssuePdfBlock(
                fileName = uiState.selectedPdfName,
                sizeMb = uiState.pdfSizeMb,
                hasPdf = uiState.selectedPdfUri != null,
                onPick = { pdfPicker.launch("application/pdf") },
                onClear = viewModel::clearPdf,
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        IssueStepBottomButtons(
            isLoading = uiState.isIssueSubmitting,
            canPublish = uiState.selectedPdfUri != null && issueNumberText.isNotBlank(),
            onPublish = {
                val number = issueNumberText.toIntOrNull() ?: return@IssueStepBottomButtons
                viewModel.submitIssue(
                    issueNumber = number,
                    publicationDate = publicationDate,
                )
            },
            onSkip = onSkip,
        )
    }
}

@Composable
private fun IssueStepTopBar(magazineTitle: String) {
    Row(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ШАГ 2 ИЗ 2",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = OrangePrimary,
                letterSpacing = 2.sp,
            )
            Text(
                text = "Новый выпуск",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = "Журнал «$magazineTitle»",
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IssueNumberAndDateRow(
    issueNumber: String,
    onIssueNumberChange: (String) -> Unit,
    publicationDate: String,
    onPublicationDateChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.width(80.dp)) {
            Text(
                text = "Номер",
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
            )
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
            Text(
                text = "Дата выхода",
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = publicationDate,
                onValueChange = onPublicationDateChange,
                modifier = Modifier.fillMaxWidth(),
                shape = FieldShape,
                singleLine = true,
                placeholder = {
                    Text(text = "Декабрь 2025", color = TextSecondary, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IssueTitleField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Заголовок выпуска",
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = FieldShape,
            singleLine = true,
            placeholder = {
                Text(text = "Зимние специи", color = TextSecondary, fontSize = 14.sp)
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

private val PdfHighlight = Color(0xFFFFF3E8)

@Composable
private fun IssuePdfBlock(
    fileName: String?,
    sizeMb: String?,
    hasPdf: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "PDF-файл",
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (hasPdf && fileName != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            .size(38.dp)
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
                        )
                        Text(
                            text = if (sizeMb != null) "$sizeMb МБ · PDF" else "PDF",
                            fontSize = 11.sp,
                            color = TextSecondary,
                        )
                    }
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Убрать PDF",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(FieldShape)
                    .border(1.5.dp, FieldBorder, FieldShape)
                    .background(Color.White)
                    .clickable(onClick = onPick),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Выбрать PDF файл",
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
}

@Composable
private fun IssueStepBottomButtons(
    isLoading: Boolean,
    canPublish: Boolean,
    onPublish: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onPublish,
            enabled = !isLoading && canPublish,
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
        TextButton(
            onClick = onSkip,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Пропустить, добавлю позже",
                fontSize = 14.sp,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun CoverBlock(
    selectedImageUri: Uri?,
    context: android.content.Context,
    onPickImage: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                    .size(width = 64.dp, height = 80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CoverPreviewBackground),
                contentAlignment = Alignment.Center,
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(selectedImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Превью обложки",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(28.dp),
                        )
                        Text(
                            text = "Обложка",
                            fontSize = 10.sp,
                            color = TextSecondary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Обложка журнала",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                )
                Text(
                    text = "JPG или PNG, 3:4, минимум\n900×1200",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onPickImage,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.5.dp, OrangePrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp,
                        vertical = 6.dp,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Загрузить",
                        color = OrangePrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormSection(
    title: String,
    onTitleChange: (String) -> Unit,
    publisher: String,
    onPublisherChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    categories: List<Category>,
    selectedCategory: Category?,
    categoryExpanded: Boolean,
    isLoadingCategories: Boolean,
    onCategoryExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (Category) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FormGroup(label = "Название") {
            FormField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = "Название журнала",
                singleLine = true,
            )
        }

        FormGroup(label = "Издатель") {
            FormField(
                value = publisher,
                onValueChange = onPublisherChange,
                placeholder = "Название издательства",
                singleLine = true,
            )
        }

        FormGroup(label = "Категория") {
            CategoryDropdown(
                categories = categories,
                selectedCategory = selectedCategory,
                expanded = categoryExpanded,
                isLoading = isLoadingCategories,
                onExpandedChange = onCategoryExpandedChange,
                onCategorySelected = onCategorySelected,
            )
        }

        FormGroup(label = "Описание") {
            FormField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = "Расскажите, о чём журнал...",
                singleLine = false,
                minLines = 3,
                maxLines = 5,
            )
        }
    }
}

@Composable
private fun FormGroup(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = TextSecondary,
                fontSize = 14.sp,
            )
        },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = FieldShape,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = TextPrimary,
            fontSize = 14.sp,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangePrimary,
            unfocusedBorderColor = FieldBorder,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = OrangePrimary,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<Category>,
    selectedCategory: Category?,
    expanded: Boolean,
    isLoading: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (Category) -> Unit,
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedCategory?.name
                ?: if (isLoading) "Загрузка..." else "Выберите категорию",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = FieldShape,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = TextPrimary,
                fontSize = 14.sp,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = FieldBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTrailingIconColor = OrangePrimary,
                unfocusedTrailingIconColor = TextSecondary,
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name, color = TextPrimary) },
                    onClick = { onCategorySelected(category) },
                )
            }
        }
    }
}

@Composable
private fun ContinueButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
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
        ) {
            Text(
                text = "Далее →",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}
