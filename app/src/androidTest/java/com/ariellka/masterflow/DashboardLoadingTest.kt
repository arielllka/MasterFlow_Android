package com.ariellka.masterflow

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardLoadingTest {
    private fun awaitResult(model: DashboardViewModel): DashboardState {
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (model.state == DashboardState.Loading && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(20)
        }
        return model.state
    }

    @Test fun successfulEmptyResultBecomesEmpty() {
        val model = DashboardViewModel(loadRecords = { emptyList() }, loadingDelayMillis = 0)
        InstrumentationRegistry.getInstrumentation().runOnMainSync { model.refresh() }
        assertEquals(DashboardState.Empty, awaitResult(model))
    }

    @Test fun slowLoadTimesOutAndRetryCanRecover() {
        var slow = true
        val model = DashboardViewModel(
            loadRecords = { if (slow) delay(5_000); appointments },
            loadingDelayMillis = 0, timeoutMillis = 200
        )
        InstrumentationRegistry.getInstrumentation().runOnMainSync { model.refresh() }
        assertEquals(DashboardState.Error(timedOut = true), awaitResult(model))
        InstrumentationRegistry.getInstrumentation().runOnMainSync { slow = false; model.refresh() }
        assertTrue(awaitResult(model) is DashboardState.Content)
    }

    @Test fun failedLoadBecomesError() {
        val model = DashboardViewModel(
            loadRecords = { throw IllegalStateException("Test load failure") }, loadingDelayMillis = 0
        )
        InstrumentationRegistry.getInstrumentation().runOnMainSync { model.refresh() }
        assertEquals(DashboardState.Error(), awaitResult(model))
    }
}
