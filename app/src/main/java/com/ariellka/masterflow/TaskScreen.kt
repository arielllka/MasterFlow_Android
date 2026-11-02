package com.ariellka.masterflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.ariellka.masterflow.ui.theme.FlowCorners
import com.ariellka.masterflow.ui.theme.FlowSpace

@Composable
fun TaskScreen(task: Task, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val title = stringResource(task.titleResId)
    BlankScreen(onBack, modifier.testTag("task-${task.id}").semantics { contentDescription = title })
}

@Composable
fun BlankScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(FlowSpace.page)) {
        Spacer(Modifier.weight(1f))
        BackButton(onBack)
    }
}

@Composable
fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onBack,
        modifier = modifier.fillMaxWidth().heightIn(min = FlowSpace.button).testTag("back"),
        shape = FlowCorners.button
    ) {
        Icon(painterResource(R.drawable.ic_back), contentDescription = null)
        Text(stringResource(R.string.back), Modifier.padding(start = FlowSpace.small))
    }
}
