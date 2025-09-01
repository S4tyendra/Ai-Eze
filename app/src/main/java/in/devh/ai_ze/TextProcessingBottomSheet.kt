package `in`.devh.ai_ze

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

enum class BottomSheetState {
    ACTION_SELECTION,
    PROCESSING,
    RESULT
}

@Composable
fun TextProcessingBottomSheet(
    selectedText: String,
    apiKeyManager: ApiKeyManager,
    onDismiss: () -> Unit
) {
    var currentState by remember { mutableStateOf(BottomSheetState.ACTION_SELECTION) }
    var selectedCategory by remember { mutableStateOf<ActionCategory?>(null) }
    var processedText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTemplate by remember { mutableStateOf<PromptTemplate?>(null) }

    val context = LocalContext.current
    val categories = remember { getActionCategories() }

    // Material 3 expressive animations
    val stateTransition = updateTransition(currentState, label = "state")

    val slideAnimation by stateTransition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "slide"
    ) { state ->
        when (state) {
            BottomSheetState.ACTION_SELECTION -> 0f
            BottomSheetState.PROCESSING -> -1f
            BottomSheetState.RESULT -> -2f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = currentState,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetX = { fullWidth ->
                        when (targetState) {
                            BottomSheetState.PROCESSING -> fullWidth
                            BottomSheetState.RESULT -> fullWidth
                            else -> -fullWidth
                        }
                    }
                ) togetherWith slideOutHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    targetOffsetX = { fullWidth ->
                        when (initialState) {
                            BottomSheetState.ACTION_SELECTION -> -fullWidth
                            BottomSheetState.PROCESSING -> -fullWidth
                            else -> fullWidth
                        }
                    }
                )
            },
            label = "content_transition"
        ) { state ->
            when (state) {
                BottomSheetState.ACTION_SELECTION -> {
                    ActionSelectionContent(
                        selectedText = selectedText,
                        selectedCategory = selectedCategory,
                        categories = categories,
                        onCategorySelected = { selectedCategory = it },
                        onBackPressed = { selectedCategory = null },
                        onActionSelected = { template ->
                            selectedTemplate = template
                            currentState = BottomSheetState.PROCESSING
                            handleAction(
                                template = template,
                                selectedText = selectedText,
                                apiKeyManager = apiKeyManager,
                                context = context,
                                onTextUpdate = { processedText = it },
                                onComplete = { currentState = BottomSheetState.RESULT },
                                onError = { error ->
                                    errorMessage = error
                                    currentState = BottomSheetState.RESULT
                                }
                            )
                        }
                    )
                }

                BottomSheetState.PROCESSING -> {
                    MorphingTextContent(
                        originalText = selectedText,
                        processedText = processedText,
                        isProcessing = true
                    )
                }

                BottomSheetState.RESULT -> {
                    Column {
                        MorphingTextContent(
                            originalText = selectedText,
                            processedText = processedText,
                            isProcessing = false
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BottomButtonGroup(
                            errorMessage = errorMessage,
                            onBackToMain = {
                                currentState = BottomSheetState.ACTION_SELECTION
                                selectedCategory = null
                                processedText = ""
                                errorMessage = null
                                selectedTemplate = null
                            },
                            onRetry = {
                                if (selectedTemplate != null) {
                                    processedText = ""
                                    errorMessage = null
                                    currentState = BottomSheetState.PROCESSING
                                    handleAction(
                                        template = selectedTemplate!!,
                                        selectedText = selectedText,
                                        apiKeyManager = apiKeyManager,
                                        context = context,
                                        onTextUpdate = { processedText = it },
                                        onComplete = { currentState = BottomSheetState.RESULT },
                                        onError = { error ->
                                            errorMessage = error
                                            currentState = BottomSheetState.RESULT
                                        }
                                    )
                                }
                            },
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AI Result", processedText)
                                clipboard.setPrimaryClip(clip)
                            },
                            onReplace = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AI Result", processedText)
                                clipboard.setPrimaryClip(clip)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionSelectionContent(
    selectedText: String,
    selectedCategory: ActionCategory?,
    categories: List<ActionCategory>,
    onCategorySelected: (ActionCategory) -> Unit,
    onBackPressed: () -> Unit,
    onActionSelected: (PromptTemplate) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header with back button for subcategories
        AnimatedContent(
            targetState = selectedCategory != null,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) togetherWith slideOutHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            },
            label = "header_transition"
        ) { hasCategory ->
            if (hasCategory && selectedCategory != null) {
                // Category header with back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = selectedCategory.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Main header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Text(
                        text = "Choose AI Action",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "\"${selectedText.take(80)}${if (selectedText.length > 80) "..." else ""}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Main content with proper spacing
        AnimatedContent(
            targetState = selectedCategory == null,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    initialOffsetX = { if (targetState) -it else it }
                ) + fadeIn() togetherWith
                slideOutHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    targetOffsetX = { if (initialState) it else -it }
                ) + fadeOut()
            },
            label = "content_transition"
        ) { isMainCategory ->
            if (isMainCategory) {
                // Show main categories
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(categories) { index, category ->
                        AnimatedCategoryButton(
                            category = category,
                            onClick = { onCategorySelected(category) },
                            index = index,
                            animationDelay = index * 50L
                        )
                    }
                }
            } else if (selectedCategory != null) {
                // Show actions for selected category
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(selectedCategory.templates) { index, template ->
                        AnimatedActionButton(
                            template = template,
                            icon = getIconForTemplate(template),
                            onClick = { onActionSelected(template) },
                            index = index,
                            animationDelay = index * 80L
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedCategoryButton(
    category: ActionCategory,
    onClick: () -> Unit,
    index: Int,
    animationDelay: Long
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 2 }
        ) + fadeIn(),
        label = "category_enter"
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "${category.templates.size} actions available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedActionButton(
    template: PromptTemplate,
    icon: ImageVector,
    onClick: () -> Unit,
    index: Int,
    animationDelay: Long
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 3 }
        ) + fadeIn() + scaleIn(initialScale = 0.8f),
        label = "action_enter"
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = template.displayName,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = template.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = getDescriptionForTemplate(template),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MorphingTextContent(
    originalText: String,
    processedText: String,
    isProcessing: Boolean
) {
    var displayText by remember { mutableStateOf(originalText) }
    var morphProgress by remember { mutableFloatStateOf(0f) }

    // Animate morphing progress
    val animatedProgress by animateFloatAsState(
        targetValue = if (processedText.isNotEmpty()) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "morph_progress"
    )

    // Text morphing effect
    LaunchedEffect(processedText, originalText) {
        if (processedText.isNotEmpty()) {
            val originalWords = originalText.split(" ")
            val processedWords = processedText.split(" ")
            val maxLength = maxOf(originalWords.size, processedWords.size)

            for (i in 0..maxLength) {
                val progress = i.toFloat() / maxLength
                morphProgress = progress

                val morphedText = buildString {
                    for (j in 0 until maxLength) {
                        when {
                            j < i -> {
                                // Already morphed - show new text
                                if (j < processedWords.size) {
                                    append(processedWords[j])
                                    if (j < maxLength - 1) append(" ")
                                }
                            }
                            j == i -> {
                                // Currently morphing - blend between old and new
                                val oldWord = if (j < originalWords.size) originalWords[j] else ""
                                val newWord = if (j < processedWords.size) processedWords[j] else ""

                                if (newWord.isNotEmpty()) {
                                    append(newWord)
                                    if (j < maxLength - 1) append(" ")
                                }
                            }
                            else -> {
                                // Not yet morphed - show original
                                if (j < originalWords.size) {
                                    append(originalWords[j])
                                    if (j < maxLength - 1) append(" ")
                                }
                            }
                        }
                    }
                }

                displayText = morphedText
                delay(150) // Smooth morphing delay
            }
        } else {
            displayText = originalText
            morphProgress = 0f
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isProcessing) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier.padding(20.dp)
        ) {
            Column {
                // Processing indicator
                if (isProcessing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI is transforming your text...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                // Morphing text with blur effect during transition
                val blurAmount by animateFloatAsState(
                    targetValue = if (morphProgress > 0f && morphProgress < 1f) 2f else 0f,
                    animationSpec = tween(200),
                    label = "blur"
                )

                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .blur(blurAmount.dp)
                        .heightIn(min = 60.dp, max = 300.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
private fun BottomButtonGroup(
    errorMessage: String?,
    onBackToMain: () -> Unit,
    onRetry: () -> Unit,
    onCopy: () -> Unit,
    onReplace: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Back button
            OutlinedButton(
                onClick = onBackToMain,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(16.dp)
                )
            }

            if (errorMessage != null) {
                // Error state - show retry button
                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(3f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }
            } else {
                // Success state - show retry, copy, replace
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(16.dp)
                    )
                }

                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = onReplace,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = "Replace",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryButton(
    category: ActionCategory,
    onClick: () -> Unit,
    index: Int,
    listSize: Int
) {
    val cornerRadius = 24.dp
    val shape = when {
        listSize == 1 -> RoundedCornerShape(cornerRadius)
        index == 0 -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
        index == listSize - 1 -> RoundedCornerShape(
            bottomStart = cornerRadius,
            bottomEnd = cornerRadius
        )

        else -> RoundedCornerShape(0.dp)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "${category.templates.size} actions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ActionButton(
    template: PromptTemplate,
    icon: ImageVector,
    onClick: () -> Unit,
    index: Int,
    listSize: Int
) {
    val cornerRadius = 24.dp
    val shape = when {
        listSize == 1 -> RoundedCornerShape(cornerRadius)
        index == 0 -> RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius,
            bottomStart = 2.dp,
            bottomEnd = 2.dp
        )

        index == listSize - 1 -> RoundedCornerShape(
            bottomStart = cornerRadius, bottomEnd = cornerRadius,
            topStart = 2.dp,
            topEnd = 2.dp,
        )

        else -> RoundedCornerShape(2.dp)
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = template.displayName,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = template.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = getDescriptionForTemplate(template),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun handleAction(
    template: PromptTemplate,
    selectedText: String,
    apiKeyManager: ApiKeyManager,
    context: Context,
    onTextUpdate: (String) -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
) {
    val activity = findActivity(context)
    if (activity == null) {
        onError("Unable to access activity context")
        return
    }

    activity.lifecycleScope.launch {
        try {
            val apiKey = apiKeyManager.getGeminiApiKey()
            if (apiKey == null) {
                onError("API key not found. Please configure your Gemini API key.")
                return@launch
            }

            val geminiClient = GeminiClient(apiKey)
            val prompt = template.getPrompt(selectedText)

            // Use streaming for smooth animation
            geminiClient.generateTextStream(prompt)
                .catch { exception ->
                    onError("Error: ${exception.message}")
                }
                .collect { streamedText ->
                    onTextUpdate(streamedText)
                }

            onComplete()
            geminiClient.close()

        } catch (e: Exception) {
            onError("Error: ${e.message}")
        }
    }
}

// Helper function to safely find ComponentActivity from context
private fun findActivity(context: Context): ComponentActivity? {
    var currentContext = context
    while (currentContext is ContextWrapper) {
        if (currentContext is ComponentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

private fun getIconForTemplate(template: PromptTemplate): ImageVector {
    return when (template) {
        PromptTemplate.REPHRASE -> Icons.Default.Refresh
        PromptTemplate.FIX_GRAMMAR -> Icons.Default.Spellcheck
        PromptTemplate.MODIFY_TONE_FORMAL -> Icons.Default.Business
        PromptTemplate.MODIFY_TONE_CASUAL -> Icons.Default.SentimentSatisfied
        PromptTemplate.MODIFY_TONE_POLITE -> Icons.Default.Favorite
        PromptTemplate.SUGGEST_REPLY -> Icons.AutoMirrored.Filled.Reply
        PromptTemplate.SUMMARIZE -> Icons.Default.Summarize
        PromptTemplate.EXPAND -> Icons.Default.ZoomOut
        PromptTemplate.TRANSLATE_TO_HINDI -> Icons.Default.Translate
        PromptTemplate.TRANSLATE_TO_ENGLISH -> Icons.Default.Translate
        PromptTemplate.EXPLAIN_LIKE_IM_5 -> Icons.Default.ChildCare
        PromptTemplate.MAKE_BULLET_POINTS -> Icons.AutoMirrored.Filled.FormatListBulleted
    }
}

private fun getDescriptionForTemplate(template: PromptTemplate): String {
    return when (template) {
        PromptTemplate.REPHRASE -> "Rewrite with different words"
        PromptTemplate.FIX_GRAMMAR -> "Correct grammar and spelling"
        PromptTemplate.MODIFY_TONE_FORMAL -> "Make it professional"
        PromptTemplate.MODIFY_TONE_CASUAL -> "Make it friendly and relaxed"
        PromptTemplate.MODIFY_TONE_POLITE -> "Make it more respectful"
        PromptTemplate.SUGGEST_REPLY -> "Generate smart responses"
        PromptTemplate.SUMMARIZE -> "Create a brief summary"
        PromptTemplate.EXPAND -> "Add more details"
        PromptTemplate.TRANSLATE_TO_HINDI -> "Convert to Hindi"
        PromptTemplate.TRANSLATE_TO_ENGLISH -> "Convert to English"
        PromptTemplate.EXPLAIN_LIKE_IM_5 -> "Simplify for easy understanding"
        PromptTemplate.MAKE_BULLET_POINTS -> "Convert to bullet format"
    }
}
