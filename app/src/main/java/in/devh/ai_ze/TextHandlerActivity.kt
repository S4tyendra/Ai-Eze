package `in`.devh.ai_ze

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import `in`.devh.ai_ze.ui.theme.AiZeTheme

class TextHandlerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if this is a duplicate launch
        if (isTaskRoot.not() && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
            finish()
            return
        }

        val selectedText = intent.getStringExtra("selectedText")
            ?: intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            ?: ""

        if (selectedText.isEmpty()) {
            finish()
            return
        }

        setContent {
            AiZeTheme {
                TextProcessingScreen(
                    selectedText = selectedText,
                    onFinish = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle new intent to prevent duplicate activities
        setIntent(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TextProcessingScreen(
    selectedText: String,
    onFinish: () -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var processedText by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val apiKeyManager = remember { ApiKeyManager.getInstance(context) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

//    LaunchedEffect(showBottomSheet) {
//        if (!showBottomSheet) {
//            onFinish()
//        }
//    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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

            // Animated Processing/Result Section
            AnimatedVisibility(
                visible = isProcessing || showResult,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
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

                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }

                        // Animated Text Content
                        AnimatedContent(
                            targetState = processedText,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(150)) with
                                fadeOut(animationSpec = tween(150))
                            },
                            label = "text_animation"
                        ) { text ->
                            Text(
                                text = if (text.isEmpty() && isProcessing) "Processing..." else text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            )
                        }

                        // Action Buttons
                        if (showResult && processedText.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("AI Result", processedText)
                                        clipboard.setPrimaryClip(clip)
                                    },
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
                                    onClick = {
                                        // TODO: Implement replace functionality
                                        // This would require system-level text replacement
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("AI Result", processedText)
                                        clipboard.setPrimaryClip(clip)
                                        onFinish()
                                    },
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
                    }
                }
            }

            // Error Message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Bottom Sheet for Action Selection
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState
            ) {
                TextProcessingBottomSheet(
                    selectedText = selectedText,
                    onActionSelected = { template ->
                        showBottomSheet = false
                        handleAction(
                            template = template,
                            selectedText = selectedText,
                            apiKeyManager = apiKeyManager,
                            context = context,
                            onProcessingStart = {
                                isProcessing = true
                                showResult = true
                                errorMessage = null
                                processedText = ""
                            },
                            onTextUpdate = { text ->
                                processedText = text
                            },
                            onProcessingComplete = {
                                isProcessing = false
                            },
                            onError = { error ->
                                isProcessing = false
                                errorMessage = error
                            }
                        )
                    },
                    onDismiss = { showBottomSheet = false }
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
    onProcessingStart: () -> Unit,
    onTextUpdate: (String) -> Unit,
    onProcessingComplete: () -> Unit,
    onError: (String) -> Unit
) {
    val activity = context as ComponentActivity

    activity.lifecycleScope.launch {
        onProcessingStart()

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

            onProcessingComplete()
            geminiClient.close()

        } catch (e: Exception) {
            onError("Error: ${e.message}")
        }
    }
}
