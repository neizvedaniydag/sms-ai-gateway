package com.aigateway.sms

import android.telephony.SmsManager
import android.util.Log

/**
 * Утилита для отправки SMS
 */
object SmsSender {

    private const val TAG = "SmsAiGateway"
    private const val MAX_SMS_LENGTH = 160

    /**
     * Отправляет SMS на указанный номер
     * @param phoneNumber Номер телефона получателя
     * @param message Текст сообщения
     */
    fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            
            // Если сообщение больше 160 символов, разбиваем на части
            if (message.length > MAX_SMS_LENGTH) {
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    null,
                    null
                )
                Log.i(TAG, "Sent multipart SMS (${parts.size} parts) to $phoneNumber")
            } else {
                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
                )
                Log.i(TAG, "Sent SMS to $phoneNumber")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $phoneNumber", e)
            throw e
        }
    }
}
