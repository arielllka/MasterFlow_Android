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
    private fun awaitResult(model: DashboardViewModel): DashboardUiState {
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (model.uiState.value.status == DashboardStatus.LOADING && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(20)
        }
        return model.uiState.value
    }

    @Test fun successfulEmptyResultBecomesEmpty() {
        val model = DashboardViewModel(loadRecords = { emptyList() }, loadingDelayMillis = 0)
        InstrumentationRegistry.getInstrumentation().runOnMainSync { model.refresh() }
        assertEquals(DashboardStatus.EMPTY, awaitResult(model).status)
    }

    @Test fun slowLoadTimesOutAndRetryCanRecover() {
        var slow = true
        val model = DashboardViewModel(
            loadRecords = { if (slow) delay(5_000); appointments },
            loadingDelayMillis = 0, timeoutMillis = 200
        )
        InstrumentationRegistry.getInstrumentation().runOnMainSync { model.refresh() }
        val failure = awaitResult(model)
        assertEquals(DashboardStatus.ERROR, failure.status)
        assertTrue(failure.timedOut)
        InstrumentationRegistry.getInstrumentation().runOnMainSync { slow = false; model.refresh() }
        assertEquals(DashboardStatus.CONTENT, awaitResult(model).status)
    }

    @Test fun failedLoadBecomesError() {
        val model = DashboardViewModel(
            loadRecords = { throw IllegalStateException("Test load failure") }, loadingDelayMillis = 0
        )
        InstrumentationRegistry.getInstrumentation().runOnMainSync { model.refresh() }
        val failure = awaitResult(model)
        assertEquals(DashboardStatus.ERROR, failure.status)
        assertTrue(!failure.timedOut)
    }
}
