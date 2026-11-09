package com.ariellka.masterflow

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private lateinit var launchIntent: Intent

    @Before fun rememberLaunchIntent() { launchIntent = Intent(compose.activity.intent) }
    @After fun restoreLaunchIntentForScenarioCleanup() {
        // ActivityScenario matches lifecycle callbacks against its original launch Intent.
        compose.runOnUiThread { compose.activity.intent = launchIntent }
    }

    private fun waitForHome() {
        compose.waitUntil(15_000) {
            compose.onAllNodesWithTag("home-screen").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun sendLink(link: String) {
        compose.runOnUiThread {
            compose.activity.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    .setClass(compose.activity, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
        val expectedTag = when (val route = AppLinks.resolve(link)) {
            AppLinks.HOME -> "home-screen"
            AppLinks.SCHEDULE -> "schedule-screen"
            AppLinks.INVALID -> "state-invalid"
            else -> route.replace('/', '-')
        }
        compose.waitUntil(15_000) {
            compose.onAllNodesWithTag(expectedTag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun scheduleRowsOpenEmptyScreensAndReturnToSchedule() {
        waitForHome()
        compose.onNodeWithTag("nav-schedule").performClick()
        listOf(1, 2, 42).forEach { taskId ->
            compose.onNodeWithTag("schedule-appointments").performScrollToNode(hasTestTag("appointment-$taskId"))
            compose.onNodeWithTag("appointment-$taskId").performClick()
            compose.onNodeWithTag("task-$taskId").assertIsDisplayed()
            compose.onNodeWithTag("back").performClick()
            compose.onNodeWithTag("schedule-screen").assertIsDisplayed()
        }
    }

    @Test fun repeatedWarmDeepLinksCanOpenTasksHomeAndInvalidState() {
        waitForHome()
        listOf(1, 2, 42).forEach { id ->
            sendLink(AppLinks.taskLink(id))
            compose.onNodeWithTag("task-$id").assertIsDisplayed()
        }
        sendLink(AppLinks.HOME_LINK)
        compose.onNodeWithTag("home-screen").assertIsDisplayed()
        sendLink("masterflow://task/999")
        compose.onNodeWithTag("state-invalid").assertIsDisplayed()
        compose.onNodeWithTag("back").performClick()
        compose.onNodeWithTag("home-screen").assertIsDisplayed()
    }

    @Test fun refreshShowsLoadingThenReturnsToContent() {
        waitForHome()
        compose.onNodeWithTag("nav-more").performClick()
        compose.onNodeWithTag("refresh").performClick()
        compose.onNodeWithTag("state-loading").assertIsDisplayed()
        waitForHome()
    }

    @Test fun emptyAndErrorResultsOfferTheCorrectActions() {
        waitForHome()
        var result by mutableStateOf(DashboardUiState(status = DashboardStatus.EMPTY))
        var eventId = 0L
        compose.runOnUiThread {
            compose.activity.setContent {
                MasterFlowApp(null, 0, {}, result, { action ->
                    result = when (action) {
                        is DashboardAction.OpenScreen -> result.copy(pendingNavigation = DashboardNavigation(++eventId, action.destination))
                        is DashboardAction.NavigationHandled -> result.copy(pendingNavigation = null)
                        DashboardAction.Retry -> previewDashboardState
                        else -> result
                    }
                })
            }
        }
        compose.onNodeWithTag("state-empty").assertIsDisplayed()
        compose.onNodeWithTag("state-action").performClick()
        compose.onNodeWithTag("placeholder-new-appointment").assertIsDisplayed()
        compose.onNodeWithTag("back").performClick()
        compose.runOnUiThread { result = DashboardUiState(status = DashboardStatus.ERROR, timedOut = true) }
        compose.onNodeWithTag("state-error").assertIsDisplayed()
        compose.onNodeWithTag("state-action").performClick()
        waitForHome()
    }
}
