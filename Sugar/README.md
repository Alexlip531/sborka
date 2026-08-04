# Сахар — Android-приложение для диабетиков 2 типа

Приложение «Сахар» помогает вести дневник измерений уровня глюкозы в крови и артериального давления, видеть календарь и статистику.

## Возможности

### Сахар
- Ввод уровня глюкозы в ммоль/л с указанием «до еды» или «после еды»
- Автоматическая оценка показателя (Низкий / Норма / Повышен / Высокий) с рекомендацией
- Целевые диапазоны для СД 2 типа:
  - до еды: 4.4–7.2 ммоль/л
  - после еды: до 10.0 ммоль/л

### Давление
- Ввод систолического, диастолического, пульса
- Выбор руки (левая/правая)
- Автоматическая классификация по ESH/ESC:
  - Пониженное (< 90/60)
  - Оптимальное (< 120/80)
  - Нормальное (120-129 / 80-84)
  - Высокое нормальное (130-139 / 85-89)
  - Гипертония 1 ст. (140-159 / 90-99)
  - Гипертония 2 ст. (160-179 / 100-109)
  - Гипертония 3 ст. (≥ 180 / ≥ 110)
- Текстовая рекомендация в зависимости от показателей

### Календарь
- Месячная сетка с навигацией вперёд/назад
- Точки под днями с записями
- При тапе на день — список всех замеров этого дня

### Статистика
- Количество измерений сахара и давления
- Средние значения (до/после еды, систолическое/диастолическое/пульс)
- Распределение по категориям с цветной полосой

### Общее
- Все данные хранятся локально (Room database)
- Дружелюбный Material 3 дизайн
- Тёмная тема по системной настройке
- Адаптивная вёрстка

## Технические характеристики

- **Минимальный Android**: 7.0 (API 24)
- **Целевой Android**: 14 (API 34)
- **Язык**: Kotlin
- **Библиотеки**: AndroidX, Material 3, Room, Coroutines

## Сборка APK

### Способ 0. Скачать готовый APK с GitHub Actions (САМЫЙ ПРОСТОЙ — без установки чего-либо)

1. Откройте репозиторий: https://github.com/Alexlip531/sborka/actions
2. Нажмите на последний успешный запуск **"Build APK"** (зелёная галочка)
3. В самом низу страницы прокрутите до раздела **"Artifacts"**
4. Скачайте файл **`sugar-debug-apk`** (это ZIP-архив с APK внутри)
5. Распакуйте ZIP — внутри будет `app-debug.apk`
6. Установите APK на телефон (см. раздел «Установка APK на телефон»)

> Если на странице Actions нет ни одного запуска — откройте вкладку **Actions** → слева выберите **"Build APK"** → справа нажмите **"Run workflow"** → **"Run workflow"**. Через 5–10 минут появится готовый APK.

> ⚠️ **Важно**: каждый раз когда я делаю правки и пушу в `main`, на GitHub Actions автоматически собирается новый APK. Чтобы получить последнюю версию — всегда качайте артефакт из **самого верхнего** запуска.

### Способ 1. Через Android Studio (рекомендуется, если хотите собирать локально)

1. Установите **Android Studio Hedgehog (2023.1.1)** или новее: https://developer.android.com/studio
2. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/Alexlip531/sborka.git
   ```
3. В Android Studio: **File → Open → выберите папку `Sugar/`**
4. Дождитесь окончания **Gradle Sync** (внизу появится индикатор)
5. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
6. APK появится по пути:
   ```
   Sugar/app/build/outputs/apk/debug/app-debug.apk
   ```
7. Нажмите **"locate"** в уведомлении, чтобы открыть папку в файловом менеджере


### Способ 2. Через командную строку

**Требования:**
- JDK 17 (например, Eclipse Temurin 17)
- Android SDK с `platforms;android-34` и `build-tools;34.0.0`

**Установка Android SDK через `sdkmanager`:**
```bash
# Скачать cmdline-tools: https://developer.android.com/studio#command-line-tools-only
sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
```

**Сборка:**
```bash
cd Sugar

# Linux/Mac
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk
./gradlew :app:assembleDebug

# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:ANDROID_HOME = "C:\Users\YOUR_NAME\AppData\Local\Android\Sdk"
.\gradlew.bat :app:assembleDebug
```

**APK будет по пути:**
```
Sugar/app/build/outputs/apk/debug/app-debug.apk
```

**Сборка release APK с подписью:**
```bash
# Сгенерировать keystore (один раз)
keytool -genkey -v -keystore release.keystore -alias sugar \
  -keyalg RSA -keysize 2048 -validity 10000

# Добавить в app/build.gradle.kts секцию signingConfigs
# Затем:
./gradlew :app:assembleRelease
```

## Установка APK на телефон

1. Скопируйте `app-debug.apk` на телефон (через USB, облако или мессенджер)
2. В файловом менеджере тапните по файлу `.apk`
3. При первом запуске разрешите установку из неизвестных источников для этого файлового менеджера
4. Нажмите **«Установить»**
5. Готово — приложение «Сахар» появится в списке приложений

## Структура проекта

```
Sugar/
├── app/
│   ├── build.gradle.kts              — конфигурация модуля
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/zai/sugar/
│       │   ├── data/
│       │   │   ├── entity/           — SugarMeasurement, PressureMeasurement
│       │   │   ├── dao/              — SugarDao, PressureDao
│       │   │   └── repository/       — AppDatabase, Repository
│       │   ├── medical/
│       │   │   ├── SugarEvaluator.kt      — оценка сахара
│       │   │   └── PressureEvaluator.kt   — оценка давления
│       │   ├── ui/
│       │   │   ├── main/MainActivity.kt   — BottomNav хост
│       │   │   ├── sugar/                  — фрагмент и адаптер
│       │   │   ├── pressure/               — фрагмент и адаптер
│       │   │   ├── calendar/               — фрагмент и адаптер сетки
│       │   │   ├── stats/                  — фрагмент статистики
│       │   │   └── dialogs/                — BottomSheet диалоги добавления
│       │   └── util/DateUtils.kt
│       └── res/                            — layout, values, drawables
├── build.gradle.kts                        — конфигурация проекта
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/                         — Gradle wrapper 8.5
├── gradlew, gradlew.bat                    — скрипты wrapper'а
└── README.md
```

## Troubleshooting

**«Failed to sync Gradle»** — проверьте что установлен JDK 17 (не 8, не 21).
```bash
java -version
# должно быть: openjdk version "17.x.x"
```

**«SDK location not found»** — создайте файл `Sugar/local.properties` со строкой:
```
sdk.dir=/path/to/your/Android/Sdk
```

**«Could not resolve com.android.tools.build:gradle:8.2.0»** — проверьте интернет. Gradle качает зависимости с `dl.google.com` и `repo.maven.apache.org`.

**Сборка виснет / не хватает памяти** — добавьте в `gradle.properties`:
```
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
```

**Компилятор KSP не найден** — проверьте что в `app/build.gradle.kts` указан плагин `com.google.devtools.ksp` версии `1.9.20-1.0.14` (соответствует Kotlin 1.9.20).

## Важно

Приложение не заменяет консультации врача. Все рекомендации носят информационный характер. При ухудшении самочувствия обязательно обратитесь к специалисту.

## Лицензия

MIT
