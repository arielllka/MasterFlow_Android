package com.ariellka.masterflow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
    initialLink: String?,
    linkVersion: Int,
    onShare: () -> Unit,
    initialThemeMode: String,
    onThemeChanged: (String) -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit,
    onExit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.loadOnce() }
    MasterFlowApp(
        initialLink = initialLink,
        linkVersion = linkVersion,
        onShare = onShare,
        uiState = uiState,
        onAction = viewModel::onAction,
        initialThemeMode = initialThemeMode,
        onThemeChanged = onThemeChanged,
        onDarkThemeChanged = onDarkThemeChanged,
        onExit = onExit
    )
}
