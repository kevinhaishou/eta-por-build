package io.github.mangi.eta.data.model

import kotlinx.serialization.Serializable

const val MIN_INTERFACE_SCALE = 0.8f
const val MAX_INTERFACE_SCALE = 1.1f
const val DEFAULT_INTERFACE_SCALE = 1f

const val MIN_WALLPAPER_DIM = 0f
const val MAX_WALLPAPER_DIM = 1f
const val MIN_WALLPAPER_BLUR = 0f
const val MAX_WALLPAPER_BLUR = 24f

@Serializable
data class AppearanceSettings(
    val themeMode: AppearanceThemeMode = AppearanceThemeMode.SYSTEM,
    val monetEnabled: Boolean = false,
    val paletteStyle: AppearancePaletteStyle = AppearancePaletteStyle.FRUIT_SALAD,
    val accentColor: AppearanceAccentColor = AppearanceAccentColor.SYSTEM,
    val pureBlackEnabled: Boolean = true,
    val blurEnabled: Boolean = true,
    val topBarBlurStyle: AppearanceTopBarBlurStyle = AppearanceTopBarBlurStyle.GAUSSIAN,
    /** 顶栏底色不透明度：1=实色，越小越透出背景。 */
    val topBarAlpha: Float = 0.8f,
    /** 会话侧边栏底色不透明度：1=实色，越小越透出壁纸。 */
    val conversationPaneAlpha: Float = 0.82f,
    val swipeDismissEnabled: Boolean = true,
    val predictiveBackEnabled: Boolean = true,
    val interfaceScale: Float = DEFAULT_INTERFACE_SCALE,
    // ---- 图片功能：自定义壁纸 ----
    val wallpaperEnabled: Boolean = false,
    val wallpaperPath: String = "",
    val wallpaperDim: Float = 0f,
    val wallpaperBlur: Float = 0f,
    val wallpaperScaleMode: WallpaperScaleMode = WallpaperScaleMode.CROP,
    // ---- 图片功能：聊天双方头像 ----
    val avatarEnabled: Boolean = false,
    val avatarMinePath: String = "",
    val avatarAiPath: String = "",
    /** 我的头像独立开关（总开关关闭时不生效）。 */
    val avatarMineEnabled: Boolean = true,
    /** AI 头像独立开关（总开关关闭时不生效）。 */
    val avatarAiEnabled: Boolean = true,
    // ---- 图片功能：聊天气泡样式 ----
    /** 用户气泡底色 ARGB；0 表示跟随主题。 */
    val bubbleUserColor: Long = 0L,
    /** AI 气泡底色 ARGB；0 表示跟随主题。 */
    val bubbleAiColor: Long = 0L,
    /** 气泡圆角(dp)。 */
    val bubbleCornerRadius: Float = 6f,
    /** 气泡内边距(dp)。 */
    val bubblePadding: Float = 8f,
    /** 用户气泡不透明度。 */
    val bubbleUserAlpha: Float = 1f,
    /** AI 气泡不透明度。 */
    val bubbleAiAlpha: Float = 1f,
    /** 工作过程（工具调用/终端执行）气泡底色 ARGB；0 表示跟随 AI 气泡。 */
    val workProcessColor: Long = 0L,
    /** 工作过程气泡不透明度。 */
    val workProcessAlpha: Float = 1f,
) {
    fun normalized(): AppearanceSettings = copy(
        interfaceScale = normalizeInterfaceScale(interfaceScale),
        wallpaperDim = normalizeWallpaperDim(wallpaperDim),
        wallpaperBlur = normalizeWallpaperBlur(wallpaperBlur),
        topBarAlpha = topBarAlpha.coerceIn(0.15f, 1f),
        conversationPaneAlpha = conversationPaneAlpha.coerceIn(0.2f, 1f),
        bubbleUserAlpha = bubbleUserAlpha.coerceIn(0.2f, 1f),
        bubbleAiAlpha = bubbleAiAlpha.coerceIn(0.2f, 1f),
        workProcessAlpha = workProcessAlpha.coerceIn(0.1f, 1f),
    )
}

@Serializable
enum class WallpaperScaleMode(val label: String) {
    /** 铺满屏幕：按短边放大铺满并裁剪溢出部分。 */
    CROP("铺满屏幕"),

    /** 适应屏幕：完整显示整张图，四周可能留边。 */
    FIT("适应屏幕"),
}

@Serializable
enum class AppearanceThemeMode(val persistedValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromPersistedValue(value: String?): AppearanceThemeMode =
            entries.firstOrNull { it.persistedValue == value } ?: SYSTEM
    }
}

@Serializable
enum class AppearancePaletteStyle(val persistedValue: String) {
    TONAL_SPOT("tonal_spot"),
    NEUTRAL("neutral"),
    VIBRANT("vibrant"),
    EXPRESSIVE("expressive"),
    RAINBOW("rainbow"),
    FRUIT_SALAD("fruit_salad"),
    MONOCHROME("monochrome"),
    FIDELITY("fidelity"),
    CONTENT("content");

    companion object {
        fun fromPersistedValue(value: String?): AppearancePaletteStyle =
            entries.firstOrNull { it.persistedValue == value } ?: TONAL_SPOT
    }
}

@Serializable
enum class AppearanceAccentColor(val persistedValue: String) {
    SYSTEM("system"),
    BLUE("blue"),
    PURPLE("purple"),
    PINK("pink"),
    RED("red"),
    ORANGE("orange"),
    YELLOW("yellow"),
    GREEN("green"),
    TEAL("teal");

    companion object {
        fun fromPersistedValue(value: String?): AppearanceAccentColor =
            entries.firstOrNull { it.persistedValue == value } ?: SYSTEM
    }
}

@Serializable
enum class AppearanceTopBarBlurStyle(val persistedValue: String) {
    GAUSSIAN("gaussian"),
    PROGRESSIVE("progressive");

    companion object {
        fun fromPersistedValue(value: String?): AppearanceTopBarBlurStyle =
            entries.firstOrNull { it.persistedValue == value } ?: GAUSSIAN
    }
}

fun normalizeInterfaceScale(value: Float): Float =
    if (value.isFinite()) {
        value.coerceIn(MIN_INTERFACE_SCALE, MAX_INTERFACE_SCALE)
    } else {
        DEFAULT_INTERFACE_SCALE
    }

fun normalizeWallpaperDim(value: Float): Float =
    if (value.isFinite()) value.coerceIn(MIN_WALLPAPER_DIM, MAX_WALLPAPER_DIM) else 0f

fun normalizeWallpaperBlur(value: Float): Float =
    if (value.isFinite()) value.coerceIn(MIN_WALLPAPER_BLUR, MAX_WALLPAPER_BLUR) else 0f
