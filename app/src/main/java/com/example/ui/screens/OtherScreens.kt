package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MindStoreViewModel
import com.example.ui.viewmodel.WebNavDestination
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AllAppsScreen(
    viewModel: MindStoreViewModel,
    modifier: Modifier = Modifier
) {
    val apps by viewModel.filteredApps.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val allVersions by viewModel.allVersions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("all_apps_screen"),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "Search Results for '$searchQuery'" else "All Applications (${apps.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Explore verified APK packages available for direct installation.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            CategoryFilterRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onSelectCategory = { cat -> viewModel.setSelectedCategory(cat) }
            )
        }

        if (apps.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No apps match your filter criteria.", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = {
                            viewModel.setSearchQuery("")
                            viewModel.setSelectedCategory("All")
                        }) {
                            Text("Reset Filters")
                        }
                    }
                }
            }
        } else {
            items(apps) { app ->
                val latest = allVersions.firstOrNull { it.appId == app.id && it.isPublished }
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    AppMarketCard(
                        app = app,
                        latestVersion = latest,
                        onCardClick = { viewModel.navigateTo(WebNavDestination.AppDetail(app.slug)) },
                        onDownloadClick = {
                            if (latest != null) {
                                viewModel.startDownload(app, latest)
                            } else {
                                viewModel.navigateTo(WebNavDestination.AppDetail(app.slug))
                            }
                        }
                    )
                }
            }
        }

        item {
            WebFooter(onNavigate = { dest -> viewModel.navigateTo(dest) })
        }
    }
}

@Composable
fun UpdatesScreen(
    viewModel: MindStoreViewModel,
    modifier: Modifier = Modifier
) {
    val allVersions by viewModel.allVersions.collectAsState()
    val allApps by viewModel.allApps.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("updates_screen"),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Update, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Latest Updates & Changelogs",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Stay up to date with new features, bug patches and security improvements across Mind Store apps.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(allVersions) { version ->
            val app = allApps.firstOrNull { it.id == version.appId }
            if (app != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ElectricBlue.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(getCategoryIcon(app.category), contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = app.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = "Release: ${dateFormat.format(Date(version.releaseDate))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Surface(
                                color = ElectricBlue.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = version.versionName,
                                    color = ElectricBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (version.releaseNotes.isNotEmpty()) {
                            Text(
                                text = version.releaseNotes,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Text(
                            text = version.changelog,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Size: ${version.fileSize} • File: ${version.apkFileName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Button(
                                onClick = { viewModel.startDownload(app, version) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download APK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            WebFooter(onNavigate = { dest -> viewModel.navigateTo(dest) })
        }
    }
}

@Composable
fun LegalPagesScreen(
    pageType: String,
    viewModel: MindStoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("legal_page_$pageType"),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    when (pageType) {
                        "privacy" -> {
                            Text("Privacy Policy", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = """
Mind Store (Operated by Ajay) is dedicated to user privacy and open APK distribution.

1. Information We Collect:
- Anonymous download counter statistics to track package popularity.
- We do not collect names, phone numbers, or device tracking IDs from public visitors.

2. APK Package Integrity:
- All APK packages hosted on Mind Store are verified with original SHA-256 signatures before being published.

3. Developer & Data Inquiries:
- Developer: Ajay
- Support Email: mindflayer812@gmail.com
- Phone: 8595311812
                                """.trimIndent(),
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        "terms" -> {
                            Text("Terms and Conditions", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = """
Welcome to Mind Store. By accessing or downloading APKs from this website, you agree to comply with the following terms:

1. Software License: All applications published on Mind Store are owned by their respective authors and developers.
2. Safe Usage: You are responsible for ensuring compatibility with your Android hardware before sideloading.
3. No Malicious Redistribution: Uploading or distributing malware, spyware, or modified malicious packages is strictly prohibited.
                                """.trimIndent(),
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        "disclaimer" -> {
                            Text("Disclaimer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = """
Mind Store is an independent Android APK publishing platform. Android is a trademark of Google LLC.

All application packages (.apk) are provided 'as-is' for testing, productivity, and verified distribution. Sideloading requires enabling 'Install unknown apps' permission in Android Settings.
                                """.trimIndent(),
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        "contact" -> {
                            Text("Contact & Publisher Information", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Have inquiries regarding app publishing, DMCA requests, or technical support? Contact the developer directly:",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            ContactItem(icon = Icons.Default.Person, title = "Developer / Publisher", value = "Ajay")
                            ContactItem(icon = Icons.Default.Phone, title = "Phone Support", value = "8595311812")
                            ContactItem(icon = Icons.Default.Email, title = "Direct Email", value = "mindflayer812@gmail.com")

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:mindflayer812@gmail.com")
                                            putExtra(Intent.EXTRA_SUBJECT, "Inquiry from Mind Store")
                                        }
                                        try { context.startActivity(intent) } catch (_: Exception) {}
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send Email")
                                }

                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:8595311812")
                                        }
                                        try { context.startActivity(intent) } catch (_: Exception) {}
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call Support")
                                }
                            }
                        }
                        "installation_guide" -> {
                            Text("Complete Android APK Installation Guide", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Sideloading an APK on Android allows you to install apps directly outside of traditional app stores. Follow these simple steps:",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "1. Click 'Download APK' on any app page.\n2. When prompted 'File might be harmful', click 'Download anyway'. (All Mind Store APKs are signed & verified).\n3. Open your browser's download notifications or Files app.\n4. If prompted, toggle 'Allow from this source'.\n5. Tap 'Install' and launch your app!",
                                fontSize = 13.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        else -> {
                            Text("About Mind Store", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Mind Store is a next-generation Android APK marketplace designed to give developers full autonomy over version distribution and give users fast, safe, direct access to Android packages without intrusive ads or forced logins.",
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            WebFooter(onNavigate = { dest -> viewModel.navigateTo(dest) })
        }
    }
}

@Composable
private fun ContactItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
