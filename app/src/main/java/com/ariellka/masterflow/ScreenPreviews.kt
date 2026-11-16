package com.ariellka.masterflow

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ariellka.masterflow.ui.theme.MasterFlowTheme

@Preview(name = "Главная", widthDp = 412, heightDp = 917)
@Preview(name = "Тёмная тема", widthDp = 412, heightDp = 917, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Шрифт 150%", widthDp = 412, heightDp = 917, fontScale = 1.5f)
@Composable
private fun DashboardPreview() {
    MasterFlowApp(null, 0, {}, DashboardState.Content(appointments), {})
}

@Preview(name = "Загрузка", widthDp = 412, heightDp = 917)
@Composable
private fun LoadingPreview() {
    MasterFlowTheme { FlowStateScreen(AppLinks.LOADING, {}, {}) }
}

@Preview(name = "Нет записей", widthDp = 412, heightDp = 917)
@Composable
private fun EmptyPreview() {
    MasterFlowTheme { FlowStateScreen(AppLinks.EMPTY, {}, {}) }
}

@Preview(name = "Пустой раздел", widthDp = 412, heightDp = 917)
@Composable
private fun EmptyDestinationPreview() {
    MasterFlowTheme {
        Surface {
            FlowStateScreen(AppLinks.EMPTY, {}, {}, showPrimaryAction = false)
        }
    }
}

@Preview(name = "Ошибка ожидания", widthDp = 412, heightDp = 917)
@Composable
private fun ErrorPreview() {
    MasterFlowTheme { FlowStateScreen(AppLinks.ERROR, {}, {}, timeoutError = true) }
}
