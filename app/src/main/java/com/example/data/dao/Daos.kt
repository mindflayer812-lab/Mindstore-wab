package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY updatedAt DESC")
    fun getAllApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isPublished = 1 ORDER BY updatedAt DESC")
    fun getPublishedApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isFeatured = 1 AND isPublished = 1 ORDER BY rating DESC")
    fun getFeaturedApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE slug = :slug LIMIT 1")
    suspend fun getAppBySlug(slug: String): AppEntity?

    @Query("SELECT * FROM apps WHERE id = :id LIMIT 1")
    suspend fun getAppById(id: Long): AppEntity?

    @Query("SELECT * FROM apps WHERE isPublished = 1 AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')")
    fun searchApps(query: String): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isPublished = 1 AND category = :category ORDER BY updatedAt DESC")
    fun getAppsByCategory(category: String): Flow<List<AppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppEntity): Long

    @Update
    suspend fun updateApp(app: AppEntity)

    @Delete
    suspend fun deleteApp(app: AppEntity)

    @Query("DELETE FROM apps WHERE id = :id")
    suspend fun deleteAppById(id: Long)

    @Query("UPDATE apps SET totalDownloads = totalDownloads + 1 WHERE id = :id")
    suspend fun incrementDownloadCount(id: Long)

    @Query("UPDATE apps SET downloadsEnabled = :enabled WHERE id = :id")
    suspend fun setDownloadStatus(id: Long, enabled: Boolean)

    @Query("SELECT COUNT(*) FROM apps")
    fun getTotalAppsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM apps WHERE isPublished = 1")
    fun getPublishedAppsCount(): Flow<Int>
}

@Dao
interface VersionDao {
    @Query("SELECT * FROM app_versions WHERE appId = :appId ORDER BY versionCode DESC")
    fun getVersionsForApp(appId: Long): Flow<List<AppVersionEntity>>

    @Query("SELECT * FROM app_versions WHERE appId = :appId AND isPublished = 1 AND isArchived = 0 ORDER BY versionCode DESC LIMIT 1")
    suspend fun getLatestPublishedVersion(appId: Long): AppVersionEntity?

    @Query("SELECT * FROM app_versions WHERE appId = :appId AND isPublished = 1 AND isArchived = 0 ORDER BY versionCode DESC LIMIT 1")
    fun observeLatestPublishedVersion(appId: Long): Flow<AppVersionEntity?>

    @Query("SELECT * FROM app_versions ORDER BY releaseDate DESC")
    fun getAllVersions(): Flow<List<AppVersionEntity>>

    @Query("SELECT * FROM app_versions WHERE isPublished = 1 ORDER BY releaseDate DESC LIMIT 10")
    fun getRecentUpdates(): Flow<List<AppVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: AppVersionEntity): Long

    @Update
    suspend fun updateVersion(version: AppVersionEntity)

    @Delete
    suspend fun deleteVersion(version: AppVersionEntity)

    @Query("UPDATE app_versions SET isArchived = :isArchived WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean)

    @Query("UPDATE app_versions SET isPublished = :isPublished WHERE id = :id")
    suspend fun setPublished(id: Long, isPublished: Boolean)

    @Query("UPDATE app_versions SET downloadCount = downloadCount + 1 WHERE id = :id")
    suspend fun incrementVersionDownloadCount(id: Long)

    @Query("SELECT COUNT(*) FROM app_versions")
    fun getTotalVersionsCount(): Flow<Int>

    @Query("SELECT SUM(downloadCount) FROM app_versions")
    fun getTotalDownloadsCount(): Flow<Int?>
}

@Dao
interface ScreenshotDao {
    @Query("SELECT * FROM app_screenshots WHERE appId = :appId ORDER BY sortOrder ASC")
    fun getScreenshotsForApp(appId: Long): Flow<List<AppScreenshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshot(screenshot: AppScreenshotEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshots(screenshots: List<AppScreenshotEntity>)

    @Delete
    suspend fun deleteScreenshot(screenshot: AppScreenshotEntity)

    @Query("DELETE FROM app_screenshots WHERE appId = :appId")
    suspend fun deleteAllScreenshotsForApp(appId: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM site_settings")
    fun getAllSettings(): Flow<List<SiteSettingEntity>>

    @Query("SELECT value FROM site_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Query("SELECT value FROM site_settings WHERE `key` = :key LIMIT 1")
    fun observeSetting(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: SiteSettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSettings(settings: List<SiteSettingEntity>)
}
