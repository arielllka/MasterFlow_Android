package com.ariellka.masterflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import com.ariellka.masterflow.ui.theme.FlowCorners
import com.ariellka.masterflow.ui.theme.FlowSpace
import java.time.format.DateTimeFormatter


@Composable
fun ScheduleScreen(
    uiState: DashboardUiState,
    onAction: (DashboardAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val locale = LocalConfiguration.current.locales[0]
    val date = uiState.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))
        .replaceFirstChar { it.titlecase(locale) }

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = FlowSpace.contentWidth).fillMaxSize().imePadding().padding(FlowSpace.page),
            verticalArrangement = Arrangement.spacedBy(FlowSpace.medium)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("back")) {
                    Icon(painterResource(R.drawable.ic_back), stringResource(R.string.back))
                }
                Text(stringResource(R.string.schedule), style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f).semantics { heading() })
                IconButton(onClick = { onAction(DashboardAction.Refresh) }, modifier = Modifier.testTag("schedule-refresh")) {
                    Icon(painterResource(R.drawable.ic_refresh), stringResource(R.string.refresh))
                }
            }
            Text(stringResource(R.string.today_schedule), style = MaterialTheme.typography.titleMedium)
            Text(date, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onAction(DashboardAction.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth().testTag("schedule-search"),
                label = { Text(stringResource(R.string.search_by_client)) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onAction(DashboardAction.ClearQuery) }, modifier = Modifier.testTag("clear-search")) {
                            Icon(painterResource(R.drawable.ic_close), stringResource(R.string.clear_search))
                        }
                    }
                },
                singleLine = true,
                shape = FlowCorners.button,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() })
            )

            when (uiState.status) {
                DashboardStatus.LOADING -> FlowStateScreen(
                    AppLinks.LOADING, onBack, {}, Modifier.weight(1f), showBackButton = false
                )
                DashboardStatus.ERROR -> FlowStateScreen(
                    AppLinks.ERROR, onBack, { onAction(DashboardAction.Retry) }, Modifier.weight(1f),
                    timeoutError = uiState.timedOut, showBackButton = false
                )
                DashboardStatus.EMPTY -> FlowStateScreen(
                    AppLinks.EMPTY, onBack, {}, Modifier.weight(1f),
                    showPrimaryAction = false, showBackButton = false
                )
                DashboardStatus.CONTENT -> {
                    if (uiState.scheduleAppointments.isEmpty()) {
                        Column(
                            Modifier.weight(1f).fillMaxWidth().testTag("search-empty"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(FlowSpace.medium, Alignment.CenterVertically)
                        ) {
                            Text(stringResource(R.string.search_no_results), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.search_no_results_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth().testTag("schedule-appointments"),
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(FlowSpace.medium),
                            contentPadding = PaddingValues(bottom = FlowSpace.small)
                        ) {
                            items(uiState.scheduleAppointments, key = { it.id }) { appointment ->
                                Surface(
                                    onClick = { onAction(DashboardAction.OpenAppointment(appointment.id)) },
                                    modifier = Modifier.fillMaxWidth().testTag("appointment-${appointment.id}"),
                                    shape = FlowCorners.card,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(Modifier.padding(FlowSpace.page),
                                        verticalArrangement = Arrangement.spacedBy(FlowSpace.small)) {
                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text(appointment.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                                style = MaterialTheme.typography.labelLarge)
                                            Spacer(Modifier.weight(1f))
                                            Text("${uiState.currency}${appointment.price}", style = MaterialTheme.typography.labelLarge)
                                        }
                                        AppointmentRow(appointment)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
