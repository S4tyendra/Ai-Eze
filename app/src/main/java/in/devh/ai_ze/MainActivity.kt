package `in`.devh.ai_ze

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.devh.ai_ze.ui.theme.AiZeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiZeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AiAssistantScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AiZe Text Assistant",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Your AI-powered text companion",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "How to Use",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "1. Select any text in any app",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "2. Look for 'AiZe' in the text selection menu",
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "3. Choose your AI action from the bottom sheet",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        // Demo the text handler with sample text
                        val demoIntent = Intent(context, TextHandlerActivity::class.java).apply {
                            putExtra("selectedText", "This is a sample text for testing the AI features.")
                        }
                        context.startActivity(demoIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try Demo")
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "AI Features",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                listOf(
                    "📝 Rephrase - Rewrite with different words",
                    "🎭 Modify Tone - Change writing style",
                    "✅ Fix Grammar - Correct errors",
                    "💬 Suggest Reply - Generate smart responses"
                ).forEach { feature ->
                    Text(
                        text = feature,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "💡 Tip",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Works system-wide! Select text in messaging apps, browsers, notes, and more to see AiZe in your text selection options.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}