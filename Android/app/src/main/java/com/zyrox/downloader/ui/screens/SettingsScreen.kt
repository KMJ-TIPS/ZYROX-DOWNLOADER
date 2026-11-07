package com.zyrox.downloader.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zyrox.downloader.BuildConfig
import com.zyrox.downloader.core.*
import com.zyrox.downloader.ui.components.*
import com.zyrox.downloader.ui.theme.LocalAurora
import com.zyrox.downloader.util.MediaSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(themeFlow: kotlinx.coroutines.flow.Flow<Prefs.ThemeMode>, onTheme: (Prefs.ThemeMode) -> Unit) {
    val a = LocalAurora.current
    val context = LocalContext.current
    val ctx = context.applicationContext
    val theme by themeFlow.collectAsState(initial = Prefs.ThemeMode.System)
    val update by UpdateManager.state.collectAsState()
    var version by remember { mutableStateOf(Engine.BUNDLED_VERSION) }
    var showAbout by remember { mutableStateOf(false) }
    LaunchedEffect(update) {
        version = withContext(Dispatchers.IO) { Engine.engineVersion(ctx) } ?: Engine.BUNDLED_VERSION
    }
    val busy = update is UpdateState.Checking || update is UpdateState.Updating
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 680.dp).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column {
                Text("MAKE IT YOURS", style = MaterialTheme.typography.labelSmall, color = a.primary, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Text("Settings", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(5.dp))
                Text("A little more you. A lot more ZYROX.", style = MaterialTheme.typography.bodySmall, color = a.textDim)
            }
            Surface(shape = RoundedCornerShape(24.dp), color = a.surface, border = BorderStroke(1.dp, a.glassBorder)) {
                Row(
                    Modifier.fillMaxWidth().background(a.gradientSoft).clickable { showAbout = true }.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrandMark(Modifier.size(62.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("ZYROX", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                        Text("D O W N L O A D E R", style = MaterialTheme.typography.labelSmall, color = a.textDim, fontSize = 9.sp)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(5.dp).clip(CircleShape).background(a.primary))
                            Spacer(Modifier.width(6.dp))
                            Text("v${BuildConfig.VERSION_NAME}  ·  No in-app ads", style = MaterialTheme.typography.bodySmall, color = a.textDim)
                        }
                    }
                }
            }
            Column {
                SectionHeading("Your look")
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(Prefs.ThemeMode.System, Prefs.ThemeMode.Light, Prefs.ThemeMode.Dark).forEach { mode ->
                        ThemeChoice(mode, theme == mode, Modifier.weight(1f)) { onTheme(mode) }
                    }
                }
            }
            SettingsPanel(VidIcons.Update, "Download engine", "The power behind every saved moment") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("yt-dlp", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(version, style = MaterialTheme.typography.labelSmall, color = a.primary,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(a.primary.copy(alpha = .09f)).padding(horizontal = 9.dp, vertical = 5.dp))
                }
                Spacer(Modifier.height(10.dp))
                val label = when (update) {
                    is UpdateState.Checking -> "Checking for an engine update…"
                    is UpdateState.Updating -> "Updating your engine in the background…"
                    is UpdateState.Done -> "Your engine is up to date"
                    is UpdateState.Failed -> "Couldn't update. Check your connection and retry."
                    else -> "Official updates checked automatically"
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(if (update is UpdateState.Failed) a.danger else a.success))
                    Spacer(Modifier.width(7.dp))
                    Text(label, style = MaterialTheme.typography.bodySmall, color = a.textDim)
                }
                Spacer(Modifier.height(16.dp))
                GradientButton(if (update is UpdateState.Updating) "Updating engine…" else if (busy) "Checking engine…" else "Check for engine updates", icon = VidIcons.Update, enabled = !busy, loading = busy) {
                    UpdateManager.manualCheck(ctx)
                }
                Spacer(Modifier.height(10.dp))
                Text("Get the latest platform fixes from the official yt-dlp project.", style = MaterialTheme.typography.bodySmall, color = a.textDim)
            }
            SettingsPanel(VidIcons.Download, "Saved on your device", "Your files, ready when you are") {
                val direct = MediaSaver.hasAllFilesAccess(ctx)
                val root = MediaSaver.ROOT_DIR
                LocationRow(VidIcons.VideoLibrary, "Videos", if (direct) "$root/Video" else "Movies/$root/Video")
                HorizontalDivider(Modifier.padding(vertical = 14.dp), color = a.glassBorder)
                LocationRow(VidIcons.Download, "Audio", if (direct) "$root/Audio" else "Music/$root/Audio")
                Spacer(Modifier.height(13.dp))
                Text("Find saved files in your gallery or file manager. Uninstalling ZYROX clears app history; your downloaded media stays on the device.", style = MaterialTheme.typography.bodySmall, color = a.textDim)
            }
            SettingsPanel(VidIcons.Shield, "Your space. Your privacy.", "No ZYROX account required") {
                Text("Downloads connect directly to the source platform. Media, history, and saved thumbnails stay on this device. Engine update checks connect to GitHub.", style = MaterialTheme.typography.bodySmall, color = a.textDim)
                Spacer(Modifier.height(12.dp))
                Text("Platform support can change. Private, protected, or login-only posts may be unavailable. Save content you own or have permission to download.", style = MaterialTheme.typography.bodySmall, color = a.textDim)
            }
            TextButton(onClick = { showAbout = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("About ZYROX & project credits", style = MaterialTheme.typography.labelLarge)
            }
            Text("SAVE THE MOMENT.", style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp, color = a.textFaint, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(10.dp))
        }
    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            icon = { BrandMark(Modifier.size(52.dp)) },
            title = { Text("ZYROX DOWNLOADER") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Version ${BuildConfig.VERSION_NAME}")
                    Text("A home for the moments you want to keep. Powered by yt-dlp and FFmpeg.")
                    Text("Based on the VidGrab project by Saurabh-gzp. Original project credit and third-party notices are included with the source.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Done") } },
            dismissButton = {
                TextButton(onClick = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Saurabh-gzp/VidGrab"))) }
                }) { Text("Original project") }
            }
        )
    }
}

@Composable
private fun ThemeChoice(mode: Prefs.ThemeMode, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val a = LocalAurora.current
    val shape = RoundedCornerShape(17.dp)
    val border by animateColorAsState(if (selected) a.primary else a.glassBorder, label = "appearance border")
    val background by animateColorAsState(if (selected) a.primary.copy(alpha = .08f) else a.surface, label = "appearance background")
    val dark = mode == Prefs.ThemeMode.Dark
    val previewBg = if (dark) Color(0xFF0A1014) else Color(0xFFF2F6F0)
    Column(modifier.clip(shape).background(background).border(if (selected) 1.5.dp else 1.dp, border, shape)
        .selectable(selected, role = Role.RadioButton, onClick = onClick).padding(11.dp)) {
        Box(Modifier.fillMaxWidth().height(55.dp).clip(RoundedCornerShape(9.dp))
            .background(if (mode == Prefs.ThemeMode.System) Brush.horizontalGradient(listOf(Color(0xFFF2F6F0), Color(0xFF0A1014)))
            else Brush.linearGradient(listOf(previewBg, previewBg)))) {
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.width(20.dp).height(5.dp).clip(CircleShape).background(if (dark) Color(0xFFB6F5C5) else Color(0xFF176548)))
                Box(Modifier.fillMaxWidth(.8f).height(4.dp).clip(CircleShape).background(if (dark) Color(0xFF43595F) else Color(0xFFBBCDC2)))
                Box(Modifier.fillMaxWidth(.6f).height(4.dp).clip(CircleShape).background(if (dark) Color(0xFF43595F) else Color(0xFFBBCDC2)))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(mode.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            if (selected) Icon(Icons.Default.Check, null, Modifier.size(15.dp), tint = a.primary)
        }
    }
}

@Composable
private fun SettingsPanel(icon: ImageVector, title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    val a = LocalAurora.current
    Surface(shape = RoundedCornerShape(24.dp), color = a.surface, border = BorderStroke(1.dp, a.glassBorder)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(a.primary.copy(alpha = .09f)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(19.dp), tint = a.primary)
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = a.textDim)
                }
            }
            Spacer(Modifier.height(18.dp))
            content()
        }
    }
}

@Composable
private fun LocationRow(icon: ImageVector, title: String, path: String) {
    val a = LocalAurora.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(19.dp), tint = a.textDim)
        Spacer(Modifier.width(11.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(3.dp))
            Text(path, style = MaterialTheme.typography.bodySmall, color = a.textDim)
        }
    }
}
