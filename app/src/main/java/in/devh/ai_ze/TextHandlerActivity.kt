package `in`.devh.ai_ze

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextProcessingScreen(
    selectedText: String,
    onFinish: () -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val apiKeyManager = remember { ApiKeyManager.getInstance(context) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // Empty transparent background
    Box(modifier = Modifier.fillMaxSize()) {
        // Bottom Sheet for Action Selection and Processing
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = onFinish,
                sheetState = bottomSheetState
            ) {
                TextProcessingBottomSheet(
                    selectedText = selectedText,
                    apiKeyManager = apiKeyManager,
                    onDismiss = onFinish
                )
            }
        }
    }
}
