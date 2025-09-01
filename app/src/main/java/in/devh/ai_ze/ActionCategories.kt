package `in`.devh.ai_ze

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector

data class ActionCategory(
    val name: String,
    val icon: ImageVector,
    val templates: List<PromptTemplate>
)

fun getActionCategories(): List<ActionCategory> {
    return listOf(
        ActionCategory(
            "Quick Actions",
            Icons.Default.Refresh,
            PromptTemplate.getCommonActions()
        ),
        ActionCategory(
            "Change tone",
            Icons.Default.SentimentSatisfied,
            PromptTemplate.getToneActions()
        ),
        ActionCategory(
            "Format text",
            Icons.AutoMirrored.Filled.FormatListBulleted,
            PromptTemplate.getFormattingActions()
        ),
        ActionCategory(
            "Translation",
            Icons.Default.Translate,
            PromptTemplate.getTranslationActions()
        )
    )
}
