package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class MindStoreRepository(
    private val appDao: AppDao,
    private val versionDao: VersionDao,
    private val screenshotDao: ScreenshotDao,
    private val categoryDao: CategoryDao,
    private val settingsDao: SettingsDao
) {
    val allApps: Flow<List<AppEntity>> = appDao.getAllApps()
    val publishedApps: Flow<List<AppEntity>> = appDao.getPublishedApps()
    val featuredApps: Flow<List<AppEntity>> = appDao.getFeaturedApps()
    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allVersions: Flow<List<AppVersionEntity>> = versionDao.getAllVersions()
    val recentUpdates: Flow<List<AppVersionEntity>> = versionDao.getRecentUpdates()
    val allSettings: Flow<List<SiteSettingEntity>> = settingsDao.getAllSettings()

    // Analytics stats for admin
    val totalAppsCount: Flow<Int> = appDao.getTotalAppsCount()
    val publishedAppsCount: Flow<Int> = appDao.getPublishedAppsCount()
    val totalVersionsCount: Flow<Int> = versionDao.getTotalVersionsCount()
    val totalDownloadsCount: Flow<Int?> = versionDao.getTotalDownloadsCount()

    fun searchApps(query: String): Flow<List<AppEntity>> {
        return if (query.isBlank()) {
            appDao.getPublishedApps()
        } else {
            appDao.searchApps(query)
        }
    }

    fun getAppsByCategory(category: String): Flow<List<AppEntity>> {
        return if (category.equals("All", ignoreCase = true) || category.isBlank()) {
            appDao.getPublishedApps()
        } else {
            appDao.getAppsByCategory(category)
        }
    }

    suspend fun getAppBySlug(slug: String): AppEntity? {
        return appDao.getAppBySlug(slug)
    }

    suspend fun getAppById(id: Long): AppEntity? {
        return appDao.getAppById(id)
    }

    fun getVersionsForApp(appId: Long): Flow<List<AppVersionEntity>> {
        return versionDao.getVersionsForApp(appId)
    }

    fun observeLatestPublishedVersion(appId: Long): Flow<AppVersionEntity?> {
        return versionDao.observeLatestPublishedVersion(appId)
    }

    suspend fun getLatestPublishedVersion(appId: Long): AppVersionEntity? {
        return versionDao.getLatestPublishedVersion(appId)
    }

    fun getScreenshotsForApp(appId: Long): Flow<List<AppScreenshotEntity>> {
        return screenshotDao.getScreenshotsForApp(appId)
    }

    suspend fun insertApp(app: AppEntity): Long {
        return appDao.insertApp(app)
    }

    suspend fun updateApp(app: AppEntity) {
        appDao.updateApp(app.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteAppById(appId: Long) {
        screenshotDao.deleteAllScreenshotsForApp(appId)
        appDao.deleteAppById(appId)
    }

    suspend fun setDownloadStatus(appId: Long, enabled: Boolean) {
        appDao.setDownloadStatus(appId, enabled)
    }

    suspend fun insertVersion(version: AppVersionEntity): Long {
        val versionId = versionDao.insertVersion(version)
        // Also update the parent app's updatedAt
        val app = appDao.getAppById(version.appId)
        if (app != null) {
            appDao.updateApp(app.copy(updatedAt = System.currentTimeMillis()))
        }
        return versionId
    }

    suspend fun updateVersion(version: AppVersionEntity) {
        versionDao.updateVersion(version)
    }

    suspend fun deleteVersion(version: AppVersionEntity) {
        versionDao.deleteVersion(version)
    }

    suspend fun setVersionArchived(versionId: Long, isArchived: Boolean) {
        versionDao.setArchived(versionId, isArchived)
    }

    suspend fun setVersionPublished(versionId: Long, isPublished: Boolean) {
        versionDao.setPublished(versionId, isPublished)
    }

    suspend fun registerAppDownload(appId: Long, versionId: Long) {
        appDao.incrementDownloadCount(appId)
        versionDao.incrementVersionDownloadCount(versionId)
    }

    suspend fun insertScreenshot(screenshot: AppScreenshotEntity) {
        screenshotDao.insertScreenshot(screenshot)
    }

    suspend fun deleteScreenshot(screenshot: AppScreenshotEntity) {
        screenshotDao.deleteScreenshot(screenshot)
    }

    suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    suspend fun getSetting(key: String): String? {
        return settingsDao.getSetting(key)
    }

    fun observeSetting(key: String): Flow<String?> {
        return settingsDao.observeSetting(key)
    }

    suspend fun saveSetting(key: String, value: String) {
        settingsDao.setSetting(SiteSettingEntity(key, value))
    }

    suspend fun saveSettings(settings: Map<String, String>) {
        val list = settings.map { (k, v) -> SiteSettingEntity(k, v) }
        settingsDao.setSettings(list)
    }
}
