package com.aigateway.sms

import android.content.Context

/**
 * Конфигурация приложения - хранение API ключа и белого списка
 */
object Config {

    private const val PREFS_NAME = "sms_ai_gateway_prefs"
    private const val KEY_API_KEY = "perplexity_api_key"
    private const val KEY_WHITELIST = "phone_whitelist"

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

    /**
     * Сохраняет белый список номеров телефонов
     * @param phones Список номеров (каждый номер на новой строке)
     */
    fun saveWhitelist(context: Context, phones: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_WHITELIST, phones).apply()
    }

    /**
     * Получает белый список номеров
     */
    fun getWhitelist(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_WHITELIST, "") ?: ""
        return raw.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * Проверяет, находится ли номер в белом списке
     * Учитывает различные форматы: +7, 8, 7, с пробелами и дефисами
     */
    fun isPhoneWhitelisted(context: Context, phone: String): Boolean {
        val whitelist = getWhitelist(context)
        
        // Если белый список пуст - разрешаем всех (режим без фильтрации)
        if (whitelist.isEmpty()) {
            return true
        }

        // Нормализуем входящий номер (убираем всё кроме цифр)
        val normalizedPhone = normalizePhone(phone)

        // Проверяем совпадение с каждым номером из белого списка
        return whitelist.any { whitelistedPhone ->
            val normalized = normalizePhone(whitelistedPhone)
            // Сравниваем последние 10 цифр (без кода страны)
            normalizedPhone.takeLast(10) == normalized.takeLast(10)
        }
    }

    /**
     * Нормализует номер телефона - оставляет только цифры
     * +7 (999) 123-45-67 -> 79991234567
     */
    private fun normalizePhone(phone: String): String {
        return phone.replace("[^0-9]".toRegex(), "")
    }
}
