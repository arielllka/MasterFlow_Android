package com.ariellka.masterflow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private var key = 0
    private val anna = Appointment(1, "Анна К.", "Услуга", LocalTime.of(14, 30), 60, 5)
    private val maria = Appointment(2, "Мария С.", "Услуга", LocalTime.of(15, 30), 60, 8)

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    private fun model(
        handle: SavedStateHandle = SavedStateHandle(),
        loader: suspend () -> List<Appointment> = { listOf(maria, anna) },
        delayMillis: Long = 0,
        timeout: Long = 10_000
    ): DashboardViewModel = DashboardViewModel(handle, loader, delayMillis, timeout).also {
        store.put("test-${key++}", it)
    }

    @Test fun loadingPublishesSortedContentAndCompleteSummary() = runTest(dispatcher) {
        val vm = model(delayMillis = 100)
        vm.loadOnce()
        runCurrent()
        assertEquals(DashboardStatus.LOADING, vm.uiState.value.status)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertEquals(DashboardStatus.CONTENT, state.status)
        assertEquals(listOf(anna, maria), state.appointments)
        assertEquals(anna, state.nextAppointment)
        assertEquals(2, state.appointmentCount)
        assertEquals(13, state.totalRevenue)
    }

    @Test fun noRecordsProduceAnEmptySnapshot() = runTest(dispatcher) {
        val vm = model(loader = { emptyList() })
        vm.loadOnce()
        advanceUntilIdle()
        assertEquals(DashboardStatus.EMPTY, vm.uiState.value.status)
        assertEquals(0, vm.uiState.value.totalRevenue)
        assertNull(vm.uiState.value.nextAppointment)
    }

    @Test fun exceptionsProduceAnError() = runTest(dispatcher) {
        val vm = model(loader = { error("Source is unavailable") })
        vm.refresh()
        advanceUntilIdle()
        assertEquals(DashboardStatus.ERROR, vm.uiState.value.status)
        assertFalse(vm.uiState.value.timedOut)
    }

    @Test fun timeoutPreservesTheQueryAndRetryRecovers() = runTest(dispatcher) {
        var slow = true
        val vm = model(loader = { if (slow) delay(5_000); listOf(anna, maria) }, timeout = 200)
        vm.onAction(DashboardAction.QueryChanged("Анна"))
        vm.refresh()
        advanceTimeBy(201)
        runCurrent()
        assertEquals(DashboardStatus.ERROR, vm.uiState.value.status)
        assertTrue(vm.uiState.value.timedOut)
        slow = false
        vm.onAction(DashboardAction.Retry)
        advanceUntilIdle()
        assertEquals(DashboardStatus.CONTENT, vm.uiState.value.status)
        assertEquals("Анна", vm.uiState.value.searchQuery)
        assertEquals(listOf(anna), vm.uiState.value.scheduleAppointments)
    }

    @Test fun nameSearchIgnoresCaseAndSurroundingSpacesWithoutChangingTotals() = runTest(dispatcher) {
        val vm = model()
        vm.loadOnce()
        advanceUntilIdle()
        vm.onAction(DashboardAction.QueryChanged("  аНнА  "))
        assertEquals(listOf(anna), vm.uiState.value.scheduleAppointments)
        assertEquals(2, vm.uiState.value.appointmentCount)
        assertEquals(13, vm.uiState.value.totalRevenue)
        vm.onAction(DashboardAction.QueryChanged("Unknown"))
        assertTrue(vm.uiState.value.scheduleAppointments.isEmpty())
        assertEquals(DashboardStatus.CONTENT, vm.uiState.value.status)
        vm.onAction(DashboardAction.ClearQuery)
        assertEquals(listOf(anna, maria), vm.uiState.value.scheduleAppointments)
    }

    @Test fun restoredInputIsAppliedToNewlyLoadedData() = runTest(dispatcher) {
        val handle = SavedStateHandle(mapOf(DashboardViewModel.SEARCH_QUERY_KEY to "Мария"))
        val vm = model(handle = handle, delayMillis = 100)
        assertEquals("Мария", vm.uiState.value.searchQuery)
        vm.loadOnce()
        advanceUntilIdle()
        assertEquals(listOf(maria), vm.uiState.value.scheduleAppointments)
        vm.onAction(DashboardAction.QueryChanged("Анна"))
        assertEquals("Анна", handle.get<String>(DashboardViewModel.SEARCH_QUERY_KEY))
        assertEquals(setOf(DashboardViewModel.SEARCH_QUERY_KEY), handle.keys())
    }

    @Test fun typingWhileLoadingUsesTheLatestQuery() = runTest(dispatcher) {
        val vm = model(delayMillis = 200)
        vm.loadOnce()
        runCurrent()
        vm.onAction(DashboardAction.QueryChanged("Мария"))
        advanceUntilIdle()
        assertEquals(listOf(maria), vm.uiState.value.scheduleAppointments)
    }

    @Test fun navigationNeedsAMatchingAcknowledgement() = runTest(dispatcher) {
        val vm = model()
        vm.loadOnce()
        advanceUntilIdle()
        vm.onAction(DashboardAction.OpenScreen(AppLinks.SCHEDULE))
        val first = vm.uiState.value.pendingNavigation!!
        assertEquals(AppLinks.SCHEDULE, first.destination)
        vm.onAction(DashboardAction.OpenAppointment(anna.id))
        val second = vm.uiState.value.pendingNavigation!!
        assertTrue(second.id > first.id)
        assertEquals(AppLinks.taskDestination(anna.id), second.destination)
        vm.onAction(DashboardAction.NavigationHandled(first.id))
        assertEquals(second, vm.uiState.value.pendingNavigation)
        vm.onAction(DashboardAction.NavigationHandled(second.id))
        assertNull(vm.uiState.value.pendingNavigation)
        vm.onAction(DashboardAction.OpenAppointment(999))
        assertNull(vm.uiState.value.pendingNavigation)
    }

    @Test fun repeatedContainerEntryDoesNotReloadAnInitializedViewModel() = runTest(dispatcher) {
        var loads = 0
        val vm = model(loader = { loads++; listOf(anna) })
        vm.loadOnce()
        advanceUntilIdle()
        vm.loadOnce()
        advanceUntilIdle()
        assertEquals(1, loads)
    }
}
