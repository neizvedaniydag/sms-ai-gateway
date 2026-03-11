# SMS AI Gateway 🚀

Android-приложение для автоматической пересылки SMS в Perplexity AI с веб-поиском и академическими источниками. Ответ отправляется обратно SMS.

## Как это работает

```
Входящая SMS → Perplexity Sonar API (поиск в веб + Scholar) → Автоматический ответ SMS
```

## Возможности

- ✅ **Автоматическая обработка SMS** в фоновом режиме
- ✅ **Perplexity Sonar Pro** - поиск в веб + академических источниках
- ✅ **Надёжный ForegroundService** - не убивается Android
- ✅ **Логирование запросов** для отладки
- ✅ **Простая настройка** API ключа

## Быстрый старт

### 1. Получите API ключ Perplexity

1. Зарегистрируйтесь на [perplexity.ai/settings/api](https://www.perplexity.ai/settings/api)
2. Скопируйте API ключ
3. Первые $5 бесплатно

### 2. Сборка приложения

```bash
git clone https://github.com/neizvedaniydag/sms-ai-gateway.git
cd sms-ai-gateway
```

Откройте проект в **Android Studio**:

1. File → Open → выберите папку `sms-ai-gateway`
2. Build → Build Bundle(s) / APK(s) → Build APK(s)
3. APK сохранится в `app/build/outputs/apk/debug/app-debug.apk`

### 3. Установка и настройка

1. Установите APK на **второй телефон** (который будет работать шлюзом)
2. При первом запуске введите **Perplexity API ключ**
3. Разрешите доступ к SMS и уведомлениям
4. Оставьте телефон подключённым к Wi-Fi

### 4. Использование

Просто отправьте SMS на номер второго телефона с вопросом. Приложение:

1. Получит SMS
2. Отправит вопрос в Perplexity (с веб-поиском)
3. Автоматически ответит SMS с результатом

## Настройки

### Файл конфигурации

API ключ хранится в SharedPreferences. Изменить можно:

- В самом приложении (главный экран)
- Или в коде: `app/src/main/java/com/aigateway/sms/Config.kt`

### Модели Perplexity

| Модель | Описание | Цена |
|--------|----------|------|
| `sonar-pro` | Веб + академический поиск | $1/1M токенов |
| `sonar` | Только веб-поиск | $0.2/1M токенов |

Изменить модель: `AiSmsService.kt` строка 45

## Структура проекта

```
app/src/main/java/com/aigateway/sms/
├── SmsReceiver.kt          # BroadcastReceiver для приёма SMS
├── AiSmsService.kt         # ForegroundService для обработки
├── MainActivity.kt         # Главный экран настроек
├── SmsSender.kt            # Отправка SMS
└── Config.kt               # Конфигурация
```

## Требования

- Android 8.0+ (API 26+)
- Разрешения: RECEIVE_SMS, SEND_SMS, INTERNET, FOREGROUND_SERVICE
- Wi-Fi или мобильный интернет
- Perplexity API ключ

## Отладка

Логи сохраняются в logcat с тегом `SmsAiGateway`:

```bash
adb logcat | grep SmsAiGateway
```

## Важные замечания

⚠️ **Отключите режим энергосбережения** для приложения:
- Настройки → Приложения → SMS AI Gateway → Батарея → Без ограничений

⚠️ **Убедитесь что телефон не засыпает:**
- Настройки → Экран → Тайм-аут → Никогда (пока идёт контрольная)

## Лицензия

MIT License - используйте свободно

## Поддержка

Проблемы? [Создайте Issue](https://github.com/neizvedaniydag/sms-ai-gateway/issues)
