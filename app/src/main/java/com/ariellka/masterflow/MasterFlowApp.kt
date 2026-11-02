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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.ariellka.masterflow.ui.theme.FlowSpace
import com.ariellka.masterflow.ui.theme.MasterFlowTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterFlowApp(
    initialLink: String?,
    linkVersion: Int,
    onShare: () -> Unit,
    dashboardState: DashboardState,
    onRefresh: () -> Unit,
    initialThemeMode: String = "system",
    onThemeChanged: (String) -> Unit = {},
    onDarkThemeChanged: (Boolean) -> Unit = {},
    onExit: () -> Unit = {}
) {
    var destination by rememberSaveable { mutableStateOf(AppLinks.resolve(initialLink)) }
    var showMore by rememberSaveable { mutableStateOf(false) }
    var themeMode by rememberSaveable { mutableStateOf(initialThemeMode) }
    val darkTheme = if (themeMode == "system") isSystemInDarkTheme() else themeMode == "dark"
    val homeListState = rememberLazyListState()
    val scheduleListState = rememberLazyListState()
    val goHome = { destination = AppLinks.HOME; showMore = false }

    SideEffect { onDarkThemeChanged(darkTheme) }
    LaunchedEffect(linkVersion) {
        if (linkVersion > 0) {
            destination = AppLinks.resolve(initialLink)
            showMore = false
        }
    }
    BackHandler(enabled = destination != AppLinks.HOME, onBack = goHome)

    MasterFlowTheme(darkTheme = darkTheme) {
        val visibleDestination = if (destination == AppLinks.HOME) {
            when (dashboardState) {
                DashboardState.Loading -> AppLinks.LOADING
                DashboardState.Empty -> AppLinks.EMPTY
                is DashboardState.Error -> AppLinks.ERROR
                is DashboardState.Content -> AppLinks.HOME
            }
        } else destination

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (visibleDestination == AppLinks.HOME) {
                    FlowBottomBar { target ->
                        if (target == "more") showMore = true else destination = target
                    }
                }
            }
        ) { padding ->
            val screenModifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
            val currentAppointment = appointments.find { AppLinks.taskDestination(it.id) == destination }
            when {
                visibleDestination == AppLinks.LOADING -> FlowStateScreen(AppLinks.LOADING, goHome, {}, screenModifier)
                visibleDestination == AppLinks.ERROR -> FlowStateScreen(
                    AppLinks.ERROR, onExit, onRefresh, screenModifier,
                    timeoutError = (dashboardState as? DashboardState.Error)?.timedOut == true
                )
                visibleDestination == AppLinks.EMPTY -> FlowStateScreen(
                    AppLinks.EMPTY, onExit, { destination = "new-appointment" }, screenModifier
                )
                destination == AppLinks.HOME && dashboardState is DashboardState.Content -> MainScreen(
                    records = dashboardState.records,
                    onAppointmentClick = { destination = AppLinks.taskDestination(it.id) },
                    modifier = screenModifier.testTag("home-screen"),
                    onNavigate = { destination = it }, onShare = onShare,
                    onToggleTheme = {
                        themeMode = if (darkTheme) "light" else "dark"
                        onThemeChanged(themeMode)
                    },
                    darkTheme = darkTheme, listState = homeListState, scheduleState = scheduleListState
                )
                destination == AppLinks.INVALID ->
                    FlowStateScreen(AppLinks.INVALID, goHome, goHome, screenModifier)
                currentAppointment != null -> Box(
                    screenModifier.testTag("task-${currentAppointment.id}").semantics {
                        contentDescription = currentAppointment.clientName
                    }
                ) {
                    FlowStateScreen(AppLinks.EMPTY, goHome, {}, showPrimaryAction = false)
                }
                else -> Box(screenModifier.testTag("placeholder-$destination")) {
                    FlowStateScreen(AppLinks.EMPTY, goHome, {}, showPrimaryAction = false)
                }
            }
        }
        if (showMore) {
            ModalBottomSheet(onDismissRequest = { showMore = false }, containerColor = MaterialTheme.colorScheme.background) {
                Column(Modifier.fillMaxWidth().padding(FlowSpace.page),
                    verticalArrangement = Arrangement.spacedBy(FlowSpace.small)) {
                    Text(stringResource(R.string.more), style = MaterialTheme.typography.titleLarge)
                    TextButton(
                        onClick = { showMore = false; onRefresh() },
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
