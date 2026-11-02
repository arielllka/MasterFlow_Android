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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

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
            AppLinks.INVALID -> "state-invalid"
            else -> route.replace('/', '-')
        }
        compose.waitUntil(15_000) {
            compose.onAllNodesWithTag(expectedTag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun scheduleRowsOpenBlankScreensAndReturn() {
        waitForHome()
        listOf(1 to 1, 2 to 2, 3 to 42).forEach { (row, taskId) ->
            compose.onNodeWithTag("home-list").performScrollToNode(hasTestTag("schedule-section"))
            compose.onNodeWithTag("schedule-list").performScrollToNode(hasTestTag("open-appointment-$row"))
            compose.onNodeWithTag("open-appointment-$row").performClick()
            compose.onNodeWithTag("task-$taskId").assertIsDisplayed()
            compose.onNodeWithTag("back").performClick()
            compose.onNodeWithTag("home-screen").assertIsDisplayed()
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
        var result by mutableStateOf<DashboardState>(DashboardState.Empty)
        compose.runOnUiThread {
            compose.activity.setContent {
                MasterFlowApp(null, 0, {}, result, { result = DashboardState.Content(appointments) })
            }
        }
        compose.onNodeWithTag("state-empty").assertIsDisplayed()
        compose.onNodeWithTag("state-action").performClick()
        compose.onNodeWithTag("placeholder-new-appointment").assertIsDisplayed()
        compose.onNodeWithTag("back").performClick()
        compose.runOnUiThread { result = DashboardState.Error(timedOut = true) }
        compose.onNodeWithTag("state-error").assertIsDisplayed()
        compose.onNodeWithTag("state-action").performClick()
        waitForHome()
    }
}
