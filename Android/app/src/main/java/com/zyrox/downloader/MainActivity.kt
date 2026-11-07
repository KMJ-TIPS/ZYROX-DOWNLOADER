package com.zyrox.downloader

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.zyrox.downloader.ui.AppRoot
import com.zyrox.downloader.ui.theme.ZyroxTheme
import com.zyrox.downloader.util.UrlUtils

class MainActivity : ComponentActivity() {

    private var incomingUrl by mutableStateOf<String?>(null)

    private val notifPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* optional */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Zyrox)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            var showSplash by remember { mutableStateOf(savedInstanceState == null) }
            LaunchedEffect(Unit) {
                if (showSplash) {
                    delay(if (android.animation.ValueAnimator.areAnimatorsEnabled()) 1000L else 150L)
                    showSplash = false
                    delay(240)
                }
                askNotificationPermission()
            }
            val themeFlow = remember { com.zyrox.downloader.core.Prefs.themeFlow(applicationContext) }
            val theme by themeFlow.collectAsState(initial = com.zyrox.downloader.core.Prefs.ThemeMode.System)
            val systemDark = isSystemInDarkTheme()
            SideEffect {
                val dark = when (theme) {
                    com.zyrox.downloader.core.Prefs.ThemeMode.Dark -> true
                    com.zyrox.downloader.core.Prefs.ThemeMode.Light -> false
                    else -> systemDark
                }
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !dark && !showSplash
                    isAppearanceLightNavigationBars = !dark && !showSplash
                }
            }
            ZyroxTheme(mode = theme) {
                Box(Modifier.fillMaxSize()) {
                    AppRoot(
                        themeFlow = themeFlow,
                        incomingUrl = incomingUrl,
                        onUrlConsumed = { incomingUrl = null }
                    )
                    AnimatedVisibility(visible = showSplash, exit = fadeOut(tween(220))) {
                        AndroidView(factory = { ZyroxSplashView(it) }, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
            incomingUrl = UrlUtils.extractUrl(text) ?: text.trim()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
