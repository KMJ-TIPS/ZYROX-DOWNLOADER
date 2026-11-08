package com.zyrox.downloader.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.zyrox.downloader.audio.SoundEffects
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zyrox.downloader.core.*
import com.zyrox.downloader.ui.HomeViewModel
import com.zyrox.downloader.ui.components.*
import com.zyrox.downloader.ui.theme.LocalAurora
import com.zyrox.downloader.util.UrlUtils

@Composable
fun HomeScreen(vm: HomeViewModel, incomingUrl: String?, onUrlConsumed: () -> Unit, onLibrary: () -> Unit = {}) {
    val a = LocalAurora.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val urlFocus = remember { FocusRequester() }
    val history by DownloadRepository.history.collectAsState()
    var entered by remember { mutableStateOf(false) }
    val reveal by animateFloatAsState(if (entered) 1f else 0f, tween(340), label = "home entrance")
    LaunchedEffect(Unit) { entered = true }
    LaunchedEffect(incomingUrl) { if (!incomingUrl.isNullOrBlank()) { vm.fetch(incomingUrl); onUrlConsumed() } }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 680.dp).fillMaxSize().verticalScroll(rememberScrollState()).imePadding()
            .graphicsLayer { alpha = reveal; translationY = (1f - reveal) * 12.dp.toPx() }.padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(15.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark(Modifier.size(45.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("ZYROX", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-.8).sp, color = a.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("D O W N L O A D E R", fontSize = 8.sp, fontWeight = FontWeight.Medium, color = a.textDim, lineHeight = 13.sp)
                }
                Spacer(Modifier.width(10.dp))
                Box(Modifier.size(43.dp).clip(CircleShape).background(a.surface).border(1.dp, a.glassBorder, CircleShape).clickable(onClick = { SoundEffects.tap(context); onLibrary() }), contentAlignment = Alignment.Center) {
                    Icon(VidIcons.VideoLibrary, "Open saved downloads", Modifier.size(20.dp), tint = a.text)
                }
            }
            Spacer(Modifier.height(24.dp))
            if (vm.meta == null && !vm.fetching) { StudioHero(); Spacer(Modifier.height(22.dp)) }
            if (vm.meta == null) {
                Surface(shape = RoundedCornerShape(30.dp), color = a.surface, border = androidx.compose.foundation.BorderStroke(1.dp, a.glassBorder)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Drop a link.", style = MaterialTheme.typography.titleLarge, letterSpacing = (-.6).sp)
                                Text("We'll take it from here.", style = MaterialTheme.typography.bodySmall, color = a.textDim)
                            }
                            TextButton(onClick = { SoundEffects.tap(context); clipboard.getText()?.text?.takeIf { it.isNotBlank() }?.let { vm.onUrlChange(UrlUtils.extractUrl(it) ?: it.trim()) } }, contentPadding = PaddingValues(horizontal = 10.dp)) {
                                Icon(VidIcons.Paste, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Paste", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Spacer(Modifier.height(17.dp))
                        StudioInput(vm.url, vm::onUrlChange, "Video or post link", VidIcons.Link, modifier = Modifier.focusRequester(urlFocus), uri = true) { keyboard?.hide(); vm.fetch() }
                        Spacer(Modifier.height(12.dp))
                        GradientButton(if (vm.fetching) "Finding available formats…" else "Find media", icon = Icons.Default.ArrowForward,
                            enabled = !vm.fetching, loading = vm.fetching) {
                            if (vm.url.isBlank()) {
                                urlFocus.requestFocus(); keyboard?.show()
                            } else { keyboard?.hide(); vm.fetch() }
                        }
                        if (vm.fetching) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Connecting to the source", style = MaterialTheme.typography.bodySmall, color = a.textDim, modifier = Modifier.weight(1f))
                            TextButton(onClick = { SoundEffects.tap(context); vm.cancelFetch() }) { Text("Cancel") }
                        } else {
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(VidIcons.Shield, null, Modifier.size(11.dp), tint = a.textFaint)
                                Spacer(Modifier.width(5.dp))
                                Text("On your phone. On your terms.", style = MaterialTheme.typography.bodySmall, color = a.textFaint, fontSize = 10.sp)
                            }
                        }
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(a.surface).border(1.dp, a.glassBorder, RoundedCornerShape(22.dp)).padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(VidIcons.Link, null, Modifier.size(16.dp), tint = a.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(vm.url, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = a.textDim, modifier = Modifier.weight(1f))
                    TextButton(onClick = { SoundEffects.tap(context); vm.clearAll() }) { Text("New link", style = MaterialTheme.typography.labelSmall) }
                }
            }
            vm.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Surface(color = a.danger.copy(alpha = .09f), shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.padding(15.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Info, null, Modifier.size(18.dp), tint = a.danger)
                        Column { Text("This link needs another try", style = MaterialTheme.typography.labelLarge, color = a.danger); Text(error, style = MaterialTheme.typography.bodySmall, color = a.text) }
                    }
                }
            }
            Spacer(Modifier.height(23.dp))
            if (vm.fetching) {
                ShimmerBox(Modifier.fillMaxWidth().aspectRatio(16f / 9f), 22)
                Spacer(Modifier.height(12.dp)); ShimmerBox(Modifier.fillMaxWidth(.8f).height(16.dp), 7)
            } else if (vm.meta != null) ResultCard(vm, onLibrary)
            else {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("ONE LINK. MANY POSSIBILITIES.", style = MaterialTheme.typography.labelSmall, color = a.textFaint, fontSize = 9.sp, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
                    Text("yt-dlp", style = MaterialTheme.typography.labelSmall, color = a.primary, fontSize = 9.sp)
                }
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("YouTube", "Instagram", "Facebook", "TikTok", "X", "Vimeo")) { name ->
                        Row(Modifier.clip(RoundedCornerShape(17.dp)).background(a.surface).border(1.dp, a.glassBorder, RoundedCornerShape(17.dp)).padding(horizontal = 11.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            PlatformSymbol(name, Modifier.size(16.dp)); Spacer(Modifier.width(7.dp)); Text(name, style = MaterialTheme.typography.labelSmall, color = a.text)
                        }
                    }
                }
                Spacer(Modifier.height(23.dp))
                if (history.isNotEmpty()) {
                    SectionHeading("Your latest finds", "View all", onLibrary)
                    history.take(2).forEach { item ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = { SoundEffects.tap(context); onLibrary() }).padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            MediaArtwork(item.thumbnailPath, item.thumbnailUrl, item.title, Modifier.size(76.dp, 54.dp).clip(RoundedCornerShape(17.dp)), item.mime.startsWith("audio"))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(4.dp)); Text("${item.formatLabel} · ${UrlUtils.formatBytes(item.sizeBytes)}", style = MaterialTheme.typography.bodySmall, color = a.textDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, null, Modifier.size(20.dp), tint = a.textFaint)
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MiniFeature(VidIcons.VideoLibrary, "Keep the moment", "Video in your quality.", Modifier.weight(1f))
                        MiniFeature(VidIcons.MusicNote, "Take the music", "Audio. Ready offline.", Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
        }
    }
}

@Composable
private fun StudioHero() {
    val a = LocalAurora.current
    val fontScale = LocalDensity.current.fontScale
    val largeText = fontScale >= 1.25f
    BoxWithConstraints(Modifier.fillMaxWidth().height((211f * fontScale.coerceAtLeast(1f)).dp)) {
        if (!largeText && maxWidth >= 280.dp) Canvas(Modifier.align(Alignment.CenterEnd).width(146.dp).fillMaxHeight()) {
            scale(size.width / 160f, size.height / 182f, pivot = Offset.Zero) {
                val crystal = Color(0xFF92B7FF)
                val ink = Color(0xFF192E59)
                drawCircle(Brush.radialGradient(listOf(crystal.copy(alpha = .11f), Color.Transparent), Offset(95f, 90f), 91f), 91f, Offset(95f, 90f))
                rotate(-16f, Offset(87f, 87f)) {
                    drawRoundRect(crystal.copy(alpha = .05f), Offset(35f, 31f), Size(108f, 116f), CornerRadius(23f))
                    drawRoundRect(crystal.copy(alpha = .17f), Offset(35f, 31f), Size(108f, 116f), CornerRadius(23f), style = Stroke(1.2f))
                }
                rotate(9f, Offset(87f, 87f)) {
                    drawRoundRect(ink, Offset(32f, 29f), Size(108f, 116f), CornerRadius(23f))
                    drawRoundRect(crystal.copy(alpha = .45f), Offset(32f, 29f), Size(108f, 116f), CornerRadius(23f), style = Stroke(1.2f))
                    drawCircle(crystal, 24f, Offset(87f, 77f))
                    drawLine(ink, Offset(87f, 65f), Offset(87f, 87f), 3.5f, StrokeCap.Round)
                    drawPath(Path().apply { moveTo(78f, 79f); lineTo(87f, 88f); lineTo(96f, 79f) }, ink, style = Stroke(3.5f, cap = StrokeCap.Round))
                    drawLine(crystal.copy(alpha = .6f), Offset(61f, 117f), Offset(109f, 117f), 3f, StrokeCap.Round)
                    drawLine(crystal.copy(alpha = .22f), Offset(70f, 125f), Offset(100f, 125f), 3f, StrokeCap.Round)
                }
                drawCircle(if (a.isDark) a.bg else a.surface, 21f, Offset(44f, 139f))
                drawCircle(crystal.copy(alpha = .5f), 20f, Offset(44f, 139f), style = Stroke(1f))
                for (i in 0..4) {
                    val h = listOf(7f, 14f, 20f, 11f, 6f)[i]
                    drawLine(if (a.isDark) crystal else a.primary, Offset(34f + i * 5f, 139f - h / 2), Offset(34f + i * 5f, 139f + h / 2), 2.2f, StrokeCap.Round)
                }
                drawCircle(crystal, 3f, Offset(139f, 23f))
                drawCircle(crystal.copy(alpha = .3f), 2f, Offset(15f, 81f))
            }
        }
        Column(Modifier.align(Alignment.CenterStart).widthIn(max = if (largeText) 600.dp else 220.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(a.primary))
                Spacer(Modifier.width(7.dp))
                Text("YOUR MEDIA. IN A NEW DIMENSION.", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = a.textDim, letterSpacing = 1.1.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text("Save it.", color = a.text, fontWeight = FontWeight.ExtraBold, fontSize = 35.sp, lineHeight = 37.sp, letterSpacing = (-1.3).sp)
            Text("Play it.", color = a.text, fontWeight = FontWeight.ExtraBold, fontSize = 35.sp, lineHeight = 37.sp, letterSpacing = (-1.3).sp)
            Text("Keep it.", color = a.primary, fontWeight = FontWeight.ExtraBold, fontSize = 35.sp, lineHeight = 37.sp, letterSpacing = (-1.3).sp)
            Spacer(Modifier.height(10.dp))
            Text("Video. Music. Ready when you are.", color = a.textDim, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PlatformSymbol(name: String, modifier: Modifier) {
    val a = LocalAurora.current
    val color = when (name) { "YouTube" -> Color(0xFFFF777C); "Instagram" -> a.magenta; "Facebook" -> Color(0xFF91B5FA); "TikTok" -> a.cyan; else -> a.textDim }
    Canvas(modifier) {
        scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) {
            when (name) {
                "YouTube" -> {
                    drawRoundRect(color, Offset(1f, 4f), Size(22f, 16f), CornerRadius(5f))
                    drawPath(Path().apply { moveTo(10f, 8f); lineTo(16f, 12f); lineTo(10f, 16f); close() }, a.surface)
                }
                "Instagram" -> {
                    drawRoundRect(color, Offset(3f, 3f), Size(18f, 18f), CornerRadius(5.5f), style = Stroke(2f))
                    drawCircle(color, 4.2f, Offset(12f, 12f), style = Stroke(2f)); drawCircle(color, 1.3f, Offset(17.2f, 6.8f))
                }
                "Facebook" -> drawPath(Path().apply { moveTo(14.5f, 22f); lineTo(14.5f, 13f); lineTo(18f, 13f); lineTo(18.5f, 9.5f); lineTo(14.5f, 9.5f); lineTo(14.5f, 7f); quadraticBezierTo(14.5f, 5f, 17f, 5f); lineTo(19f, 5f); lineTo(19f, 2f); lineTo(16.5f, 2f); quadraticBezierTo(10f, 2f, 10f, 7f); lineTo(10f, 9.5f); lineTo(7f, 9.5f); lineTo(7f, 13f); lineTo(10f, 13f); lineTo(10f, 22f); close() }, color)
                "TikTok" -> {
                    drawPath(Path().apply { moveTo(14f, 3f); lineTo(14f, 16f); cubicTo(14f, 22f, 4f, 22f, 4f, 16f); cubicTo(4f, 12f, 8f, 11f, 10f, 12f) }, color, style = Stroke(3.5f, cap = StrokeCap.Round))
                    drawPath(Path().apply { moveTo(14f, 3f); quadraticBezierTo(15f, 8f, 20f, 8f) }, color, style = Stroke(3.5f, cap = StrokeCap.Round))
                }
                "X" -> { drawLine(color, Offset(4f, 3f), Offset(20f, 21f), 2.5f); drawLine(color, Offset(20f, 3f), Offset(4f, 21f), 1.5f) }
                else -> drawPath(Path().apply { moveTo(3f, 8f); cubicTo(7f, 3f, 9f, 5f, 10f, 11f); lineTo(12f, 17f); cubicTo(15f, 14f, 18f, 8f, 14f, 9f); cubicTo(18f, 1f, 26f, 5f, 19f, 15f); cubicTo(11f, 27f, 8f, 23f, 6f, 14f); lineTo(4f, 8f); close() }, color)
            }
        }
    }
}

@Composable
private fun MiniFeature(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String, modifier: Modifier) {
    val a = LocalAurora.current
    Row(modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(a.surface), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(17.dp), tint = a.primary)
        }
        Spacer(Modifier.width(9.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelSmall, color = a.text)
            Text(detail, style = MaterialTheme.typography.bodySmall, fontSize = 9.sp, color = a.textFaint)
        }
    }
}

@Composable
private fun ResultCard(vm: HomeViewModel, onLibrary: () -> Unit) {
    val a = LocalAurora.current
    val info = vm.meta ?: return
    val selected = vm.formats.firstOrNull { it.id == vm.selectedId }
    val audio = selected?.isAudioOnly ?: false
    var expanded by rememberSaveable(info.webpageUrl) { mutableStateOf(false) }
    val options = vm.formats.filter { it.isAudioOnly == audio }
    Column {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(30.dp))) {
            MediaArtwork(info.thumbnailPath, info.thumbnailUrl, info.title, Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .7f)))))
            Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(7.dp), color = Color.White.copy(alpha = .16f)) { Text(info.platform, Modifier.padding(horizontal = 8.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = Color.White) }
                Spacer(Modifier.weight(1f))
                Text(UrlUtils.formatDuration(info.durationSec), color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(16.dp)); Text(info.title, style = MaterialTheme.typography.titleLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
        if (!info.uploader.isNullOrBlank()) { Spacer(Modifier.height(4.dp)); Text(info.uploader, style = MaterialTheme.typography.bodySmall, color = a.textDim) }
        Spacer(Modifier.height(20.dp))
        StudioTabs(listOf("Video", "Audio only"), if (audio) 1 else 0, onSelect = { index -> vm.formats.firstOrNull { it.isAudioOnly == (index == 1) }?.let { vm.select(it.id) } }, enabled = listOf(vm.formats.any { !it.isAudioOnly }, vm.formats.any { it.isAudioOnly }))
        Spacer(Modifier.height(12.dp))
        SectionHeading("Make it your quality", if (options.size > 4) if (expanded) "Less" else "All formats" else null) { expanded = !expanded }
        val visible = if (expanded) options else options.take(4).let { first -> if (selected != null && selected !in first) first.dropLast(1) + selected else first }
        visible.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { option -> QualityTile(option, option.id == selected?.id, Modifier.weight(1f)) { vm.select(option.id) } }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(7.dp))
        GradientButton(if (vm.addedToQueue) "View downloads" else if (audio) "Save audio" else "Save video", icon = if (vm.addedToQueue) Icons.Default.CheckCircle else VidIcons.Download,
            enabled = selected != null && !vm.enqueuing, loading = vm.enqueuing) { if (vm.addedToQueue) onLibrary() else vm.download() }
        Spacer(Modifier.height(10.dp))
        Text(if (vm.addedToQueue) "You're all set. It's in your download queue." else "Saves to your phone · downloads in the background", style = MaterialTheme.typography.bodySmall, color = a.textDim, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun QualityTile(option: FormatOption, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val a = LocalAurora.current
    val shape = RoundedCornerShape(23.dp)
    val interaction = remember { MutableInteractionSource() }
    val context = LocalContext.current
    Column(modifier.pressScale(interaction).clip(shape).background(if (selected) a.primary.copy(alpha = .1f) else a.surface)
        .border(if (selected) 1.5.dp else 1.dp, if (selected) a.primary.copy(alpha = .8f) else a.glassBorder, shape)
        .selectable(selected, role = Role.RadioButton, interactionSource = interaction, indication = ripple(), onClick = { if (!selected) SoundEffects.tap(context); onClick() }).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(option.label.removePrefix("Best — "), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (selected) a.primary else a.text, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(4.dp))
            Box(Modifier.size(17.dp).clip(CircleShape).background(if (selected) a.primary else Color.Transparent).border(1.dp, if (selected) a.primary else a.textFaint, CircleShape), contentAlignment = Alignment.Center) {
                if (selected) Icon(Icons.Default.Check, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
        Spacer(Modifier.height(7.dp))
        val size = if (option.estBytes == null || option.estBytes <= 0) "Size unavailable" else "${if (option.isApprox) "~" else ""}${UrlUtils.formatBytes(option.estBytes)}${if (option.isApprox) " estimated" else ""}"
        Text(size, style = MaterialTheme.typography.bodySmall, color = a.textDim)
        if (option.id == "best") { Spacer(Modifier.height(4.dp)); Text("BEST AVAILABLE", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = a.primary) }
    }
}
