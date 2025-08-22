package `in`.devh.ai_ze

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.material.icons.automirrored.filled.Send

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextProcessingBottomSheet(
    selectedText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with selected text preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Selected Text",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedText,
                        fontSize = 16.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // AI Action Tiles
            Text(
                text = "Choose AI Action",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionTile(
                        icon = Icons.Default.Edit,
                        title = "Rephrase",
                        description = "Rewrite with different words",
                        onClick = {
                            handleAction(context, "Rephrase", selectedText)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ActionTile(
                        icon = Icons.Default.Settings,
                        title = "Modify Tone",
                        description = "Change writing style",
                        onClick = {
                            handleAction(context, "Modify tone", selectedText)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionTile(
                        icon = Icons.Default.Check,
                        title = "Fix Grammar",
                        description = "Correct errors",
                        onClick = {
                            handleAction(context, "Fix grammar", selectedText)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ActionTile(
                        icon = Icons.AutoMirrored.Filled.Send,
                        title = "Suggest Reply",
                        description = "Generate response",
                        onClick = {
                            handleAction(context, "Suggest reply", selectedText)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Bottom spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionTile(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun handleAction(context: android.content.Context, action: String, text: String) {
    Toast.makeText(context, "$action: \"${text.take(30)}...\"", Toast.LENGTH_SHORT).show()
    // TODO: Implement actual AI processing here
}
