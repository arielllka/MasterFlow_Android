package com.ariellka.masterflow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private fun TestScope.model(
        handle: SavedStateHandle = SavedStateHandle(),
        loader: suspend () -> List<Appointment> = { listOf(maria, anna) },
        delayMillis: Long = 0,
        timeout: Long = 10_000,
        servicesLoader: suspend () -> List<Service> = { services }
    ): DashboardViewModel = DashboardViewModel(handle, loader, delayMillis, timeout, servicesLoader).also { vm ->
        store.put("test-${key++}", vm)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
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
        assertEquals(400L, testScheduler.currentTime)
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

        var failServices = true
        var recordsCancelled = false
        val catalogModel = model(
            loader = {
                try {
                    delay(1_000)
                    listOf(anna)
                } finally {
                    recordsCancelled = !currentCoroutineContext().isActive
                }
            },
            servicesLoader = { if (failServices) error("Catalog is unavailable"); services }
        )
        catalogModel.loadOnce()
        advanceUntilIdle()
        assertEquals(DashboardStatus.ERROR, catalogModel.uiState.value.status)
        assertTrue(recordsCancelled)
        failServices = false
        catalogModel.onAction(DashboardAction.Retry)
        advanceUntilIdle()
        assertEquals(DashboardStatus.CONTENT, catalogModel.uiState.value.status)
    }

    @Test fun timeoutPreservesTheQueryAndRetryRecovers() = runTest(dispatcher) {
        var slow = true
        val vm = model(loader = { if (slow) delay(5_000); listOf(anna, maria) }, timeout = 200)
        vm.onAction(DashboardAction.QueryChanged("Анна"))
        vm.refresh()
        advanceTimeBy(DashboardViewModel.SEARCH_DEBOUNCE_MILLIS + 201)
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
        var loads = 0
        val vm = model(loader = { loads++; listOf(maria, anna) })
        vm.loadOnce()
        advanceUntilIdle()
        vm.onAction(DashboardAction.QueryChanged("  аНнА  "))
        advanceUntilIdle()
        assertEquals(listOf(anna), vm.uiState.value.scheduleAppointments)
        assertEquals(2, vm.uiState.value.appointmentCount)
        assertEquals(13, vm.uiState.value.totalRevenue)
        val beforeRepeat = loads
        vm.onAction(DashboardAction.QueryChanged("анна"))
        advanceUntilIdle()
        assertEquals(beforeRepeat, loads)
        vm.onAction(DashboardAction.QueryChanged("Unknown"))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.scheduleAppointments.isEmpty())
        assertEquals(DashboardStatus.EMPTY_SEARCH, vm.uiState.value.status)
        vm.onAction(DashboardAction.ClearQuery)
        advanceUntilIdle()
        assertEquals(listOf(anna, maria), vm.uiState.value.scheduleAppointments)
        vm.onAction(DashboardAction.ServiceChanged(2))
        advanceUntilIdle()
        assertEquals(DashboardStatus.EMPTY_SEARCH, vm.uiState.value.status)
        assertEquals(2, vm.uiState.value.appointmentCount)
        vm.onAction(DashboardAction.ClearFilters)
        advanceUntilIdle()
        assertEquals(DashboardStatus.CONTENT, vm.uiState.value.status)
        assertEquals(0, vm.uiState.value.selectedServiceId)
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
        vm.onAction(DashboardAction.ServiceChanged(2))
        assertEquals(2, handle.get<Int>(DashboardViewModel.SERVICE_FILTER_KEY))
        assertEquals(setOf(DashboardViewModel.SEARCH_QUERY_KEY, DashboardViewModel.SERVICE_FILTER_KEY), handle.keys())
    }

    @Test fun typingWhileLoadingUsesTheLatestQuery() = runTest(dispatcher) {
        var loads = 0
        var cancellations = 0
        val vm = model(loader = {
            loads++
            try {
                delay(1_000)
                listOf(anna, maria)
            } catch (cancelled: CancellationException) {
                cancellations++
                throw cancelled
            }
        })
        vm.loadOnce()
        advanceTimeBy(DashboardViewModel.SEARCH_DEBOUNCE_MILLIS)
        runCurrent()
        vm.onAction(DashboardAction.QueryChanged("М"))
        runCurrent()
        assertEquals("М", vm.uiState.value.searchQuery)
        advanceTimeBy(100)
        vm.onAction(DashboardAction.QueryChanged("Ма"))
        runCurrent()
        advanceTimeBy(100)
        vm.onAction(DashboardAction.QueryChanged("Мария"))
        runCurrent()
        advanceTimeBy(DashboardViewModel.SEARCH_DEBOUNCE_MILLIS - 1)
        assertEquals(1, loads)
        assertEquals(0, cancellations)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, loads)
        assertEquals(1, cancellations)
        assertEquals(DashboardStatus.LOADING, vm.uiState.value.status)
        advanceUntilIdle()
        assertEquals(DashboardStatus.CONTENT, vm.uiState.value.status)
        assertEquals(listOf(maria), vm.uiState.value.scheduleAppointments)
    }

    @Test fun navigationNeedsAMatchingAcknowledgement() = runTest(dispatcher) {
        val vm = model()
        vm.loadOnce()
        advanceUntilIdle()
        vm.onAction(DashboardAction.OpenScreen(AppDestination.Schedule))
        runCurrent()
        val first = vm.uiState.value.pendingNavigation!!
        assertEquals(AppDestination.Schedule, first.destination)
        vm.onAction(DashboardAction.OpenAppointment(anna.id))
        runCurrent()
        val second = vm.uiState.value.pendingNavigation!!
        assertTrue(second.id > first.id)
        assertEquals(AppDestination.Detail(anna.id), second.destination)
        vm.onAction(DashboardAction.NavigationHandled(first.id))
        runCurrent()
        assertEquals(second, vm.uiState.value.pendingNavigation)
        vm.onAction(DashboardAction.NavigationHandled(second.id))
        runCurrent()
        assertNull(vm.uiState.value.pendingNavigation)
        vm.onAction(DashboardAction.OpenAppointment(999))
        runCurrent()
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
