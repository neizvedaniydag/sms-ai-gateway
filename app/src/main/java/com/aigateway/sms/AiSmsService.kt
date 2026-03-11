package com.aigateway.sms

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * ForegroundService для обработки SMS через Perplexity API
 * Работает в фоне и не убивается Android благодаря Foreground режиму
 */
class AiSmsService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = OkHttpClient()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Создаём уведомление чтобы сервис работал в Foreground режиме
        createNotificationChannel()
        val notification = buildNotification("Обработка запроса...")
        startForeground(NOTIFICATION_ID, notification)

        val question = intent?.getStringExtra(SmsReceiver.EXTRA_QUESTION) ?: ""
        val senderPhone = intent?.getStringExtra(SmsReceiver.EXTRA_SENDER) ?: ""

        if (question.isEmpty() || senderPhone.isEmpty()) {
            Log.w(TAG, "Empty question or sender, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        Log.i(TAG, "Processing question from $senderPhone: $question")

        // Запускаем корутину для асинхронной обработки
        serviceScope.launch {
            try {
                // 1. Отправляем запрос в Perplexity
                val answer = askPerplexity(question)
                
                // 2. Обновляем уведомление
                val successNotification = buildNotification("Ответ отправлен")
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, successNotification)
                
                // 3. Отправляем SMS с ответом
                SmsSender.sendSms(senderPhone, answer)
                
                Log.i(TAG, "Answer sent successfully to $senderPhone")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing SMS", e)
                
                // Отправляем SMS с ошибкой
                val errorMsg = "Ошибка: ${e.message ?: "Неизвестная ошибка"}"
                SmsSender.sendSms(senderPhone, errorMsg)
            } finally {
                // Останавливаем сервис через 2 секунды
                delay(2000)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    /**
     * Отправляет запрос в Perplexity Sonar API
     * @param question Вопрос пользователя
     * @return Ответ от AI с источниками
     */
    private suspend fun askPerplexity(question: String): String = withContext(Dispatchers.IO) {
        val apiKey = Config.getApiKey(this@AiSmsService)
        if (apiKey.isEmpty()) {
            throw IllegalStateException("Perplexity API ключ не настроен")
        }

        val requestBody = JSONObject().apply {
            put("model", "sonar-pro") // Модель с веб-поиском + академические источники
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", question)
                })
            })
            put("max_tokens", 300) // Ограничиваем чтобы уместить в SMS
            put("temperature", 0.2) // Более точные ответы
        }

        val request = Request.Builder()
            .url("https://api.perplexity.ai/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        Log.d(TAG, "Sending request to Perplexity API")

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            Log.e(TAG, "API error: ${response.code} - $responseBody")
            throw Exception("API error: ${response.code}")
        }

        Log.d(TAG, "Response received from Perplexity")

        // Парсим JSON ответ
        val jsonResponse = JSONObject(responseBody)
        val choices = jsonResponse.getJSONArray("choices")
        if (choices.length() == 0) {
            throw Exception("No response from AI")
        }

        val answer = choices.getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        // Обрезаем ответ до 155 символов (лимит SMS с запасом)
        return@withContext if (answer.length > 155) {
            answer.substring(0, 152) + "..."
        } else {
            answer
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SMS AI Gateway",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Обработка SMS через AI"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("SMS AI Gateway")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.i(TAG, "Service destroyed")
    }

    companion object {
        private const val TAG = "SmsAiGateway"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "sms_ai_gateway"
    }
}
