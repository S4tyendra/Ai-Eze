package `in`.devh.ai_ze

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import `in`.devh.ai_ze.ui.theme.AiZeTheme

class TextHandlerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get the selected text from PROCESS_TEXT intent
        val selectedText = when {
            intent.action == Intent.ACTION_PROCESS_TEXT -> {
                intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""
            }
            else -> intent.getStringExtra("selectedText") ?: ""
        }

        if (selectedText.isEmpty()) {
            finish()
            return
        }

        setContent {
            AiZeTheme {
                TextProcessingBottomSheet(
                    selectedText = selectedText,
                    onDismiss = { finish() }
                )
            }
        }
    }
}
