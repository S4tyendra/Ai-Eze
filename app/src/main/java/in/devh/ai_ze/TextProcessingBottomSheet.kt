package `in`.devh.ai_ze

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TextProcessingBottomSheet(
    selectedText: String,
    onActionSelected: (PromptTemplate) -> Unit,
    onDismiss: () -> Unit
) {
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

        Text(
            text = "Choose AI Action",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Selected: \"${selectedText.take(50)}${if (selectedText.length > 50) "..." else ""}\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Common Actions
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(PromptTemplate.getCommonActions()) { template ->
                ActionButton(
                    template = template,
                    icon = getIconForTemplate(template),
                    onClick = { onActionSelected(template) }
                )
            }

            // Tone Modifications
            item {
                Text(
                    text = "Change Tone",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(PromptTemplate.getToneActions()) { template ->
                ActionButton(
                    template = template,
                    icon = getIconForTemplate(template),
                    onClick = { onActionSelected(template) }
                )
            }

            // Formatting Actions
            item {
                Text(
                    text = "Format Text",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(PromptTemplate.getFormattingActions()) { template ->
                ActionButton(
                    template = template,
                    icon = getIconForTemplate(template),
                    onClick = { onActionSelected(template) }
                )
            }

            // Translation Actions
            item {
                Text(
                    text = "Translation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(PromptTemplate.getTranslationActions()) { template ->
                ActionButton(
                    template = template,
                    icon = getIconForTemplate(template),
                    onClick = { onActionSelected(template) }
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
private fun ActionButton(
    template: PromptTemplate,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
