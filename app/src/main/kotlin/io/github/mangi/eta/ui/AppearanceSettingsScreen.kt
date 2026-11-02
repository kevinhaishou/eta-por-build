package io.github.mangi.eta.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.mangi.eta.R
import io.github.mangi.eta.data.model.AppearanceAccentColor
import io.github.mangi.eta.data.model.AppearancePaletteStyle
import io.github.mangi.eta.data.model.MAX_WALLPAPER_DIM
import io.github.mangi.eta.data.model.MIN_WALLPAPER_BLUR
import io.github.mangi.eta.data.model.MAX_WALLPAPER_BLUR
import io.github.mangi.eta.data.model.WallpaperScaleMode
import io.github.mangi.eta.data.model.AppearanceSettings
import io.github.mangi.eta.data.model.AppearanceThemeMode
import io.github.mangi.eta.data.model.AppearanceTopBarBlurStyle
import io.github.mangi.eta.data.model.MAX_INTERFACE_SCALE
import io.github.mangi.eta.data.model.MIN_INTERFACE_SCALE
import io.github.mangi.eta.data.model.normalizeInterfaceScale
import io.github.mangi.eta.data.repository.AppearanceSettingsRepository
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import io.github.mangi.eta.ui.app.LocalAppearanceSettings
import java.io.File
import io.github.mangi.eta.ui.components.EtaArrowPreference
import io.github.mangi.eta.ui.components.EtaCard
import io.github.mangi.eta.ui.components.EtaOverlayDropdownPreference
import io.github.mangi.eta.ui.components.EtaPreferenceDivider
import io.github.mangi.eta.ui.components.EtaPreferenceGroup
import io.github.mangi.eta.ui.components.EtaPreferenceGroupTitle
import io.github.mangi.eta.ui.components.EtaSwitchPreference
import io.github.mangi.eta.ui.components.EtaWindowDialog
import io.github.mangi.eta.ui.components.MiuixDialogActions
import io.github.mangi.eta.ui.components.MiuixScaffoldPage
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SliderDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun AppearanceSettingsScreen(onBack: () -> Unit) {
    val appearance = LocalAppearanceSettings.current
    val coroutineScope = rememberCoroutineScope()
    var scaleDraft by remember(appearance.interfaceScale) {
        mutableFloatStateOf(appearance.interfaceScale * 100f)
    }
    var showScaleDialog by remember { mutableStateOf(false) }
    var scaleInput by remember { mutableStateOf("") }
    var dimDraft by remember(appearance.wallpaperDim) { mutableFloatStateOf(appearance.wallpaperDim) }
    var showBubbleDialog by remember { mutableStateOf(false) }
    var topBarAlphaDraft by remember(appearance.topBarAlpha) { mutableFloatStateOf(appearance.topBarAlpha) }
    var paneAlphaDraft by remember(appearance.conversationPaneAlpha) {
        mutableFloatStateOf(appearance.conversationPaneAlpha)
    }
    var bubbleCornerDraft by remember(appearance.bubbleCornerRadius) {
        mutableFloatStateOf(appearance.bubbleCornerRadius)
    }
    var bubblePaddingDraft by remember(appearance.bubblePadding) {
        mutableFloatStateOf(appearance.bubblePadding)
    }
    var bubbleUserAlphaDraft by remember(appearance.bubbleUserAlpha) {
        mutableFloatStateOf(appearance.bubbleUserAlpha)
    }
    var bubbleAiAlphaDraft by remember(appearance.bubbleAiAlpha) {
        mutableFloatStateOf(appearance.bubbleAiAlpha)
    }
    var blurDraft by remember(appearance.wallpaperBlur) { mutableFloatStateOf(appearance.wallpaperBlur) }
    val blurSupported = isRuntimeShaderSupported()

    fun update(transform: (AppearanceSettings) -> AppearanceSettings) {
        coroutineScope.launch {
            AppearanceSettingsRepository.update(transform)
        }
    }

    val context = LocalContext.current
    val wallpaperPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            copyPickedImage(context, uri, "wallpaper")?.let { path ->
                update { current -> current.copy(wallpaperPath = path, wallpaperEnabled = true) }
            }
        }
    }
    val avatarMinePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            copyPickedImage(context, uri, "avatar_mine")?.let { path ->
                update { current -> current.copy(avatarMinePath = path, avatarEnabled = true) }
            }
        }
    }
    val avatarAiPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            copyPickedImage(context, uri, "avatar_ai")?.let { path ->
                update { current -> current.copy(avatarAiPath = path, avatarEnabled = true) }
            }
        }
    }

    fun commitScale(percent: Float) {
        val scale = normalizeInterfaceScale(percent.roundToInt() / 100f)
        scaleDraft = scale * 100f
        update { current -> current.copy(interfaceScale = scale) }
    }

    val themeModes = AppearanceThemeMode.entries
    val themeModeLabels = listOf(
        stringResource(R.string.appearance_theme_system),
        stringResource(R.string.appearance_theme_light),
        stringResource(R.string.appearance_theme_dark),
    )
    val paletteStyles = AppearancePaletteStyle.entries
    val paletteLabels = listOf(
        stringResource(R.string.appearance_palette_tonal_spot),
        stringResource(R.string.appearance_palette_neutral),
        stringResource(R.string.appearance_palette_vibrant),
        stringResource(R.string.appearance_palette_expressive),
        stringResource(R.string.appearance_palette_rainbow),
        stringResource(R.string.appearance_palette_fruit_salad),
        stringResource(R.string.appearance_palette_monochrome),
        stringResource(R.string.appearance_palette_fidelity),
        stringResource(R.string.appearance_palette_content),
    )
    val accentColors = AppearanceAccentColor.entries
    val accentLabels = listOf(
        stringResource(R.string.appearance_accent_system),
        stringResource(R.string.appearance_accent_blue),
        stringResource(R.string.appearance_accent_purple),
        stringResource(R.string.appearance_accent_pink),
        stringResource(R.string.appearance_accent_red),
        stringResource(R.string.appearance_accent_orange),
        stringResource(R.string.appearance_accent_yellow),
        stringResource(R.string.appearance_accent_green),
        stringResource(R.string.appearance_accent_teal),
    )
    val blurStyles = AppearanceTopBarBlurStyle.entries
    val blurStyleLabels = listOf(
        stringResource(R.string.appearance_blur_style_gaussian),
        stringResource(R.string.appearance_blur_style_progressive),
    )
    val blurStyleSummaries = listOf(
        stringResource(R.string.appearance_blur_style_gaussian_summary),
        stringResource(R.string.appearance_blur_style_progressive_summary),
    )

    MiuixScaffoldPage(
        title = stringResource(R.string.appearance_title),
        onBack = onBack,
    ) {
        item(key = "appearance_color_title") {
            EtaPreferenceGroupTitle(text = stringResource(R.string.appearance_group_color))
        }
        item(key = "appearance_color_card") {
            EtaPreferenceGroup(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                EtaOverlayDropdownPreference(
                    title = stringResource(R.string.appearance_theme_mode),
                    summary = themeModeLabels[appearance.themeMode.ordinal],
                    items = themeModeLabels,
                    selectedIndex = appearance.themeMode.ordinal,
                    onSelectedIndexChange = { index ->
                        themeModes.getOrNull(index)?.let { mode ->
                            update { current -> current.copy(themeMode = mode) }
                        }
                    },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaSwitchPreference(
                    title = stringResource(R.string.appearance_monet),
                    summary = stringResource(R.string.appearance_monet_summary),
                    checked = appearance.monetEnabled,
                    onCheckedChange = { enabled ->
                        update { current -> current.copy(monetEnabled = enabled) }
                    },
                )
                AnimatedVisibility(
                    visible = appearance.monetEnabled,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                ) {
                    Column {
                        EtaPreferenceDivider(hasLeading = false)
                        EtaOverlayDropdownPreference(
                            title = stringResource(R.string.appearance_palette_style),
                            summary = paletteLabels[appearance.paletteStyle.ordinal],
                            items = paletteLabels,
                            selectedIndex = appearance.paletteStyle.ordinal,
                            onSelectedIndexChange = { index ->
                                paletteStyles.getOrNull(index)?.let { style ->
                                    update { current -> current.copy(paletteStyle = style) }
                                }
                            },
                        )
                        EtaPreferenceDivider(hasLeading = false)
                        EtaOverlayDropdownPreference(
                            title = stringResource(R.string.appearance_accent_color),
                            summary = accentLabels[appearance.accentColor.ordinal],
                            items = accentLabels,
                            selectedIndex = appearance.accentColor.ordinal,
                            onSelectedIndexChange = { index ->
                                accentColors.getOrNull(index)?.let { accent ->
                                    update { current -> current.copy(accentColor = accent) }
                                }
                            },
                        )
                        EtaPreferenceDivider(hasLeading = false)
                        EtaSwitchPreference(
                            title = stringResource(R.string.appearance_pure_black),
                            summary = stringResource(R.string.appearance_pure_black_summary),
                            checked = appearance.pureBlackEnabled,
                            onCheckedChange = { enabled ->
                                update { current -> current.copy(pureBlackEnabled = enabled) }
                            },
                        )
                    }
                }
            }
        }

        item(key = "appearance_interface_title") {
            EtaPreferenceGroupTitle(text = stringResource(R.string.appearance_group_interface))
        }
        item(key = "appearance_interface_card") {
            EtaPreferenceGroup(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                EtaSwitchPreference(
                    title = stringResource(R.string.appearance_blur),
                    summary = stringResource(R.string.appearance_blur_summary),
                    checked = appearance.blurEnabled && blurSupported,
                    onCheckedChange = { enabled ->
                        update { current -> current.copy(blurEnabled = enabled) }
                    },
                    enabled = blurSupported,
                )
                AnimatedVisibility(
                    visible = appearance.blurEnabled && blurSupported,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                ) {
                    // AnimatedVisibility 的内容按层叠摆放，分割线和行必须包进 Column 才会上下排列。
                    Column {
                        EtaPreferenceDivider(hasLeading = false)
                        EtaOverlayDropdownPreference(
                            title = stringResource(R.string.appearance_blur_style),
                            summary = blurStyleSummaries[appearance.topBarBlurStyle.ordinal],
                            items = blurStyleLabels,
                            selectedIndex = appearance.topBarBlurStyle.ordinal,
                            onSelectedIndexChange = { index ->
                                blurStyles.getOrNull(index)?.let { style ->
                                    update { current -> current.copy(topBarBlurStyle = style) }
                                }
                            },
                        )
                    }
                }
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(
                    title = "顶栏透明度",
                    summary = "调低可让顶栏更透出壁纸",
                    endActions = {
                        Text(
                            text = "${(topBarAlphaDraft * 100f).roundToInt()}%",
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                        )
                    },
                    bottomAction = {
                        Slider(
                            value = topBarAlphaDraft.coerceIn(0.15f, 1f),
                            onValueChange = { topBarAlphaDraft = it },
                            modifier = Modifier.fillMaxWidth(),
                            valueRange = 0.15f..1f,
                            onValueChangeFinished = {
                                update { it.copy(topBarAlpha = topBarAlphaDraft) }
                            },
                            showKeyPoints = true,
                            keyPoints = listOf(0.15f, 0.4f, 0.7f, 1f),
                            hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                        )
                    },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(
                    title = "侧边栏透明度",
                    summary = "调低可让会话侧边栏更透出壁纸",
                    endActions = {
                        Text(
                            text = "${(paneAlphaDraft * 100f).roundToInt()}%",
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                        )
                    },
                    bottomAction = {
                        Slider(
                            value = paneAlphaDraft.coerceIn(0.2f, 1f),
                            onValueChange = { paneAlphaDraft = it },
                            modifier = Modifier.fillMaxWidth(),
                            valueRange = 0.2f..1f,
                            onValueChangeFinished = {
                                update { it.copy(conversationPaneAlpha = paneAlphaDraft) }
                            },
                            showKeyPoints = true,
                            keyPoints = listOf(0.2f, 0.5f, 0.8f, 1f),
                            hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                        )
                    },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaSwitchPreference(
                    title = stringResource(R.string.appearance_swipe_dismiss),
                    summary = stringResource(R.string.appearance_swipe_dismiss_summary),
                    checked = appearance.swipeDismissEnabled,
                    onCheckedChange = { enabled ->
                        update { current -> current.copy(swipeDismissEnabled = enabled) }
                    },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaSwitchPreference(
                    title = stringResource(R.string.appearance_predictive_back),
                    summary = stringResource(R.string.appearance_predictive_back_summary),
                    checked = appearance.predictiveBackEnabled,
                    onCheckedChange = { enabled ->
                        update { current -> current.copy(predictiveBackEnabled = enabled) }
                    },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(
                    title = stringResource(R.string.appearance_interface_scale),
                    summary = stringResource(R.string.appearance_interface_scale_summary),
                    endActions = {
                        Text(
                            text = "${scaleDraft.roundToInt()}%",
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                        )
                    },
                    bottomAction = {
                        Slider(
                            value = scaleDraft.coerceIn(
                                MIN_INTERFACE_SCALE * 100f,
                                MAX_INTERFACE_SCALE * 100f,
                            ),
                            onValueChange = { scaleDraft = it },
                            modifier = Modifier.fillMaxWidth(),
                            valueRange = (MIN_INTERFACE_SCALE * 100f)..(MAX_INTERFACE_SCALE * 100f),
                            onValueChangeFinished = { commitScale(scaleDraft) },
                            showKeyPoints = true,
                            keyPoints = listOf(80f, 90f, 100f, 110f),
                            magnetThreshold = 0.01f,
                            hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                        )
                    },
                    onClick = {
                        scaleInput = scaleDraft.roundToInt().toString()
                        showScaleDialog = true
                    },
                    holdDownState = showScaleDialog,
                )
            }
        }
        item(key = "appearance_wallpaper_title") {
            EtaPreferenceGroupTitle(text = "壁纸与背景")
        }
        item(key = "appearance_wallpaper_card") {
            EtaPreferenceGroup(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                EtaSwitchPreference(
                    title = "使用自定义壁纸",
                    summary = "把图片铺满作为应用背景",
                    checked = appearance.wallpaperEnabled,
                    onCheckedChange = { enabled -> update { it.copy(wallpaperEnabled = enabled) } },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(
                    title = "选择壁纸",
                    summary = if (appearance.wallpaperPath.isBlank()) "未选择" else "已选择",
                    onClick = {
                        wallpaperPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                )
                if (appearance.wallpaperPath.isNotBlank()) {
                    EtaPreferenceDivider(hasLeading = false)
                    EtaArrowPreference(
                        title = "壁纸压暗程度",
                        summary = "压暗背景层，保证正文对比度",
                        endActions = {
                            Text(
                                text = "${(dimDraft * 100f).roundToInt()}%",
                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                            )
                        },
                        bottomAction = {
                            Slider(
                                value = dimDraft.coerceIn(0f, MAX_WALLPAPER_DIM),
                                onValueChange = { dimDraft = it },
                                modifier = Modifier.fillMaxWidth(),
                                valueRange = 0f..MAX_WALLPAPER_DIM,
                                onValueChangeFinished = { update { it.copy(wallpaperDim = dimDraft) } },
                                showKeyPoints = true,
                                keyPoints = listOf(0f, 0.25f, 0.5f, 0.75f),
                                hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                            )
                        },
                    )
                    EtaPreferenceDivider(hasLeading = false)
                    EtaArrowPreference(
                        title = "壁纸模糊",
                        summary = "模糊背景，让内容更突出",
                        endActions = {
                            Text(
                                text = "${blurDraft.roundToInt()}dp",
                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                            )
                        },
                        bottomAction = {
                            Slider(
                                value = blurDraft.coerceIn(MIN_WALLPAPER_BLUR, MAX_WALLPAPER_BLUR),
                                onValueChange = { blurDraft = it },
                                modifier = Modifier.fillMaxWidth(),
                                valueRange = MIN_WALLPAPER_BLUR..MAX_WALLPAPER_BLUR,
                                onValueChangeFinished = { update { it.copy(wallpaperBlur = blurDraft) } },
                                showKeyPoints = true,
                                keyPoints = listOf(0f, 8f, 16f, 24f),
                                hapticEffect = SliderDefaults.SliderHapticEffect.Step,
                            )
                        },
                    )
                    EtaPreferenceDivider(hasLeading = false)
                    EtaArrowPreference(
                        title = "壁纸缩放方式",
                        summary = appearance.wallpaperScaleMode.label,
                        endActions = {
                            Text(
                                text = appearance.wallpaperScaleMode.label,
                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                            )
                        },
                        onClick = {
                            update {
                                it.copy(
                                    wallpaperScaleMode = if (it.wallpaperScaleMode == WallpaperScaleMode.CROP) {
                                        WallpaperScaleMode.FIT
                                    } else {
                                        WallpaperScaleMode.CROP
                                    },
                                )
                            }
                        },
                    )
                    EtaPreferenceDivider(hasLeading = false)
                    EtaArrowPreference(
                        title = "移除壁纸",
                        summary = "清空当前壁纸图片",
                        onClick = { update { it.copy(wallpaperPath = "") } },
                    )
                }
            }
        }
        item(key = "appearance_bubble_title") {
            EtaPreferenceGroupTitle(text = "聊天气泡")
        }
        item(key = "appearance_bubble_card") {
            EtaPreferenceGroup(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                EtaArrowPreference(
                    title = "编辑气泡样式",
                    summary = "分别设置用户与 AI 气泡的颜色、圆角与内边距",
                    onClick = { showBubbleDialog = true },
                )
            }
        }
        item(key = "appearance_avatar_title") {
            EtaPreferenceGroupTitle(text = "双方头像")
        }
        item(key = "appearance_avatar_card") {
            EtaPreferenceGroup(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                EtaSwitchPreference(
                    title = "我的头像",
                    summary = "在我的消息旁显示头像",
                    checked = appearance.avatarMineEnabled,
                    onCheckedChange = { enabled -> update { it.copy(avatarMineEnabled = enabled) } },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(
                    title = "更换图片",
                    summary = "我的头像",
                    onClick = {
                        avatarMinePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(
                    title = "移除图片",
                    summary = "我的头像",
                    onClick = { update { it.copy(avatarMinePath = "") } },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaSwitchPreference(
                    title = "AI 头像",
                    summary = "在 AI 回复旁显示头像",
                    checked = appearance.avatarAiEnabled,
                    onCheckedChange = { enabled -> update { it.copy(avatarAiEnabled = enabled) } },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(
                    title = "更换图片",
                    summary = "AI 头像",
                    onClick = {
                        avatarAiPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                )
                EtaPreferenceDivider(hasLeading = false)
                EtaArrowPreference(
                    title = "移除图片",
                    summary = "AI 头像",
                    onClick = { update { it.copy(avatarAiPath = "") } },
                )
            }
        }
    }

    val parsedScale = scaleInput.toIntOrNull()
    EtaWindowDialog(
        show = showScaleDialog,
        title = stringResource(R.string.appearance_interface_scale_dialog_title),
        summary = stringResource(R.string.appearance_interface_scale_dialog_summary),
        onDismissRequest = { showScaleDialog = false },
    ) {
        Column {
            TextField(
                value = scaleInput,
                onValueChange = { value -> scaleInput = value.filter(Char::isDigit).take(3) },
                label = stringResource(R.string.appearance_interface_scale_input_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            MiuixDialogActions(
                confirmText = stringResource(R.string.action_confirm),
                confirmEnabled = parsedScale != null && parsedScale in 80..110,
                onCancel = { showScaleDialog = false },
                onConfirm = {
                    parsedScale?.let { commitScale(it.toFloat()) }
                    showScaleDialog = false
                },
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }

    // 气泡样式编辑弹窗
    val bubbleColorChoices = listOf(
        0L to "跟随主题",
        0xFF3B82F6L to "蓝",
        0xFF8B5CF6L to "紫",
        0xFF10B981L to "绿",
        0xFFF97316L to "橙",
        0xFFEC4899L to "粉",
    )
    EtaWindowDialog(
        show = showBubbleDialog,
        title = "编辑气泡样式",
        summary = "分别设置用户与 AI 气泡的颜色、圆角与内边距",
        onDismissRequest = { showBubbleDialog = false },
    ) {
        Column {
            Text("用户气泡颜色", style = MiuixTheme.textStyles.body2)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                bubbleColorChoices.forEach { (argb, _) ->
                    val selected = appearance.bubbleUserColor == argb
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (argb == 0L) MiuixTheme.colorScheme.surfaceContainerHigh else Color(argb),
                            )
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                },
                                shape = CircleShape,
                            )
                            .clickable { update { it.copy(bubbleUserColor = argb) } },
                    )
                }
            }
            Text("AI 气泡颜色", style = MiuixTheme.textStyles.body2)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                bubbleColorChoices.forEach { (argb, _) ->
                    val selected = appearance.bubbleAiColor == argb
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (argb == 0L) MiuixTheme.colorScheme.surfaceContainerHigh else Color(argb),
                            )
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) {
                                    MiuixTheme.colorScheme.primary
                                } else {
                                    MiuixTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                },
                                shape = CircleShape,
                            )
                            .clickable { update { it.copy(bubbleAiColor = argb) } },
                    )
                }
            }
            Text("圆角 ${bubbleCornerDraft.roundToInt()}dp", style = MiuixTheme.textStyles.body2)
            Slider(
                value = bubbleCornerDraft,
                onValueChange = { bubbleCornerDraft = it },
                modifier = Modifier.fillMaxWidth(),
                valueRange = 4f..28f,
                onValueChangeFinished = { update { it.copy(bubbleCornerRadius = bubbleCornerDraft) } },
            )
            Text("内边距 ${bubblePaddingDraft.roundToInt()}dp", style = MiuixTheme.textStyles.body2)
            Slider(
                value = bubblePaddingDraft,
                onValueChange = { bubblePaddingDraft = it },
                modifier = Modifier.fillMaxWidth(),
                valueRange = 8f..20f,
                onValueChangeFinished = { update { it.copy(bubblePadding = bubblePaddingDraft) } },
            )
            Text(
                "用户气泡不透明度 ${(bubbleUserAlphaDraft * 100f).roundToInt()}%",
                style = MiuixTheme.textStyles.body2,
            )
            Slider(
                value = bubbleUserAlphaDraft.coerceIn(0.2f, 1f),
                onValueChange = { bubbleUserAlphaDraft = it },
                modifier = Modifier.fillMaxWidth(),
                valueRange = 0.2f..1f,
                onValueChangeFinished = {
                    update { it.copy(bubbleUserAlpha = bubbleUserAlphaDraft) }
                },
            )
            Text(
                "AI 气泡不透明度 ${(bubbleAiAlphaDraft * 100f).roundToInt()}%",
                style = MiuixTheme.textStyles.body2,
            )
            Slider(
                value = bubbleAiAlphaDraft.coerceIn(0.2f, 1f),
                onValueChange = { bubbleAiAlphaDraft = it },
                modifier = Modifier.fillMaxWidth(),
                valueRange = 0.2f..1f,
                onValueChangeFinished = {
                    update { it.copy(bubbleAiAlpha = bubbleAiAlphaDraft) }
                },
            )
            MiuixDialogActions(
                confirmText = "完成",
                onCancel = { showBubbleDialog = false },
                onConfirm = { showBubbleDialog = false },
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}


private fun copyPickedImage(context: Context, uri: Uri, baseName: String): String? {
    return runCatching {
        val dir = File(context.filesDir, "appearance").apply { mkdirs() }
        val out = File(dir, baseName + "-" + System.currentTimeMillis() + ".img")
        context.contentResolver.openInputStream(uri)?.use { input ->
            out.outputStream().use { output -> input.copyTo(output) }
        }
        out.absolutePath
    }.getOrNull()
}
