package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "apps",
    indices = [Index(value = ["slug"], unique = true)]
)
data class AppEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slug: String,
    val name: String,
    val tagline: String,
    val description: String,
    val category: String,
    val developer: String = "Ajay",
    val iconResName: String = "img_app_icon",
    val bannerResName: String = "img_hero_banner",
    val minAndroidReq: String = "Android 8.0+",
    val rating: Float = 4.8f,
    val ratingCount: Int = 1250,
    val totalDownloads: Int = 14200,
    val isFeatured: Boolean = true,
    val isPublished: Boolean = true,
    val downloadsEnabled: Boolean = true,
    val features: String = "", // JSON or newline-separated
    val privacyPolicy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "app_versions",
    foreignKeys = [
        ForeignKey(
            entity = AppEntity::class,
            parentColumns = ["id"],
            childColumns = ["appId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["appId"])]
)
data class AppVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appId: Long,
    val versionName: String, // e.g. "v2.4.0"
    val versionCode: Int,     // e.g. 24
    val apkFileName: String, // e.g. "MindAI_v2.4.0.apk"
    val fileSize: String,    // e.g. "24.8 MB"
    val fileSizeBytes: Long = 26004600L,
    val changelog: String = "",
    val releaseNotes: String = "",
    val isPublished: Boolean = true,
    val isArchived: Boolean = false,
    val downloadCount: Int = 0,
    val releaseDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "app_screenshots",
    foreignKeys = [
        ForeignKey(
            entity = AppEntity::class,
            parentColumns = ["id"],
            childColumns = ["appId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["appId"])]
)
data class AppScreenshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appId: Long,
    val title: String,
    val subtitle: String,
    val iconName: String = "dashboard",
    val accentColorHex: Long = 0xFF2563EB,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["slug"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val slug: String,
    val iconName: String,
    val description: String,
    val colorHex: Long = 0xFF3B82F6,
    val appCount: Int = 0
)

@Entity(tableName = "site_settings")
data class SiteSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

// Combined Data Holder for App with Latest Version
data class AppWithDetails(
    val app: AppEntity,
    val latestVersion: AppVersionEntity?,
    val allVersions: List<AppVersionEntity> = emptyList(),
    val screenshots: List<AppScreenshotEntity> = emptyList()
)
