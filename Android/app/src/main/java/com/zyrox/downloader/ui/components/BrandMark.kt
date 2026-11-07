package com.zyrox.downloader.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/** ZYROX's angular Z: a fast diagonal joining two strong horizontal strokes. */
@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    Canvas(modifier.semantics { contentDescription = "ZYROX DOWNLOADER logo" }) {
        drawRoundRect(Color(0xFF17382C), cornerRadius = CornerRadius(size.minDimension * .26f))
        scale(size.width / 64f, size.height / 64f, pivot = Offset.Zero) {
            drawPath(Path().apply {
                moveTo(16f, 17f); lineTo(49f, 17f); lineTo(49f, 25f)
                lineTo(29f, 42f); lineTo(48f, 42f); lineTo(48f, 50f)
                lineTo(14f, 50f); lineTo(14f, 42f); lineTo(35f, 25f)
                lineTo(16f, 25f); close()
            }, Color(0xFFB6F5C5))
            drawPath(Path().apply {
                moveTo(40f, 17f); lineTo(49f, 17f); lineTo(49f, 25f)
                lineTo(32f, 25f); close()
            }, Color(0xFFF4F4EB))
        }
    }
}
