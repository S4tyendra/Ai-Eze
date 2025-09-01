package `in`.devh.ai_ze

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ApiKeySetupDialog(
    onApiKeySubmit: (String) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var isValidApiKey by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Setup Gemini API Key",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "To use AI features, please enter your Gemini API key. Your key will be stored securely on your device using encrypted storage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = {
                        apiKey = it
                        isValidApiKey = true
                    },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                imageVector = if (showApiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showApiKey) "Hide API key" else "Show API key"
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = !isValidApiKey,
                    supportingText = if (!isValidApiKey) {
                        { Text("Please enter a valid API key") }
                    } else null,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (apiKey.trim().isNotEmpty() && apiKey.trim().startsWith("AIza")) {
                                onApiKeySubmit(apiKey.trim())
                            } else {
                                isValidApiKey = false
                            }
                        },
                        enabled = !isLoading && apiKey.trim().isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }

                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                        context.startActivity(intent)
                    },
                    enabled = !isLoading
                ) {
                    Text("Get API Key from Google AI Studio")
                }
            }
        }
    }
}
