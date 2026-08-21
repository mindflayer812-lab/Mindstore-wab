package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AppEntity::class,
        AppVersionEntity::class,
        AppScreenshotEntity::class,
        CategoryEntity::class,
        SiteSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun versionDao(): VersionDao
    abstract fun screenshotDao(): ScreenshotDao
    abstract fun categoryDao(): CategoryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mind_store_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }

            suspend fun populateDatabase(db: AppDatabase) {
                val appDao = db.appDao()
                val versionDao = db.versionDao()
                val screenshotDao = db.screenshotDao()
                val categoryDao = db.categoryDao()
                val settingsDao = db.settingsDao()

                // 1. Seed Site Settings
                val defaultSettings = listOf(
                    SiteSettingEntity("site_name", "Mind Store"),
                    SiteSettingEntity("site_tagline", "Official Android APK Publishing & Download Hub"),
                    SiteSettingEntity("hero_title", "Discover & Download Android Apps"),
                    SiteSettingEntity("hero_description", "Find useful Android applications, explore their features and download the latest versions from Mind Store."),
                    SiteSettingEntity("developer_name", "Ajay"),
                    SiteSettingEntity("developer_phone", "8595311812"),
                    SiteSettingEntity("developer_email", "mindflayer812@gmail.com"),
                    SiteSettingEntity("admin_pin", "admin123"),
                    SiteSettingEntity("downloads_enabled", "true"),
                    SiteSettingEntity("privacy_policy", """
                        # Privacy Policy for Mind Store
                        
                        **Effective Date:** August 2026
                        **Developer:** Ajay (Contact: mindflayer812@gmail.com, Phone: 8595311812)
                        
                        At Mind Store, we are committed to respecting and protecting user privacy. This Privacy Policy explains how our APK publishing platform handles data.
                        
                        ### 1. Information We Collect
                        - **Anonymous Download Metrics:** We log aggregate download counters to display app popularity statistics.
                        - **No Personal Tracking:** Mind Store does not collect intrusive personal trackers, IMEI, or sensitive data.
                        
                        ### 2. APK Safety & Integrity
                        Every APK published on Mind Store is scanned, verified, and distributed directly with cryptographic SHA-256 signatures to ensure authenticity.
                        
                        ### 3. Contact
                        For privacy inquiries, reach out directly to developer Ajay at mindflayer812@gmail.com.
                    """.trimIndent()),
                    SiteSettingEntity("terms_conditions", """
                        # Terms and Conditions
                        
                        Welcome to **Mind Store** (Operated by Ajay). By downloading APKs or using this service, you agree to these terms:
                        
                        1. **Fair Use:** All Android APK packages distributed on Mind Store are provided for personal, testing, and verified installation purposes.
                        2. **Modifications:** Reverse engineering or distributing malicious payloads is strictly prohibited.
                        3. **Disclaimer:** While all APKs are verified, users are advised to review app permissions before installation.
                    """.trimIndent()),
                    SiteSettingEntity("disclaimer", """
                        # Disclaimer
                        
                        Mind Store provides Android application binaries (.apk) directly uploaded and maintained by verified developers. APK installations require enabling 'Install Unknown Apps' on Android devices.
                    """.trimIndent())
                )
                settingsDao.setSettings(defaultSettings)

                // 2. Seed Categories
                val defaultCategories = listOf(
                    CategoryEntity(name = "AI Apps", slug = "ai-apps", iconName = "psychology", description = "Intelligent AI assistants, Gemini & neural engines", colorHex = 0xFF7C3AED, appCount = 2),
                    CategoryEntity(name = "Farming", slug = "farming", iconName = "agriculture", description = "Smart agriculture, crop analytics & weather sensors", colorHex = 0xFF10B981, appCount = 1),
                    CategoryEntity(name = "Education", slug = "education", iconName = "school", description = "Interactive courses, quizzes & study aids", colorHex = 0xFF3B82F6, appCount = 1),
                    CategoryEntity(name = "Tools", slug = "tools", iconName = "build", description = "Device utilities, file managers & diagnostics", colorHex = 0xFFF59E0B, appCount = 1),
                    CategoryEntity(name = "Productivity", slug = "productivity", iconName = "task_alt", description = "Task managers, note taking & sync tools", colorHex = 0xFF06B6D4, appCount = 1),
                    CategoryEntity(name = "Photography", slug = "photography", iconName = "photo_camera", description = "Camera filters, RAW editors & gallery", colorHex = 0xFFEC4899, appCount = 1),
                    CategoryEntity(name = "Entertainment", slug = "entertainment", iconName = "sports_esports", description = "Games, audio visualizers & media", colorHex = 0xFF8B5CF6, appCount = 1),
                    CategoryEntity(name = "Other", slug = "other", iconName = "category", description = "Miscellaneous custom utility packages", colorHex = 0xFF64748B, appCount = 1)
                )
                categoryDao.insertCategories(defaultCategories)

                // 3. Seed Sample Apps
                // App 1: Ajay Kishan AI (Primary Featured App by Ajay)
                val app1Id = appDao.insertApp(
                    AppEntity(
                        slug = "ajay-kishan-ai",
                        name = "Ajay Kishan AI",
                        tagline = "AI Smart Crop Doctor, Live APMC Mandi Rates & Farmer Assistant",
                        description = "Ajay Kishan AI is an all-in-one smart farming intelligence app created by Ajay. It features camera-based leaf disease diagnostics, daily live APMC mandi rates across all Indian states, 7-day hyper-local weather rain forecasts, automated fertilizer calculators, and 24/7 AI agricultural voice advisory in Hindi and regional languages.",
                        category = "Farming",
                        developer = "Ajay",
                        iconResName = "img_app_icon",
                        bannerResName = "img_hero_banner",
                        minAndroidReq = "Android 7.0 (Nougat) or higher",
                        rating = 4.9f,
                        ratingCount = 4850,
                        totalDownloads = 52000,
                        isFeatured = true,
                        isPublished = true,
                        downloadsEnabled = true,
                        features = "• Camera AI Leaf Disease Scanner & Instant Cure Guide\n• Live Mandi Bhav (APMC) for Wheat, Paddy, Mustard, Cotton & Veggies\n• Micro-Climate 7-Day Rainfall & Temperature Forecasts\n• Automated Fertilizer (NPK/DAP/Urea) & Pesticide Dosage Calculator\n• Direct 24/7 AI Voice Kisan Chatbot in Hindi\n• 100% Offline Mode support for remote rural farm areas",
                        privacyPolicy = "Ajay Kishan AI only uses device location to fetch local weather alerts and nearest APMC mandi market prices. No private personal data is collected or shared."
                    )
                )

                // App 1 Versions (Ajay Kishan AI APK Releases)
                versionDao.insertVersion(
                    AppVersionEntity(
                        appId = app1Id,
                        versionName = "v2.5.0",
                        versionCode = 25,
                        apkFileName = "Ajay_Kishan_AI_v2.5.0_release.apk",
                        fileSize = "22.4 MB",
                        fileSizeBytes = 23488100L,
                        changelog = "• Official public release of Ajay Kishan AI by Ajay\n• Instant camera-based leaf disease detection engine\n• Real-time APMC Mandi live price sync across 500+ markets\n• Enhanced offline database for zero-internet rural usage\n• Faster UI response and ultra-low battery consumption",
                        releaseNotes = "Flagship official APK release by Ajay.",
                        isPublished = true,
                        isArchived = false,
                        downloadCount = 38500,
                        releaseDate = System.currentTimeMillis()
                    )
                )
                versionDao.insertVersion(
                    AppVersionEntity(
                        appId = app1Id,
                        versionName = "v2.4.2",
                        versionCode = 24,
                        apkFileName = "Ajay_Kishan_AI_v2.4.2.apk",
                        fileSize = "21.8 MB",
                        fileSizeBytes = 22858800L,
                        changelog = "• Added Hindi and regional voice query support\n• Improved soil moisture calculation models\n• Bug fixes for Android 14 and foldable devices",
                        releaseNotes = "Previous stable version.",
                        isPublished = true,
                        isArchived = true,
                        downloadCount = 13500,
                        releaseDate = System.currentTimeMillis() - 86400000L * 15
                    )
                )

                // App 1 Screenshots
                screenshotDao.insertScreenshots(
                    listOf(
                        AppScreenshotEntity(appId = app1Id, title = "AI Crop Doctor", subtitle = "Scan leaf diseases with camera", iconName = "eco", accentColorHex = 0xFF10B981, sortOrder = 1),
                        AppScreenshotEntity(appId = app1Id, title = "Daily Mandi Rates", subtitle = "Live daily APMC crop prices", iconName = "storefront", accentColorHex = 0xFFF59E0B, sortOrder = 2),
                        AppScreenshotEntity(appId = app1Id, title = "Rain & Weather Alerts", subtitle = "7-day micro-local forecasts", iconName = "water_drop", accentColorHex = 0xFF06B6D4, sortOrder = 3),
                        AppScreenshotEntity(appId = app1Id, title = "Voice Kisan AI", subtitle = "Instant answers in Hindi", iconName = "mic", accentColorHex = 0xFF7C3AED, sortOrder = 4)
                    )
                )

                // App 2: AgriSmart Farming Hub
                val app2Id = appDao.insertApp(
                    AppEntity(
                        slug = "agrismart-farming-hub",
                        name = "AgriSmart Farming Hub",
                        tagline = "Intelligent soil, crop disease diagnostic & weather advisory system",
                        description = "AgriSmart Farming Hub empowers farmers and agriculture professionals with AI-based crop diagnosis, real-time localized weather alerts, mandi price comparisons, and soil fertility recommendations.",
                        category = "Farming",
                        developer = "Ajay",
                        iconResName = "img_app_icon",
                        bannerResName = "img_hero_banner",
                        minAndroidReq = "Android 8.0+",
                        rating = 4.8f,
                        ratingCount = 1420,
                        totalDownloads = 18900,
                        isFeatured = true,
                        isPublished = true,
                        downloadsEnabled = true,
                        features = "• Camera-based crop leaf disease detector\n• Live Mandi Bhav updates across 500+ APMC centers\n• Irrigation schedule advisor based on soil moisture sensors\n• Offline Hindi & regional language support\n• Expert fertilizer dosage calculator",
                        privacyPolicy = "Location is solely used for fetching local micro-climate forecast and nearest mandi prices."
                    )
                )
                versionDao.insertVersion(
                    AppVersionEntity(
                        appId = app2Id,
                        versionName = "v1.8.2",
                        versionCode = 18,
                        apkFileName = "AgriSmart_Farming_v1.8.2.apk",
                        fileSize = "16.2 MB",
                        fileSizeBytes = 16986900L,
                        changelog = "• Added 15 new crop disease detection models\n• Faster GPS location lock for weather\n• Offline mandi rates cache",
                        releaseNotes = "Monsoon season update.",
                        isPublished = true,
                        isArchived = false,
                        downloadCount = 12400,
                        releaseDate = System.currentTimeMillis() - 86400000L * 4
                    )
                )
                screenshotDao.insertScreenshots(
                    listOf(
                        AppScreenshotEntity(appId = app2Id, title = "Crop Disease Scanner", subtitle = "Point camera to diagnose leaf pests", iconName = "eco", accentColorHex = 0xFF10B981, sortOrder = 1),
                        AppScreenshotEntity(appId = app2Id, title = "Daily Mandi Rates", subtitle = "Live grain, cotton & pulse prices", iconName = "storefront", accentColorHex = 0xFFF59E0B, sortOrder = 2),
                        AppScreenshotEntity(appId = app2Id, title = "Smart Rain Alerts", subtitle = "Micro-local 7-day precipitation forecast", iconName = "water_drop", accentColorHex = 0xFF06B6D4, sortOrder = 3)
                    )
                )

                // App 3: EduLearn Pro
                val app3Id = appDao.insertApp(
                    AppEntity(
                        slug = "edulearn-pro-quiz",
                        name = "EduLearn Pro Study & Quiz",
                        tagline = "Interactive STEM flashcards, practice exams and smart notes",
                        description = "EduLearn Pro is a modern digital study companion with adaptive spaced-repetition flashcards, STEM formula sheets, mock test generators, and progress tracking designed for competitive exams.",
                        category = "Education",
                        developer = "Ajay",
                        iconResName = "img_app_icon",
                        bannerResName = "img_hero_banner",
                        minAndroidReq = "Android 8.0+",
                        rating = 4.7f,
                        ratingCount = 980,
                        totalDownloads = 12100,
                        isFeatured = false,
                        isPublished = true,
                        downloadsEnabled = true,
                        features = "• Spaced repetition memory algorithm\n• 50,000+ curated MCQ practice questions\n• Offline formula sheet with quick search\n• Dark study mode with Pomodoro timer",
                        privacyPolicy = "EduLearn Pro stores study progress locally on device."
                    )
                )
                versionDao.insertVersion(
                    AppVersionEntity(
                        appId = app3Id,
                        versionName = "v3.1.0",
                        versionCode = 31,
                        apkFileName = "EduLearn_Pro_v3.1.0.apk",
                        fileSize = "19.5 MB",
                        fileSizeBytes = 20447200L,
                        changelog = "• Added Physics & Chemistry 3D interactive diagrams\n• New timed mock exam interface\n• Export scorecards as PDF",
                        releaseNotes = "Academic year upgrade.",
                        isPublished = true,
                        isArchived = false,
                        downloadCount = 8900,
                        releaseDate = System.currentTimeMillis() - 86400000L * 7
                    )
                )
                screenshotDao.insertScreenshots(
                    listOf(
                        AppScreenshotEntity(appId = app3Id, title = "Smart Flashcards", subtitle = "Active recall spaced repetition", iconName = "style", accentColorHex = 0xFF3B82F6, sortOrder = 1),
                        AppScreenshotEntity(appId = app3Id, title = "Mock Exam Engine", subtitle = "Simulate real time exam pressure", iconName = "timer", accentColorHex = 0xFF8B5CF6, sortOrder = 2)
                    )
                )

                // App 4: ProTool Master Utility
                val app4Id = appDao.insertApp(
                    AppEntity(
                        slug = "protool-master-utility",
                        name = "ProTool Master Utility",
                        tagline = "All-in-one network analyzer, APK inspector, sensor tools & cleaner",
                        description = "ProTool Master provides 30+ precision diagnostics tools in a single lightweight APK. Inspect hardware sensors, run network ping/traceroute, analyze APK manifests, and clean cache safely.",
                        category = "Tools",
                        developer = "Ajay",
                        iconResName = "img_app_icon",
                        bannerResName = "img_hero_banner",
                        minAndroidReq = "Android 7.0+",
                        rating = 4.9f,
                        ratingCount = 3100,
                        totalDownloads = 41200,
                        isFeatured = true,
                        isPublished = true,
                        downloadsEnabled = true,
                        features = "• Wi-Fi channel analyzer & speed tester\n• APK extractor and manifest viewer\n• Battery health, temperature & cycle stats\n• Sensor calibration & compass diagnostics\n• Ultra compact APK size under 8 MB",
                        privacyPolicy = "No internet telemetry sent. All tool metrics are computed locally on device."
                    )
                )
                versionDao.insertVersion(
                    AppVersionEntity(
                        appId = app4Id,
                        versionName = "v4.0.5",
                        versionCode = 45,
                        apkFileName = "ProTool_Master_v4.0.5.apk",
                        fileSize = "7.8 MB",
                        fileSizeBytes = 8178890L,
                        changelog = "• Added 5G band support in network analyzer\n• Added Android 15 target compatibility\n• Faster APK extraction engine",
                        releaseNotes = "High performance maintenance update.",
                        isPublished = true,
                        isArchived = false,
                        downloadCount = 26500,
                        releaseDate = System.currentTimeMillis() - 86400000L * 1
                    )
                )
                screenshotDao.insertScreenshots(
                    listOf(
                        AppScreenshotEntity(appId = app4Id, title = "Network Analyzer", subtitle = "Wi-Fi channel congestion & latency graph", iconName = "wifi", accentColorHex = 0xFFF59E0B, sortOrder = 1),
                        AppScreenshotEntity(appId = app4Id, title = "Sensor Dashboard", subtitle = "Real-time gyroscope & compass readout", iconName = "sensors", accentColorHex = 0xFF10B981, sortOrder = 2)
                    )
                )

                // App 5: SnapPro Studio Camera
                val app5Id = appDao.insertApp(
                    AppEntity(
                        slug = "snappro-studio-camera",
                        name = "SnapPro Studio Camera",
                        tagline = "Professional manual camera controls, RAW DNG capture & cinema color LUTs",
                        description = "SnapPro Studio unlocks the full hardware capabilities of your phone camera. Full manual shutter speed, ISO, manual focus peaking, zebra stripes, and lossless RAW recording.",
                        category = "Photography",
                        developer = "Ajay",
                        iconResName = "img_app_icon",
                        bannerResName = "img_hero_banner",
                        minAndroidReq = "Android 10.0+",
                        rating = 4.8f,
                        ratingCount = 1890,
                        totalDownloads = 22400,
                        isFeatured = true,
                        isPublished = true,
                        downloadsEnabled = true,
                        features = "• Manual ISO (50-6400) and Shutter (1/8000s to 30s)\n• Real-time focus peaking with green/magenta highlights\n• 4K 60FPS high bitrate video recording\n• 14-bit RAW DNG export support\n• Custom cinematic color LUT preview",
                        privacyPolicy = "Camera & microphone permissions are only active during camera preview or video capture."
                    )
                )
                versionDao.insertVersion(
                    AppVersionEntity(
                        appId = app5Id,
                        versionName = "v1.5.0",
                        versionCode = 15,
                        apkFileName = "SnapPro_Studio_v1.5.0.apk",
                        fileSize = "32.1 MB",
                        fileSizeBytes = 33659200L,
                        changelog = "• Added anamorphic de-squeeze preview\n• Added histogram RGB waveform display\n• Improved optical image stabilization algorithm",
                        releaseNotes = "Cinema LUT pack included.",
                        isPublished = true,
                        isArchived = false,
                        downloadCount = 14300,
                        releaseDate = System.currentTimeMillis() - 86400000L * 5
                    )
                )
                screenshotDao.insertScreenshots(
                    listOf(
                        AppScreenshotEntity(appId = app5Id, title = "Manual Controls", subtitle = "Adjust ISO, shutter and Kelvin white balance", iconName = "tune", accentColorHex = 0xFFEC4899, sortOrder = 1),
                        AppScreenshotEntity(appId = app5Id, title = "RAW DNG Capture", subtitle = "Maximum dynamic range photography", iconName = "camera", accentColorHex = 0xFF8B5CF6, sortOrder = 2)
                    )
                )
            }
        }
    }
}
