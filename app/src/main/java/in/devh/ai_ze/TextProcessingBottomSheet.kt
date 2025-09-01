package `in`.devh.ai_ze

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
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

    val context = LocalContext.current
    val categories = remember { getActionCategories() }

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

        when (currentState) {
            BottomSheetState.ACTION_SELECTION -> {
                ActionSelectionContent(
                    selectedText = selectedText,
                    selectedCategory = selectedCategory,
                    categories = categories,
                    onCategorySelected = { selectedCategory = it },
                    onBackPressed = { selectedCategory = null },
                    onActionSelected = { template ->
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
                ProcessingContent(
                    selectedText = selectedText,
                    processedText = processedText
                )
            }

            BottomSheetState.RESULT -> {
                ResultContent(
                    selectedText = selectedText,
                    processedText = processedText,
                    errorMessage = errorMessage,
                    onCopyResult = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("AI Result", processedText)
                        clipboard.setPrimaryClip(clip)
                    },
                    onReplaceAndFinish = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("AI Result", processedText)
                        clipboard.setPrimaryClip(clip)
                        onDismiss()
                    },
                    onTryAgain = {
                        currentState = BottomSheetState.ACTION_SELECTION
                        selectedCategory = null
                        processedText = ""
                        errorMessage = null
                    }
                )
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
    // Header with back button for subcategories
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedCategory != null) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }

        Text(
            text = selectedCategory?.name ?: "Choose AI Action",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }

    if (selectedCategory == null) {
        Text(
            text = "Selected: \"${selectedText.take(50)}${if (selectedText.length > 50) "..." else ""}\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }

    // Main content
    if (selectedCategory == null) {
        // Show main categories
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            itemsIndexed(categories) { index, category ->
                CategoryButton(
                    category = category,
                    onClick = { onCategorySelected(category) },
                    index = index,
                    listSize = categories.size
                )
            }
        }

    } else {
        // Show actions for selected category
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(selectedCategory.templates) { index, template ->
                ActionButton(
                    template = template,
                    icon = getIconForTemplate(template),
                    onClick = { onActionSelected(template) },
                    index = index,
                    listSize = selectedCategory.templates.size
                )
            }

            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProcessingContent(
    selectedText: String,
    processedText: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Processing...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Original Text Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Original Text",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = selectedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Processing Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "AI Result",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }

                Text(
                    text = if (processedText.isEmpty()) "Processing..." else processedText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp, max = 200.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ResultContent(
    selectedText: String,
    processedText: String,
    errorMessage: String?,
    onCopyResult: () -> Unit,
    onReplaceAndFinish: () -> Unit,
    onTryAgain: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (errorMessage != null) "Error" else "Result",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Original Text Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Original Text",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = selectedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (errorMessage != null)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (errorMessage != null) "Error" else "AI Result",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = errorMessage ?: processedText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp, max = 200.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        if (errorMessage != null) {
            Button(
                onClick = onTryAgain,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Try Again",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        } else if (processedText.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCopyResult,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy")
                }

                Button(
                    onClick = onReplaceAndFinish,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = "Replace",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Replace")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
