package com.zyrox.downloader

import android.app.Application
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.detectEnvironment
import com.zyrox.downloader.core.Prefs
import com.zyrox.downloader.ui.AppRoot
import com.zyrox.downloader.ui.HomeViewModel
import com.zyrox.downloader.ui.Tab
import com.zyrox.downloader.ui.theme.ZyroxTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

/** Renders production AppRoot unchanged, with empty on-device history and no engine/network startup. */
class ProductionScreenshots {
    private lateinit var paparazzi: Paparazzi

    @get:Rule
    val screenshotRule = TestRule { base, description ->
        // Android 10 is supported by this app. Its scoped-storage path avoids querying
        // a real StorageManager/package service, which layoutlib does not implement.
        val api = if (description.methodName == "settingsDark") 29 else 36
        paparazzi = Paparazzi(
            environment = detectEnvironment().copy(compileSdkVersion = api),
            deviceConfig = DeviceConfig.PIXEL_5,
            theme = "android:Theme.Material.Light.NoActionBar",
            showSystemUi = false,
            useDeviceResolution = true
        )
        paparazzi.apply(base, description)
    }

    @Test fun homeDark() = capture(Tab.HOME, Prefs.ThemeMode.Dark)
    @Test fun homeLight() = capture(Tab.HOME, Prefs.ThemeMode.Light)
    @Test fun libraryDark() = capture(Tab.DOWNLOADS, Prefs.ThemeMode.Dark)
    @Test fun musicDark() = capture(Tab.MUSIC, Prefs.ThemeMode.Dark)
    @Test fun settingsDark() = capture(Tab.SETTINGS, Prefs.ThemeMode.Dark)

    @Test fun splash() {
        val view = ZyroxSplashView(paparazzi.context)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        paparazzi.gif(view, name = "splash", start = 0, end = 1500, fps = 2)
    }

    private fun capture(tab: Tab, mode: Prefs.ThemeMode) {
        val owner = ScreenshotViewModelOwner()
        // AppRoot's first rememberSaveable is the selected tab. Restoring its real saved state
        // exercises the production navigation and screen layout without a screenshot-only UI.
        val delegate = SaveableStateRegistry(emptyMap()) { true }
        val registry = object : SaveableStateRegistry by delegate {
            private var first = true
            override fun consumeRestored(key: String): Any? = if (first) {
                first = false
                mutableStateOf(tab)
            } else delegate.consumeRestored(key)
        }
        val view = ComposeView(paparazzi.context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setContent {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides owner,
                    LocalSaveableStateRegistry provides registry
                ) {
                    ZyroxTheme(mode) {
                        AppRoot(flowOf(mode), incomingUrl = null, onUrlConsumed = {})
                    }
                }
            }
        }
        try {
            // Record frames from attachment through 1500ms so launch transitions reach
            // their actual final state. Publication uses the final lossless PNG frame.
            paparazzi.gif(view, name = "${tab.name.lowercase()}-${mode.id}", start = 0, end = 1500, fps = 2)
        } finally {
            owner.viewModelStore.clear()
        }
    }

    private class ScreenshotViewModelOwner : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
        override val viewModelStore = ViewModelStore()
        override val defaultViewModelProviderFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass == HomeViewModel::class.java)
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(Application()) as T
            }
        }
    }
}
