package com.aigateway.sms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Главный экран приложения для настройки API ключа, белого списка и проверки разрешений
 */
class MainActivity : AppCompatActivity() {

    private lateinit var apiKeyInput: EditText
    private lateinit var whitelistInput: EditText
    private lateinit var saveButton: Button
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiKeyInput = findViewById(R.id.apiKeyInput)
        whitelistInput = findViewById(R.id.whitelistInput)
        saveButton = findViewById(R.id.saveButton)
        statusText = findViewById(R.id.statusText)

        // Загружаем сохранённые настройки
        apiKeyInput.setText(Config.getApiKey(this))
        
        val whitelist = Config.getWhitelist(this)
        whitelistInput.setText(whitelist.joinToString("\n"))

        saveButton.setOnClickListener {
            val newKey = apiKeyInput.text.toString().trim()
            val newWhitelist = whitelistInput.text.toString().trim()

            if (newKey.isEmpty()) {
                Toast.makeText(this, "Введите API ключ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Config.saveApiKey(this, newKey)
            Config.saveWhitelist(this, newWhitelist)
            
            Toast.makeText(this, "✅ Настройки сохранены", Toast.LENGTH_SHORT).show()
            updateStatus()
        }

        // Запрашиваем разрешения
        requestPermissions()
        updateStatus()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), REQUEST_CODE)
        }
    }

    private fun updateStatus() {
        val hasApiKey = Config.getApiKey(this).isNotEmpty()
        val whitelist = Config.getWhitelist(this)
        val hasSmsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val status = buildString {
            appendLine("📊 Статус:")
            appendLine("")
            appendLine(if (hasApiKey) "✅ API ключ настроен" else "❌ API ключ не настроен")
            appendLine(if (hasSmsPermission) "✅ Разрешения SMS получены" else "❌ Требуются разрешения SMS")
            appendLine("")
            
            // Белый список
            appendLine("👥 Белый список:")
            if (whitelist.isEmpty()) {
                appendLine("⚠️  Пустой - отвечает ВСЕМ (небезопасно!)")
                appendLine("   Добавьте номера доверенных людей")
            } else {
                appendLine("✅ ${whitelist.size} номер(ов):")
                whitelist.take(5).forEach { phone ->
                    appendLine("   • $phone")
                }
                if (whitelist.size > 5) {
                    appendLine("   ... и ещё ${whitelist.size - 5}")
                }
            }
            appendLine("")
            
            if (hasApiKey && hasSmsPermission) {
                appendLine("🚀 Готово к работе!")
                appendLine("")
                if (whitelist.isEmpty()) {
                    appendLine("⚠️  Рекомендация: добавьте номера")
                    appendLine("   в белый список для безопасности")
                } else {
                    appendLine("Только номера из белого списка")
                    appendLine("смогут получать ответы AI.")
                }
            } else {
                appendLine("⚠️ Завершите настройку")
            }
        }

        statusText.text = status
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            updateStatus()
        }
    }

    companion object {
        private const val REQUEST_CODE = 100
    }
}
