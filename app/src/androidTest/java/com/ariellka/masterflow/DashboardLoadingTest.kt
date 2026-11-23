package com.ariellka.masterflow

import android.os.SystemClock
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardLoadingTest {
    private val store = ViewModelStore()
    private var modelId = 0

    @After fun tearDown() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { store.clear() }
    }

    private fun start(model: DashboardViewModel) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            store.put("loading-${modelId++}", model)
            model.viewModelScope.launch { model.uiState.collect {} }
            model.refresh()
        }
    }

    private fun awaitResult(model: DashboardViewModel, expected: DashboardStatus): DashboardUiState {
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (model.uiState.value.status != expected && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(20)
        }
        return model.uiState.value
    }

    @Test fun successfulEmptyResultBecomesEmpty() {
        val model = DashboardViewModel(loadRecords = { emptyList() }, loadingDelayMillis = 0)
        start(model)
        assertEquals(DashboardStatus.EMPTY, awaitResult(model, DashboardStatus.EMPTY).status)
    }

    @Test fun slowLoadTimesOutAndRetryCanRecover() {
        var slow = true
        val model = DashboardViewModel(
            loadRecords = { if (slow) delay(5_000); appointments },
            loadingDelayMillis = 0, timeoutMillis = 200
        )
        start(model)
        val failure = awaitResult(model, DashboardStatus.ERROR)
        assertEquals(DashboardStatus.ERROR, failure.status)
        assertTrue(failure.timedOut)
        InstrumentationRegistry.getInstrumentation().runOnMainSync { slow = false; model.refresh() }
        assertEquals(DashboardStatus.CONTENT, awaitResult(model, DashboardStatus.CONTENT).status)
    }

    @Test fun failedLoadBecomesError() {
        val model = DashboardViewModel(
            loadRecords = { throw IllegalStateException("Test load failure") }, loadingDelayMillis = 0
        )
        start(model)
        val failure = awaitResult(model, DashboardStatus.ERROR)
        assertEquals(DashboardStatus.ERROR, failure.status)
        assertTrue(!failure.timedOut)
    }
}
