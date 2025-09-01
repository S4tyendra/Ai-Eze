package `in`.devh.ai_ze

enum class PromptTemplate(val displayName: String, val template: String) {
    REPHRASE(
        "Rephrase",
        "Please rephrase the following text while maintaining the same meaning but using different words and sentence structure:\n\n\"{text}\"\n\nProvide only the rephrased text without any additional explanation."
    ),
    FIX_GRAMMAR(
        "Fix Grammar",
        "Please correct any grammar, spelling, and punctuation errors in the following text:\n\n\"{text}\"\n\nProvide only the corrected text without any additional explanation."
    ),
    MODIFY_TONE_FORMAL(
        "Make Formal",
        "Please rewrite the following text in a formal, professional tone:\n\n\"{text}\"\n\nProvide only the rewritten text without any additional explanation."
    ),
    MODIFY_TONE_CASUAL(
        "Make Casual",
        "Please rewrite the following text in a casual, friendly tone:\n\n\"{text}\"\n\nProvide only the rewritten text without any additional explanation."
    ),
    MODIFY_TONE_POLITE(
        "Make Polite",
        "Please rewrite the following text in a more polite and respectful tone:\n\n\"{text}\"\n\nProvide only the rewritten text without any additional explanation."
    ),
    SUGGEST_REPLY(
        "Suggest Reply",
        "Based on the following message, suggest 3 appropriate reply options:\n\n\"{text}\"\n\nProvide 3 numbered reply suggestions that are contextually appropriate and varied in tone (formal, casual, enthusiastic)."
    ),
    SUMMARIZE(
        "Summarize",
        "Please provide a concise summary of the following text:\n\n\"{text}\"\n\nProvide only the summary without any additional explanation."
    ),
    EXPAND(
        "Expand",
        "Please expand and elaborate on the following text with more details and examples:\n\n\"{text}\"\n\nProvide only the expanded text without any additional explanation."
    ),
    TRANSLATE_TO_HINDI(
        "Translate to Hindi",
        "Please translate the following text to Hindi:\n\n\"{text}\"\n\nProvide only the translated text without any additional explanation."
    ),
    TRANSLATE_TO_ENGLISH(
        "Translate to English",
        "Please translate the following text to English:\n\n\"{text}\"\n\nProvide only the translated text without any additional explanation."
    ),
    EXPLAIN_LIKE_IM_5(
        "Explain Like I'm 5",
        "Please explain the following text in very simple terms that a 5-year-old could understand:\n\n\"{text}\"\n\nUse simple words and concepts."
    ),
    MAKE_BULLET_POINTS(
        "Make Bullet Points",
        "Please convert the following text into clear bullet points:\n\n\"{text}\"\n\nProvide only the bullet points without any additional explanation."
    );

    fun getPrompt(text: String): String {
        return template.replace("{text}", text)
    }

    companion object {
        fun getCommonActions(): List<PromptTemplate> {
            return listOf(REPHRASE, FIX_GRAMMAR, SUMMARIZE, EXPAND)
        }

        fun getToneActions(): List<PromptTemplate> {
            return listOf(MODIFY_TONE_FORMAL, MODIFY_TONE_CASUAL, MODIFY_TONE_POLITE)
        }

        fun getFormattingActions(): List<PromptTemplate> {
            return listOf(MAKE_BULLET_POINTS, EXPLAIN_LIKE_IM_5)
        }

        fun getTranslationActions(): List<PromptTemplate> {
            return listOf(TRANSLATE_TO_HINDI, TRANSLATE_TO_ENGLISH)
        }
    }
}
