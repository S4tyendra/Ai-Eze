package `in`.devh.ai_ze

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiKeyManager private constructor(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "ai_ze_secure_prefs"
        private const val GEMINI_API_KEY = "gemini_api_key"

        @Volatile
        private var INSTANCE: ApiKeyManager? = null

        fun getInstance(context: Context): ApiKeyManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiKeyManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun saveGeminiApiKey(apiKey: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                encryptedPrefs.edit()
                    .putString(GEMINI_API_KEY, apiKey)
                    .apply()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun getGeminiApiKey(): String? {
        return withContext(Dispatchers.IO) {
            try {
                encryptedPrefs.getString(GEMINI_API_KEY, null)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun hasGeminiApiKey(): Boolean {
        return getGeminiApiKey()?.isNotEmpty() == true
    }

    suspend fun deleteGeminiApiKey(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                encryptedPrefs.edit()
                    .remove(GEMINI_API_KEY)
                    .apply()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
