package io.github.mangi.eta.ui.app

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.data.model.WallpaperScaleMode
import java.io.File
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 页面底板：始终铺满且不透明。
 *
 * - 开启自定义壁纸：解码私有目录图片，Crop 铺满，再叠加压暗/模糊。
 * - 未开启：绘制主题背景色。
 *
 * 关键：这一层必须不透明，让每个路由页面自带一块完整底板，
 * 半透明卡片只透出本页壁纸，不会穿透到导航栈下层页面的文字。
 */
@Composable
fun WallpaperBackground(modifier: Modifier = Modifier) {
    val appearance = LocalAppearanceSettings.current
    val scheme = MiuixTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background),
    ) {
        if (appearance.wallpaperEnabled && appearance.wallpaperPath.isNotBlank()) {
            val bitmap by produceState<ImageBitmap?>(initialValue = null, appearance.wallpaperPath) {
                value = runCatching {
                    val file = File(appearance.wallpaperPath)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                    } else {
                        null
                    }
                }.getOrNull()
            }
            val bmp = bitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = null,
                    contentScale = when (appearance.wallpaperScaleMode) {
                        WallpaperScaleMode.CROP -> ContentScale.Crop
                        WallpaperScaleMode.FIT -> ContentScale.Fit
                    },
                    modifier = if (appearance.wallpaperBlur > 0f) {
                        Modifier.fillMaxSize().blur(appearance.wallpaperBlur.dp)
                    } else {
                        Modifier.fillMaxSize()
                    },
                )
                if (appearance.wallpaperDim > 0f) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = appearance.wallpaperDim)),
                    )
                }
            }
        }
    }
}
