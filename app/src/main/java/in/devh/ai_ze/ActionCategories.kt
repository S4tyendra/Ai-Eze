package `in`.devh.ai_ze

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatTextdirectionLToR
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class ActionCategory(
    val name: String,
    val icon: ImageVector,
    val templates: List<PromptTemplate>
)

fun getActionCategories(): List<ActionCategory> {
    return listOf(
        ActionCategory(
            name = "Common Actions",
            icon = Icons.Default.AutoFixHigh,
            templates = PromptTemplate.getCommonActions()
        ),
        ActionCategory(
            name = "Change Tone",
            icon = Icons.Default.RecordVoiceOver,
            templates = PromptTemplate.getToneActions()
        ),
        ActionCategory(
            name = "Format",
            icon = Icons.AutoMirrored.Filled.FormatTextdirectionLToR,
            templates = PromptTemplate.getFormattingActions()
        ),
        ActionCategory(
            name = "Translate",
            icon = Icons.Default.Translate,
            templates = PromptTemplate.getTranslationActions()
        )
    )
}
