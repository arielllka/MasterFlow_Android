package com.ariellka.masterflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.ariellka.masterflow.ui.theme.FlowCorners
import com.ariellka.masterflow.ui.theme.FlowSpace

@Composable
fun FlowStateScreen(
    state: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    timeoutError: Boolean = false,
    showPrimaryAction: Boolean = true
) {
    val loading = state == AppLinks.LOADING
    val empty = state == AppLinks.EMPTY
    val invalid = state == AppLinks.INVALID
    val title = when {
        loading -> R.string.loading_title
        empty -> R.string.empty_title
        invalid -> R.string.invalid_title
        else -> R.string.error_title
    }
    val message = when {
        timeoutError -> R.string.timeout_message
        loading -> R.string.loading_message
        empty -> R.string.empty_message
        invalid -> R.string.invalid_message
        else -> R.string.error_message
    }
    Column(modifier.fillMaxSize().testTag("state-$state").padding(FlowSpace.page)) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                Modifier.widthIn(max = FlowSpace.contentWidth).fillMaxWidth().padding(vertical = FlowSpace.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FlowSpace.large)
            ) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary)
                Surface(
                    modifier = Modifier.size(FlowSpace.illustration), shape = FlowCorners.navigation,
                    color = if (loading || empty) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(painterResource(if (loading || empty) R.drawable.ic_calendar else R.drawable.ic_error), null,
                            modifier = Modifier.size(FlowSpace.touch),
                            tint = if (loading || empty) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                Text(stringResource(title), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading(); liveRegion = LiveRegionMode.Polite })
                Text(stringResource(message), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                } else if (showPrimaryAction) {
                    Button(
                        onClick = if (invalid) onBack else onRetry,
                        shape = FlowCorners.button,
                        modifier = Modifier.fillMaxWidth().heightIn(min = FlowSpace.button).testTag("state-action")
                    ) {
                        Text(stringResource(when { empty -> R.string.new_appointment; invalid -> R.string.go_home; else -> R.string.retry }))
                    }
                }
            }
        }
        if (!loading) BackButton(onBack)
    }
}
