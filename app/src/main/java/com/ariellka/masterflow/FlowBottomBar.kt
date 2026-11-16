package com.ariellka.masterflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.ariellka.masterflow.ui.theme.FlowCorners
import com.ariellka.masterflow.ui.theme.FlowSpace

@Composable
internal fun FlowBottomBar(onNavigate: (String) -> Unit) {
    val tabs = listOf(
        Triple(AppLinks.HOME, R.string.home, R.drawable.ic_home),
        Triple("schedule", R.string.schedule, R.drawable.ic_calendar),
        Triple("clients", R.string.clients, R.drawable.ic_people),
        Triple("services", R.string.services, R.drawable.ic_tools),
        Triple("more", R.string.more, R.drawable.ic_menu)
    )
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = FlowSpace.page, vertical = FlowSpace.small),
        contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.widthIn(max = FlowSpace.contentWidth).fillMaxWidth(),
            shape = FlowCorners.navigation, color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Row(Modifier.padding(FlowSpace.small), verticalAlignment = Alignment.CenterVertically) {
                tabs.forEach { (route, title, icon) ->
                    Column(
                        Modifier.weight(1f).heightIn(min = FlowSpace.button)
                            .selectable(route == AppLinks.HOME, role = Role.Tab) { onNavigate(route) }
                            .testTag("nav-$route").padding(vertical = FlowSpace.tiny),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(FlowSpace.tiny)
                    ) {
                        Icon(painterResource(icon), null)
                        Text(stringResource(title), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
