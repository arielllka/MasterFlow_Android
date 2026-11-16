package com.ariellka.masterflow

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ariellka.masterflow.ui.theme.MasterFlowTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScheduleTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private lateinit var launchIntent: Intent

    @Before fun rememberLaunchIntent() { launchIntent = Intent(compose.activity.intent) }
    @After fun restoreLaunchIntentForScenarioCleanup() {
        // Warm deep links replace Activity.intent; restore it for ActivityScenario teardown.
        compose.runOnUiThread { compose.activity.intent = launchIntent }
    }

    private fun waitFor(tag: String) {
        compose.waitUntil(15_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun bothViewAllButtonsTheTabAndDeepLinkOpenSchedule() {
        waitFor("home-screen")
        listOf("view-all-next", "view-all-schedule").forEach { tag ->
            if (!compose.onNodeWithTag(tag).isDisplayed()) {
                compose.onNodeWithTag("home-list").performScrollToNode(hasTestTag(tag))
            }
            compose.onNodeWithTag(tag).performClick()
            compose.onNodeWithTag("schedule-screen").assertIsDisplayed()
            compose.onNodeWithTag("back").performClick()
        }
        compose.onNodeWithTag("nav-schedule").performClick()
        compose.onNodeWithTag("schedule-screen").assertIsDisplayed()
        compose.onNodeWithTag("nav-home").performClick()
        compose.runOnUiThread {
            compose.activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppLinks.SCHEDULE_LINK))
                .setClass(compose.activity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
        waitFor("schedule-screen")
    }

    @Test fun nameSearchShowsNoMatchesAndClearRestoresTheList() {
        waitFor("home-screen")
        compose.onNodeWithTag("nav-schedule").performClick()
        compose.onNodeWithTag("schedule-search").performTextInput("Анна")
        compose.onNodeWithTag("schedule-search").performImeAction()
        compose.onNodeWithTag("appointment-1").assertExists()
        compose.onNodeWithTag("schedule-search").performTextReplacement("Несуществующий клиент")
        compose.onNodeWithTag("search-empty").assertExists()
        compose.onNodeWithTag("clear-search").performClick()
        compose.onNodeWithTag("schedule-search").assertTextContains("")
        compose.onNodeWithTag("schedule-appointments").assertExists()
    }

    @Test fun recreationKeepsSearchAndDoesNotRepeatTheHandledNavigation() {
        waitFor("home-screen")
        compose.onNodeWithTag("nav-schedule").performClick()
        compose.onNodeWithTag("schedule-search").performTextInput("Ан")
        compose.onNodeWithTag("schedule-search").performImeAction()
        compose.onNodeWithTag("appointment-1").performClick()
        compose.onNodeWithTag("task-1").assertIsDisplayed()
        compose.onNodeWithTag("back").performClick()
        compose.activityRule.scenario.recreate()
        waitFor("schedule-screen")
        compose.onNodeWithTag("schedule-search").assertTextContains("Ан")
        compose.onNodeWithTag("task-1").assertDoesNotExist()
    }

    @Test fun bottomIconsGrowWithFontScaleAndKeepAccessibleNames() {
        waitFor("home-screen")
        var fontScale by mutableFloatStateOf(1f)
        compose.runOnUiThread {
            compose.activity.setContent {
                val density = LocalDensity.current.density
                CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                    MasterFlowTheme { FlowBottomBar(onNavigate = {}) }
                }
            }
        }
        val normalWidth = compose.onNodeWithTag("nav-icon-home", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot.width
        compose.runOnUiThread { fontScale = 1.8f }
        val largeIcon = compose.onNodeWithTag("nav-icon-home", useUnmergedTree = true)
        val largeWidth = largeIcon.fetchSemanticsNode().boundsInRoot.width
        assertTrue(largeWidth > normalWidth)
        assertEquals(1.8f, largeWidth / normalWidth, 0.04f)
        largeIcon.assertContentDescriptionEquals(compose.activity.getString(R.string.home))
    }

    @Test fun launcherIntentDoesNotResetAnOpenSchedule() {
        waitFor("home-screen")
        compose.onNodeWithTag("nav-schedule").performClick()
        compose.onNodeWithTag("schedule-search").performTextInput("Ан")
        compose.onNodeWithTag("schedule-search").performImeAction()
        compose.runOnUiThread {
            compose.activity.startActivity(Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setClass(compose.activity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
        }
        compose.waitForIdle()
        compose.onNodeWithTag("schedule-screen").assertIsDisplayed()
        compose.onNodeWithTag("schedule-search").assertTextContains("Ан")
    }
}
