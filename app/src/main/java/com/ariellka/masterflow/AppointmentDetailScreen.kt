package com.ariellka.masterflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.ariellka.masterflow.ui.theme.FlowCorners
import com.ariellka.masterflow.ui.theme.FlowSpace
import java.time.format.DateTimeFormatter

@Composable
fun AppointmentDetailScreen(
    appointmentId: Int,
    uiState: DashboardUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appointment = uiState.appointmentsById[appointmentId]
    val locale = LocalConfiguration.current.locales[0]
    Box(modifier.fillMaxSize().testTag("detail-$appointmentId"), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = FlowSpace.contentWidth).fillMaxSize().padding(FlowSpace.page),
            verticalArrangement = Arrangement.spacedBy(FlowSpace.large)
        ) {
            Text(stringResource(R.string.appointment_details), style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() })
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (uiState.status) {
                    DashboardStatus.LOADING -> FlowStateScreen(
                        AppLinks.LOADING, onBack, {}, showBackButton = false,
                        titleResId = R.string.detail_loading_title,
                        messageResId = R.string.detail_loading_message
                    )
                    DashboardStatus.ERROR -> FlowStateScreen(
                        AppLinks.ERROR, onBack, onRetry, timeoutError = uiState.timedOut,
                        showBackButton = false, titleResId = R.string.detail_error_title,
                        messageResId = if (uiState.timedOut) R.string.timeout_message else R.string.detail_error_message
                    )
                    DashboardStatus.EMPTY -> FlowStateScreen(
                        AppLinks.EMPTY, onBack, {}, showPrimaryAction = false, showBackButton = false
                    )
                    DashboardStatus.CONTENT -> {
                        if (appointment == null) {
                            FlowStateScreen(AppLinks.NOT_FOUND, onBack, {},
                                showPrimaryAction = false, showBackButton = false)
                        } else {
                            LazyColumn(Modifier.fillMaxSize().testTag("detail-content"),
                                verticalArrangement = Arrangement.spacedBy(FlowSpace.large)) {
                                item {
                                    Surface(shape = FlowCorners.card, color = MaterialTheme.colorScheme.surfaceVariant) {
                                        Column(Modifier.fillMaxWidth().padding(FlowSpace.page),
                                            verticalArrangement = Arrangement.spacedBy(FlowSpace.large)) {
                                            Row(verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(FlowSpace.medium)) {
                                                Box(Modifier.size(FlowSpace.touch)
                                                    .background(MaterialTheme.colorScheme.background, CircleShape),
                                                    contentAlignment = Alignment.Center) {
                                                    Icon(painterResource(R.drawable.ic_person), null)
                                                }
                                                Column(Modifier.weight(1f)) {
                                                    Text(appointment.clientName, style = MaterialTheme.typography.titleMedium,
                                                        modifier = Modifier.testTag("detail-client-name"))
                                                    Text(stringResource(R.string.appointment_number, appointment.id),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.testTag("detail-appointment-id"))
                                                }
                                            }
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                            DetailField(stringResource(R.string.detail_service), appointment.serviceName)
                                            DetailField(stringResource(R.string.detail_date),
                                                uiState.date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale)))
                                            DetailField(stringResource(R.string.detail_time),
                                                appointment.startTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                                            DetailField(stringResource(R.string.detail_duration),
                                                stringResource(R.string.duration_minutes, appointment.durationMinutes))
                                            DetailField(stringResource(R.string.detail_price),
                                                "${uiState.currency}${appointment.price}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            BackButton(onBack)
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(FlowSpace.tiny)) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
