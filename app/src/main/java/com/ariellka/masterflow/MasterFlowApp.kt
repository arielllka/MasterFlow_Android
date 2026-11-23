package com.ariellka.masterflow

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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
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
    val backStack = rememberNavBackStack(*initialBackStack(initialLink).toTypedArray())
    val destination = backStack.lastOrNull() as? AppDestination ?: AppDestination.Home
    var showMore by rememberSaveable { mutableStateOf(false) }
    var themeMode by rememberSaveable { mutableStateOf(initialThemeMode) }
    val darkTheme = if (themeMode == "system") isSystemInDarkTheme() else themeMode == "dark"
    val homeListState = rememberLazyListState()
    val scheduleListState = rememberLazyListState()
    val goHome = { backStack.navigateTo(AppDestination.Home); showMore = false }
    val goBack = {
        if (!backStack.popDestination()) onExit()
        showMore = false
    }

    SideEffect { onDarkThemeChanged(darkTheme) }
    LaunchedEffect(uiState.pendingNavigation?.id) {
        uiState.pendingNavigation?.let { event ->
            backStack.navigateTo(event.destination)
            showMore = false
            onAction(DashboardAction.NavigationHandled(event.id))
        }
    }
    LaunchedEffect(linkVersion) {
        if (linkVersion > 0) {
            backStack.openLink(initialLink)
            showMore = false
        }
    }

    MasterFlowTheme(darkTheme = darkTheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if ((destination == AppDestination.Home && uiState.status in listOf(DashboardStatus.CONTENT, DashboardStatus.EMPTY_SEARCH)) ||
                    destination == AppDestination.Schedule) {
                    FlowBottomBar(
                        selectedDestination = destination,
                        onNavigate = { onAction(DashboardAction.OpenScreen(it)) },
                        onMore = { showMore = true }
                    )
                }
            }
        ) { padding ->
            val screenModifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
            NavDisplay(
                backStack = backStack,
                modifier = screenModifier,
                onBack = goBack,
                entryProvider = entryProvider {
                    entry<AppDestination.Home> {
                        when (uiState.status) {
                            DashboardStatus.LOADING -> FlowStateScreen(AppLinks.LOADING, onExit, {})
                            DashboardStatus.ERROR -> FlowStateScreen(
                                AppLinks.ERROR, onExit, { onAction(DashboardAction.Retry) },
                                timeoutError = uiState.timedOut
                            )
                            DashboardStatus.EMPTY -> FlowStateScreen(
                                AppLinks.EMPTY, onExit,
                                { onAction(DashboardAction.OpenScreen(AppDestination.NewAppointment)) }
                            )
                            DashboardStatus.CONTENT, DashboardStatus.EMPTY_SEARCH -> MainScreen(
                                uiState = uiState,
                                onAction = onAction,
                                modifier = Modifier.fillMaxSize().testTag("home-screen"),
                                onShare = onShare,
                                onToggleTheme = {
                                    themeMode = if (darkTheme) "light" else "dark"
                                    onThemeChanged(themeMode)
                                },
                                darkTheme = darkTheme,
                                listState = homeListState
                            )
                        }
                    }
                    entry<AppDestination.Schedule> {
                        ScheduleScreen(uiState, onAction, goBack,
                            Modifier.fillMaxSize().testTag("schedule-screen"), scheduleListState)
                    }
                    entry<AppDestination.Detail> { key ->
                        AppointmentDetailScreen(key.appointmentId, uiState, goBack,
                            { onAction(DashboardAction.Retry) })
                    }
                    entry<AppDestination.Clients> {
                        Box(Modifier.fillMaxSize().testTag("placeholder-clients")) {
                            FlowStateScreen(AppLinks.EMPTY, goBack, {}, showPrimaryAction = false)
                        }
                    }
                    entry<AppDestination.Services> {
                        Box(Modifier.fillMaxSize().testTag("placeholder-services")) {
                            FlowStateScreen(AppLinks.EMPTY, goBack, {}, showPrimaryAction = false)
                        }
                    }
                    entry<AppDestination.NewAppointment> {
                        Box(Modifier.fillMaxSize().testTag("placeholder-new-appointment")) {
                            FlowStateScreen(AppLinks.EMPTY, goBack, {}, showPrimaryAction = false)
                        }
                    }
                    entry<AppDestination.InvalidLink> {
                        FlowStateScreen(AppLinks.INVALID, goBack, goHome)
                    }
                }
            )
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
