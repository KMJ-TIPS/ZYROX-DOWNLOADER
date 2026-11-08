package com.zyrox.downloader.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.zyrox.downloader.branding.ZyroxLogoDrawable

/** The same original download mark appears in the app, splash, launcher and website. */
@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    val logo = remember { ZyroxLogoDrawable() }
    Canvas(modifier.semantics { contentDescription = "ZYROX DOWNLOADER logo" }) {
        drawIntoCanvas { canvas ->
            logo.setBounds(0, 0, size.width.toInt(), size.height.toInt())
            logo.draw(canvas.nativeCanvas)
        }
    }
}
