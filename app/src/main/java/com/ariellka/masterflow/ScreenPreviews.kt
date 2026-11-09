package com.ariellka.masterflow

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ariellka.masterflow.ui.theme.MasterFlowTheme

@Preview(name = "Главная", group = "Главная", widthDp = 412, heightDp = 917)
@Preview(name = "Тёмная тема", group = "Главная", widthDp = 412, heightDp = 917, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardPreview() {
    MasterFlowApp(null, 0, {}, previewDashboardState, {})
}

@Preview(name = "Расписание", group = "План", widthDp = 412, heightDp = 917)
@Composable
private fun SchedulePreview() {
    MasterFlowApp(AppLinks.SCHEDULE_LINK, 0, {}, previewDashboardState, {})
}

@Preview(name = "Загрузка", group = "Состояния", widthDp = 412, heightDp = 917)
@Composable
private fun LoadingPreview() {
    MasterFlowApp(null, 0, {}, previewDashboardState.copy(status = DashboardStatus.LOADING), {})
}

@Preview(name = "Нет записей", group = "Состояния", widthDp = 412, heightDp = 917)
@Composable
private fun EmptyPreview() {
    MasterFlowApp(null, 0, {}, DashboardUiState(status = DashboardStatus.EMPTY), {})
}

@Preview(name = "Пустой раздел", group = "Состояния", widthDp = 412, heightDp = 917)
@Composable
private fun EmptyDestinationPreview() {
    MasterFlowTheme {
        Surface {
            FlowStateScreen(AppLinks.EMPTY, {}, {}, showPrimaryAction = false)
        }
    }
}

@Preview(name = "Ошибка ожидания", group = "Состояния", widthDp = 412, heightDp = 917)
@Composable
private fun ErrorPreview() {
    MasterFlowApp(null, 0, {}, DashboardUiState(status = DashboardStatus.ERROR, timedOut = true), {})
}
