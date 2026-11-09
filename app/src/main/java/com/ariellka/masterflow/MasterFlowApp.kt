package com.ariellka.masterflow

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import com.ariellka.masterflow.ui.theme.FlowSpace
import com.ariellka.masterflow.ui.theme.MasterFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterFlowApp(
    initialLink: String?,
    linkVersion: Int,
    onShare: () -> Unit,
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    initialThemeMode: String = "system",
    onThemeChanged: (String) -> Unit = {},
    onDarkThemeChanged: (Boolean) -> Unit = {},
    onExit: () -> Unit = {}
) {
    var destination by rememberSaveable { mutableStateOf(AppLinks.resolve(initialLink)) }
    var previousDestination by rememberSaveable { mutableStateOf(AppLinks.HOME) }
    var showMore by rememberSaveable { mutableStateOf(false) }
    var themeMode by rememberSaveable { mutableStateOf(initialThemeMode) }
    val darkTheme = if (themeMode == "system") isSystemInDarkTheme() else themeMode == "dark"
    val homeListState = rememberLazyListState()
    val scheduleListState = rememberLazyListState()
    val goHome = { destination = AppLinks.HOME; showMore = false }
    val goBack = {
        destination = when (destination) {
            AppLinks.SCHEDULE, AppLinks.INVALID -> AppLinks.HOME
            else -> previousDestination
        }
        showMore = false
    }

    SideEffect { onDarkThemeChanged(darkTheme) }
    LaunchedEffect(uiState.pendingNavigation?.id) {
        uiState.pendingNavigation?.let { event ->
            previousDestination = if (destination == AppLinks.SCHEDULE) AppLinks.SCHEDULE else AppLinks.HOME
            destination = event.destination
            showMore = false
            onAction(DashboardAction.NavigationHandled(event.id))
        }
    }
    LaunchedEffect(linkVersion) {
        if (linkVersion > 0) {
            destination = AppLinks.resolve(initialLink)
            previousDestination = AppLinks.HOME
            showMore = false
        }
    }
    BackHandler(enabled = destination != AppLinks.HOME, onBack = goBack)

    MasterFlowTheme(darkTheme = darkTheme) {
        val visibleDestination = if (destination == AppLinks.HOME) {
            when (uiState.status) {
                DashboardStatus.LOADING -> AppLinks.LOADING
                DashboardStatus.EMPTY -> AppLinks.EMPTY
                DashboardStatus.ERROR -> AppLinks.ERROR
                DashboardStatus.CONTENT -> AppLinks.HOME
            }
        } else destination

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (visibleDestination == AppLinks.HOME || destination == AppLinks.SCHEDULE) {
                    FlowBottomBar(selectedDestination = destination) { target ->
                        if (target == "more") showMore = true
                        else onAction(DashboardAction.OpenScreen(target))
                    }
                }
            }
        ) { padding ->
            val screenModifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
            when {
                destination == AppLinks.SCHEDULE -> ScheduleScreen(
                    uiState = uiState,
                    onAction = onAction,
                    onBack = goHome,
                    listState = scheduleListState,
                    modifier = screenModifier.testTag("schedule-screen")
                )
                visibleDestination == AppLinks.LOADING -> FlowStateScreen(AppLinks.LOADING, goHome, {}, screenModifier)
                visibleDestination == AppLinks.ERROR -> FlowStateScreen(
                    AppLinks.ERROR, onExit, { onAction(DashboardAction.Retry) }, screenModifier,
                    timeoutError = uiState.timedOut
                )
                visibleDestination == AppLinks.EMPTY -> FlowStateScreen(
                    AppLinks.EMPTY, onExit,
                    { onAction(DashboardAction.OpenScreen("new-appointment")) }, screenModifier
                )
                destination == AppLinks.HOME -> MainScreen(
                    uiState = uiState,
                    onAction = onAction,
                    modifier = screenModifier.testTag("home-screen"),
                    onShare = onShare,
                    onToggleTheme = {
                        themeMode = if (darkTheme) "light" else "dark"
                        onThemeChanged(themeMode)
                    },
                    darkTheme = darkTheme, listState = homeListState
                )
                destination == AppLinks.INVALID ->
                    FlowStateScreen(AppLinks.INVALID, goHome, goHome, screenModifier)
                destination.startsWith("task/") -> Box(screenModifier.testTag(destination.replace('/', '-'))) {
                    FlowStateScreen(AppLinks.EMPTY, goBack, {}, showPrimaryAction = false)
                }
                else -> Box(screenModifier.testTag("placeholder-$destination")) {
                    FlowStateScreen(AppLinks.EMPTY, goBack, {}, showPrimaryAction = false)
                }
            }
        }
        if (showMore) {
            ModalBottomSheet(onDismissRequest = { showMore = false }, containerColor = MaterialTheme.colorScheme.background) {
                Column(Modifier.fillMaxWidth().padding(FlowSpace.page),
                    verticalArrangement = Arrangement.spacedBy(FlowSpace.small)) {
                    Text(stringResource(R.string.more), style = MaterialTheme.typography.titleLarge)
                    TextButton(
                        onClick = { showMore = false; onAction(DashboardAction.Refresh) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth().heightIn(min = FlowSpace.touch).testTag("refresh")
                    ) { Text(stringResource(R.string.refresh)) }
                    TextButton(onClick = { showMore = false; onShare() }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)) {
                        Icon(painterResource(R.drawable.ic_share), null)
                        Text(stringResource(R.string.share_app), Modifier.padding(start = FlowSpace.small))
                    }
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }
}
