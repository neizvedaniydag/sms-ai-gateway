package com.aigateway.sms

import android.content.Context

/**
 * Конфигурация приложения - хранение API ключа
 */
object Config {

    private const val PREFS_NAME = "sms_ai_gateway_prefs"
    private const val KEY_API_KEY = "perplexity_api_key"

    /**
     * Сохраняет API ключ Perplexity
     */
    fun saveApiKey(context: Context, apiKey: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    /**
     * Получает сохранённый API ключ Perplexity
     */
    fun getApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }
}
