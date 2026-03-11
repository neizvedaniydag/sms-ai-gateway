package com.aigateway.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

/**
 * BroadcastReceiver для приёма входящих SMS
 * Автоматически запускается при получении SMS (если есть разрешение RECEIVE_SMS)
 * БЕЗОПАСНОСТЬ: Обрабатывает только SMS от номеров из белого списка
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        // Извлекаем SMS из intent
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isEmpty()) {
            Log.w(TAG, "No SMS messages found in intent")
            return
        }

        // Объединяем все части SMS (если SMS состоит из нескольких частей)
        val fullMessage = messages.joinToString("") { it.messageBody ?: "" }
        val senderPhone = messages[0].originatingAddress ?: ""

        Log.i(TAG, "SMS received from: $senderPhone")
        Log.d(TAG, "Message: $fullMessage")

        // ⚠️ ПРОВЕРКА БЕЛОГО СПИСКА - ключевая защита от спама и посторонних
        if (!Config.isPhoneWhitelisted(context, senderPhone)) {
            Log.w(TAG, "❌ Phone $senderPhone NOT in whitelist - IGNORING")
            // Не отправляем ответ, не логируем детали - просто игнорируем
            return
        }

        Log.i(TAG, "✅ Phone $senderPhone IS whitelisted - processing request")

        // Запускаем ForegroundService для обработки SMS
        // Используем ForegroundService чтобы Android не убил процесс
        val serviceIntent = Intent(context, AiSmsService::class.java).apply {
            putExtra(EXTRA_QUESTION, fullMessage)
            putExtra(EXTRA_SENDER, senderPhone) // Гарантируем что ответ уйдёт именно этому номеру
        }

        try {
            context.startForegroundService(serviceIntent)
            Log.i(TAG, "AiSmsService started successfully for $senderPhone")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AiSmsService", e)
        }
    }

    companion object {
        private const val TAG = "SmsAiGateway"
        const val EXTRA_QUESTION = "question"
        const val EXTRA_SENDER = "sender"
    }
}
