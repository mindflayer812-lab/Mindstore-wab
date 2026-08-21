package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MindStoreTheme
import com.example.ui.viewmodel.MindStoreViewModel
import com.example.ui.viewmodel.WebNavDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mindStoreViewModel: MindStoreViewModel = viewModel()
            val isDarkThemePref by mindStoreViewModel.isDarkTheme.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val activeDark = isDarkThemePref ?: systemDark

            MindStoreTheme(darkTheme = activeDark) {
                MindStoreWebApp(
                    viewModel = mindStoreViewModel,
                    isDarkTheme = activeDark,
                    onToggleTheme = { mindStoreViewModel.toggleTheme() }
                )
            }
        }
    }
}

@Composable
fun MindStoreWebApp(
    viewModel: MindStoreViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val currentDestination by viewModel.currentDestination.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val previewScreenshot by viewModel.previewScreenshot.collectAsState()

    // Handle back button for browser history navigation
    BackHandler(enabled = currentDestination !is WebNavDestination.Home) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            WebNavbar(
                currentDestination = currentDestination,
                searchQuery = searchQuery,
                isDarkTheme = isDarkTheme,
                onNavigate = { dest -> viewModel.navigateTo(dest) },
                onSearchChange = { query -> viewModel.setSearchQuery(query) },
                onToggleTheme = onToggleTheme,
                onBack = { viewModel.navigateBack() },
                canGoBack = currentDestination !is WebNavDestination.Home
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Routing Viewport
            when (val dest = currentDestination) {
                is WebNavDestination.Home -> {
                    HomeScreen(viewModel = viewModel)
                }
                is WebNavDestination.AppDetail -> {
                    AppDetailScreen(slug = dest.slug, viewModel = viewModel)
                }
                is WebNavDestination.AllApps -> {
                    AllAppsScreen(viewModel = viewModel)
                }
                is WebNavDestination.CategoryFilter -> {
                    AllAppsScreen(viewModel = viewModel)
                }
                is WebNavDestination.Updates -> {
                    UpdatesScreen(viewModel = viewModel)
                }
                is WebNavDestination.Legal -> {
                    LegalPagesScreen(pageType = dest.pageType, viewModel = viewModel)
                }
                is WebNavDestination.Admin -> {
                    AdminScreen(viewModel = viewModel)
                }
            }

            // Real-time Download Progress Modal Dialog
            DownloadProgressModal(
                downloadState = downloadState,
                onDismiss = { viewModel.dismissDownloadModal() },
                onOpenGuide = {
                    viewModel.dismissDownloadModal()
                    viewModel.navigateTo(WebNavDestination.Legal("installation_guide"))
                }
            )

            // High-Resolution Screenshot Lightbox
            ScreenshotLightboxModal(
                screenshot = previewScreenshot,
                onDismiss = { viewModel.closeScreenshotPreview() }
            )
        }
    }
}
