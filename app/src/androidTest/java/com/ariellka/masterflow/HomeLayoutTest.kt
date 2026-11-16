package com.ariellka.masterflow

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ariellka.masterflow.ui.theme.MasterFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test fun thirdRowAppearsWhenSpaceIsAvailableAndHidesWhenItIsNot() {
        var height by mutableStateOf(1100.dp)
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                MasterFlowTheme {
                    MainScreen(previewDashboardState, {}, Modifier.requiredSize(412.dp, height))
                }
            }
        }
        compose.onNodeWithTag("open-appointment-3").assertIsDisplayed()
        compose.runOnIdle { height = 650.dp }
        compose.onNodeWithTag("open-appointment-3").assertIsNotDisplayed()
        compose.runOnIdle { height = 1100.dp }
        compose.onNodeWithTag("open-appointment-3").assertIsDisplayed()
    }

    @Test fun aTallViewportCanShowMoreThanThreeRows() {
        val records = (1..6).map { previewDashboardState.appointments.first().copy(id = it) }
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                MasterFlowTheme {
                    MainScreen(previewDashboardState.copy(appointments = records,
                        appointmentCount = records.size, totalRevenue = records.sumOf { it.price }), {},
                        Modifier.requiredSize(412.dp, 2000.dp))
                }
            }
        }
        compose.onNodeWithTag("open-appointment-6").assertIsDisplayed()
    }
}
