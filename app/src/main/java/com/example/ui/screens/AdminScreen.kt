package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.MindStoreViewModel
import com.example.ui.viewmodel.WebNavDestination
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminScreen(
    viewModel: MindStoreViewModel,
    modifier: Modifier = Modifier
) {
    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val activeTab by viewModel.activeAdminTab.collectAsState()
    val loginError by viewModel.adminLoginError.collectAsState()

    if (!isLoggedIn) {
        AdminLoginView(
            error = loginError,
            onLogin = { pin -> viewModel.loginAdmin(pin) },
            onCancel = { viewModel.navigateTo(WebNavDestination.Home) },
            modifier = modifier
        )
    } else {
        AdminDashboardView(
            viewModel = viewModel,
            activeTab = activeTab,
            onSelectTab = { tab -> viewModel.selectAdminTab(tab) },
            onLogout = { viewModel.logoutAdmin() },
            modifier = modifier
        )
    }
}

@Composable
private fun AdminLoginView(
    error: String?,
    onLogin: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pinInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(ElectricBlue, VioletAccent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Lock",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Mind Store Admin Portal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Enter your secure master PIN to manage APK releases, apps, categories and site settings.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("Admin PIN (Default: admin123)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input"),
                    leadingIcon = {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = RoseError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back to Store")
                    }

                    Button(
                        onClick = { onLogin(pinInput) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_login_submit_button")
                    ) {
                        Text("Login", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardView(
    viewModel: MindStoreViewModel,
    activeTab: AdminTab,
    onSelectTab: (AdminTab) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isAddAppOpen by remember { mutableStateOf(false) }
    var isAddVersionOpen by remember { mutableStateOf(false) }
    var isAddCategoryOpen by remember { mutableStateOf(false) }
    var appToEdit by remember { mutableStateOf<AppEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_container")
    ) {
        // Admin Top Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Admin Panel (Ajay)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(WebNavDestination.Home) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("View Store", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onLogout,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Logout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Admin Tab Navigation
        ScrollableTabRow(
            selectedTabIndex = activeTab.ordinal,
            edgePadding = 12.dp,
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) }
        ) {
            AdminTab.entries.forEach { tab ->
                Tab(
                    selected = activeTab == tab,
                    onClick = { onSelectTab(tab) },
                    text = {
                        Text(
                            text = when (tab) {
                                AdminTab.OVERVIEW -> "Overview"
                                AdminTab.APPS_MANAGEMENT -> "Apps Manager"
                                AdminTab.APK_VERSIONS -> "APK Releases"
                                AdminTab.CATEGORIES -> "Categories"
                                AdminTab.SITE_SETTINGS -> "Settings & Contact"
                                AdminTab.LEGAL_PAGES -> "Legal Pages"
                                AdminTab.SEO_PREVIEW -> "SEO & Web Export"
                            },
                            fontSize = 12.sp,
                            fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                AdminTab.OVERVIEW -> AdminOverviewTab(viewModel)
                AdminTab.APPS_MANAGEMENT -> AdminAppsTab(
                    viewModel = viewModel,
                    onAddNewApp = { isAddAppOpen = true },
                    onEditApp = { app ->
                        appToEdit = app
                        isAddAppOpen = true
                    }
                )
                AdminTab.APK_VERSIONS -> AdminVersionsTab(
                    viewModel = viewModel,
                    onUploadNewVersion = { isAddVersionOpen = true }
                )
                AdminTab.CATEGORIES -> AdminCategoriesTab(
                    viewModel = viewModel,
                    onAddCategory = { isAddCategoryOpen = true }
                )
                AdminTab.SITE_SETTINGS -> AdminSettingsTab(viewModel)
                AdminTab.LEGAL_PAGES -> AdminLegalEditorTab(viewModel)
                AdminTab.SEO_PREVIEW -> AdminSeoTab(viewModel)
            }
        }
    }

    // Modal Dialogs for Admin Actions
    if (isAddAppOpen) {
        AppEditorDialog(
            app = appToEdit,
            onDismiss = {
                isAddAppOpen = false
                appToEdit = null
            },
            onSave = { savedApp ->
                viewModel.saveApp(savedApp)
                isAddAppOpen = false
                appToEdit = null
            }
        )
    }

    if (isAddVersionOpen) {
        val allApps by viewModel.allApps.collectAsState()
        VersionUploaderDialog(
            apps = allApps,
            onDismiss = { isAddVersionOpen = false },
            onSave = { newVersion ->
                viewModel.saveVersion(newVersion)
                isAddVersionOpen = false
            }
        )
    }

    if (isAddCategoryOpen) {
        CategoryEditorDialog(
            onDismiss = { isAddCategoryOpen = false },
            onSave = { name, slug, desc, icon ->
                viewModel.addCategory(name, slug, desc, icon)
                isAddCategoryOpen = false
            }
        )
    }
}

@Composable
private fun AdminOverviewTab(viewModel: MindStoreViewModel) {
    val totalApps by viewModel.totalAppsCount.collectAsState()
    val publishedApps by viewModel.publishedAppsCount.collectAsState()
    val totalVersions by viewModel.totalVersionsCount.collectAsState()
    val totalDownloads by viewModel.totalDownloadsCount.collectAsState()
    val recentUpdates by viewModel.recentUpdates.collectAsState()
    val allApps by viewModel.allApps.collectAsState()

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dashboard Analytics & Health",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Metrics Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(title = "Total Apps", value = "$totalApps", sub = "$publishedApps published", color = ElectricBlue, modifier = Modifier.weight(1f))
                    MetricCard(title = "APK Versions", value = "$totalVersions", sub = "Active & archived", color = VioletAccent, modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(title = "Total Downloads", value = "$totalDownloads", sub = "Across all APKs", color = EmeraldSuccess, modifier = Modifier.weight(1f))
                    MetricCard(title = "Server Status", value = "ONLINE", sub = "SHA-256 Verified", color = CyanAccent, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Text(
                text = "Recent APK Version Releases",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (recentUpdates.isEmpty()) {
                        Text("No recent uploads found.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        recentUpdates.forEach { version ->
                            val app = allApps.firstOrNull { it.id == version.appId }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${app?.name ?: "App"} • ${version.versionName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${version.apkFileName} (${version.fileSize})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = dateFormat.format(Date(version.releaseDate)),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Surface(
                                    color = if (version.isPublished) EmeraldSuccess.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (version.isPublished) "Published" else "Draft",
                                        color = if (version.isPublished) EmeraldSuccess else AmberWarning,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, sub: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = sub, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdminAppsTab(
    viewModel: MindStoreViewModel,
    onAddNewApp: () -> Unit,
    onEditApp: (AppEntity) -> Unit
) {
    val allApps by viewModel.allApps.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "App Management (${allApps.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onAddNewApp,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.testTag("admin_add_new_app_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add New App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(allApps) { app ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(getCategoryIcon(app.category), contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = app.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Slug: /app/${app.slug} • ${app.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = app.description, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Downloads:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = app.downloadsEnabled,
                                onCheckedChange = { isEnabled -> viewModel.toggleAppDownloads(app.id, isEnabled) },
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onEditApp(app) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.deleteApp(app.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminVersionsTab(
    viewModel: MindStoreViewModel,
    onUploadNewVersion: () -> Unit
) {
    val allVersions by viewModel.allVersions.collectAsState()
    val allApps by viewModel.allApps.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "APK Version Management",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onUploadNewVersion,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.testTag("admin_upload_version_button")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload APK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(allVersions) { version ->
            val app = allApps.firstOrNull { it.id == version.appId }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = app?.name ?: "Unknown App", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Binary: ${version.apkFileName} (${version.fileSize})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = ElectricBlue.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = version.versionName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Changelog: ${version.changelog}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.archiveVersion(version.id, !version.isArchived) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(if (version.isArchived) "Unarchive" else "Archive", fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = { viewModel.deleteVersion(version) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Delete", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCategoriesTab(
    viewModel: MindStoreViewModel,
    onAddCategory: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categories (${categories.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onAddCategory,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Category", fontSize = 12.sp)
                }
            }
        }

        items(categories) { category ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(getCategoryIcon(category.name), contentDescription = null, tint = Color(category.colorHex), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = category.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = category.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    IconButton(onClick = { viewModel.deleteCategory(category) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseError, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSettingsTab(viewModel: MindStoreViewModel) {
    var siteName by remember { mutableStateOf("Mind Store") }
    var developerName by remember { mutableStateOf("Ajay") }
    var phone by remember { mutableStateOf("8595311812") }
    var email by remember { mutableStateOf("mindflayer812@gmail.com") }
    var adminPin by remember { mutableStateOf("admin123") }
    var isSavedToast by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Central Site Settings & Contact Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Update site name, logo details, and developer contact information dynamically without editing code.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = siteName,
                onValueChange = { siteName = it },
                label = { Text("Website Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = developerName,
                onValueChange = { developerName = it },
                label = { Text("Developer / Publisher Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Developer Phone Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Developer Support Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = adminPin,
                onValueChange = { adminPin = it },
                label = { Text("Master Admin PIN") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {
                    viewModel.saveAllSiteSettings(
                        mapOf(
                            "site_name" to siteName,
                            "developer_name" to developerName,
                            "developer_phone" to phone,
                            "developer_email" to email,
                            "admin_pin" to adminPin
                        )
                    )
                    isSavedToast = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Site Settings", fontWeight = FontWeight.Bold)
            }

            if (isSavedToast) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ Settings successfully updated and stored in database!",
                    color = EmeraldSuccess,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AdminLegalEditorTab(viewModel: MindStoreViewModel) {
    var privacyText by remember {
        mutableStateOf("""
# Privacy Policy for Mind Store

Developer: Ajay
Email: mindflayer812@gmail.com
Phone: 8595311812

Mind Store is committed to data privacy. We distribute APK packages directly without invasive trackers.
        """.trimIndent())
    }
    var isSaved by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Legal Pages Content Editor",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = privacyText,
                onValueChange = { privacyText = it },
                label = { Text("Privacy Policy Markdown") },
                minLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {
                    viewModel.saveSiteSetting("privacy_policy", privacyText)
                    isSaved = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Legal Content", fontWeight = FontWeight.Bold)
            }

            if (isSaved) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("✓ Legal terms updated successfully!", color = EmeraldSuccess, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AdminSeoTab(viewModel: MindStoreViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "SEO Optimization & Web Engine Export",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Mind Store generates clean URLs, OpenGraph metadata, JSON-LD Schema, sitemap.xml, and robots.txt.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Live OpenGraph & Meta Headers Preview:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = """
<title>Mind Store - Android APK Publishing & Download Hub</title>
<meta name="description" content="Download verified Android APKs directly from Mind Store." />
<meta property="og:title" content="Mind Store" />
<meta property="og:type" content="website" />
<meta property="og:url" content="https://mindstore.app" />
<link rel="canonical" href="https://mindstore.app" />
                        """.trimIndent(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Sitemap & Robots.txt Specification:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = """
User-agent: *
Allow: /
Disallow: /admin

Sitemap: https://mindstore.app/sitemap.xml
                        """.trimIndent(),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// Dialog: App Editor
@Composable
private fun AppEditorDialog(
    app: AppEntity?,
    onDismiss: () -> Unit,
    onSave: (AppEntity) -> Unit
) {
    var name by remember { mutableStateOf(app?.name ?: "") }
    var slug by remember { mutableStateOf(app?.slug ?: "") }
    var category by remember { mutableStateOf(app?.category ?: "AI Apps") }
    var developer by remember { mutableStateOf(app?.developer ?: "Ajay") }
    var tagline by remember { mutableStateOf(app?.tagline ?: "") }
    var description by remember { mutableStateOf(app?.description ?: "") }
    var minAndroidReq by remember { mutableStateOf(app?.minAndroidReq ?: "Android 8.0+") }
    var features by remember { mutableStateOf(app?.features ?: "") }
    var privacyPolicy by remember { mutableStateOf(app?.privacyPolicy ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = if (app == null) "Publish New Android App" else "Edit App: ${app.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (app == null) slug = it.lowercase(Locale.ROOT).replace(" ", "-").replace(Regex("[^a-z0-9-]"), "")
                        },
                        label = { Text("App Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = slug,
                        onValueChange = { slug = it },
                        label = { Text("Slug / URL Path (/app/{slug})") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (e.g. AI Apps, Farming, Tools)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = developer,
                        onValueChange = { developer = it },
                        label = { Text("Developer (e.g. Ajay)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("Short Tagline") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Full Description") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = features,
                        onValueChange = { features = it },
                        label = { Text("Features (one per line)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val toSave = (app ?: AppEntity(slug = slug, name = name, tagline = tagline, description = description, category = category)).copy(
                                        name = name,
                                        slug = if (slug.isBlank()) name.lowercase(Locale.ROOT).replace(" ", "-") else slug,
                                        category = category,
                                        developer = developer,
                                        tagline = tagline,
                                        description = description,
                                        features = features,
                                        privacyPolicy = privacyPolicy,
                                        minAndroidReq = minAndroidReq
                                    )
                                    onSave(toSave)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save App")
                        }
                    }
                }
            }
        }
    }
}

// Dialog: Version Uploader
@Composable
private fun VersionUploaderDialog(
    apps: List<AppEntity>,
    onDismiss: () -> Unit,
    onSave: (AppVersionEntity) -> Unit
) {
    var selectedAppId by remember { mutableLongStateOf(apps.firstOrNull()?.id ?: 0L) }
    var versionName by remember { mutableStateOf("v2.5.0") }
    var versionCode by remember { mutableStateOf("25") }
    var apkFileName by remember { mutableStateOf("app_release_v2.5.0.apk") }
    var fileSize by remember { mutableStateOf("22.4 MB") }
    var changelog by remember { mutableStateOf("• Bug fixes & performance improvements") }
    var releaseNotes by remember { mutableStateOf("Production stable update") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Upload & Publish New APK Version",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                item {
                    Text("Select Target Application:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    apps.forEach { app ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAppId = app.id
                                    apkFileName = "${app.slug.replace("-", "_")}_$versionName.apk"
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedAppId == app.id,
                                onClick = {
                                    selectedAppId = app.id
                                    apkFileName = "${app.slug.replace("-", "_")}_$versionName.apk"
                                }
                            )
                            Text(text = app.name, fontSize = 13.sp, fontWeight = if (selectedAppId == app.id) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = versionName,
                        onValueChange = {
                            versionName = it
                            val target = apps.firstOrNull { a -> a.id == selectedAppId }
                            if (target != null) {
                                apkFileName = "${target.slug.replace("-", "_")}_$it.apk"
                            }
                        },
                        label = { Text("Version Name (e.g. v2.5.0)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = versionCode,
                        onValueChange = { versionCode = it },
                        label = { Text("Version Code (e.g. 25)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = fileSize,
                        onValueChange = { fileSize = it },
                        label = { Text("Calculated APK Size (e.g. 22.4 MB)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = changelog,
                        onValueChange = { changelog = it },
                        label = { Text("Changelog / Release Notes") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (selectedAppId != 0L && versionName.isNotBlank()) {
                                    val version = AppVersionEntity(
                                        appId = selectedAppId,
                                        versionName = versionName,
                                        versionCode = versionCode.toIntOrNull() ?: 1,
                                        apkFileName = apkFileName,
                                        fileSize = fileSize,
                                        changelog = changelog,
                                        releaseNotes = releaseNotes,
                                        isPublished = true,
                                        releaseDate = System.currentTimeMillis()
                                    )
                                    onSave(version)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Publish APK")
                        }
                    }
                }
            }
        }
    }
}

// Dialog: Category Editor
@Composable
private fun CategoryEditorDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("category") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add New Category", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        slug = it.lowercase(Locale.ROOT).replace(" ", "-")
                    },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, slug, description, icon)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Category")
                    }
                }
            }
        }
    }
}
