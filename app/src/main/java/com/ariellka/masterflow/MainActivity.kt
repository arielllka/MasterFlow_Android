package com.ariellka.masterflow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    private var incomingLink by mutableStateOf<String?>(null)
    private var incomingLinkVersion by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        incomingLink = intent.dataString
        val preferences = getSharedPreferences("masterflow", MODE_PRIVATE)
        val dashboard = ViewModelProvider(this, DashboardViewModel.Factory)[DashboardViewModel::class.java]
        setContent {
            DashboardRoute(
                viewModel = dashboard,
                initialLink = incomingLink,
                linkVersion = incomingLinkVersion,
                onExit = { finish() },
                initialThemeMode = preferences.getString("theme", "system") ?: "system",
                onThemeChanged = { preferences.edit().putString("theme", it).apply() },
                onDarkThemeChanged = { dark ->
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
                },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, AppLinks.HOME_LINK)
                    }
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val link = intent.dataString ?: return
        incomingLink = link
        incomingLinkVersion++
        Log.d("MasterFlow", "Новая ссылка: ${intent.data}")
    }

    override fun onStart() { super.onStart(); Log.d("MasterFlow", "onStart") }
    override fun onResume() { super.onResume(); Log.d("MasterFlow", "onResume") }
    override fun onPause() { super.onPause(); Log.d("MasterFlow", "onPause") }
    override fun onStop() { super.onStop(); Log.d("MasterFlow", "onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d("MasterFlow", "onDestroy") }
}
