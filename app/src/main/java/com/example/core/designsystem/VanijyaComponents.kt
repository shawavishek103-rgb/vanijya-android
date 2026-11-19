package com.example.core.designsystem

import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Money
import com.example.ui.theme.AmberSale
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.IndigoDeep
import com.example.ui.theme.LocalVanijyaGlass
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.PurchaseBlue
import com.example.ui.theme.ToPayRed
import com.example.ui.theme.VioletPrimary
import kotlin.math.roundToLong

/**
 * Checks whether system animations are reduced/disabled by the user.
 */
@Composable
fun rememberReduceMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            scale == 0f
        } catch (_: Throwable) {
            false
        }
    }
}

/**
 * Full-screen glassmorphic backdrop container:
 * - Dark theme: deep navy-to-indigo gradient with soft blurred colour glows behind the content.
 * - Light theme: warm ivory/cream gradient with soft mint and sky-blue glows.
 * - AMOLED Black: pure #000000 background with subtle ambient edge glow.
 */
@Composable
fun GlassBackgroundBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalVanijyaGlass.current
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    CompositionLocalProvider(LocalContentColor provides onSurfaceColor) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            glass.backgroundGradientStart,
                            glass.backgroundGradientMid,
                            glass.backgroundGradientEnd
                        )
                    )
                )
        ) {
            // Blurred ambient colour glow orbs behind content (RenderEffect blur on Android 12+, radial brush fallback on older APIs)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 64.dp)
            ) {
                val w = size.width
                val h = size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glass.glowTopStart, Color.Transparent),
                        center = Offset(w * 0.18f, h * 0.12f),
                        radius = (w * 0.65f).coerceAtLeast(100f)
                    ),
                    radius = (w * 0.65f).coerceAtLeast(100f),
                    center = Offset(w * 0.18f, h * 0.12f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glass.glowTopEnd, Color.Transparent),
                        center = Offset(w * 0.85f, h * 0.24f),
                        radius = (w * 0.60f).coerceAtLeast(100f)
                    ),
                    radius = (w * 0.60f).coerceAtLeast(100f),
                    center = Offset(w * 0.85f, h * 0.24f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glass.glowBottomCenter, Color.Transparent),
                        center = Offset(w * 0.50f, h * 0.78f),
                        radius = (w * 0.70f).coerceAtLeast(100f)
                    ),
                    radius = (w * 0.70f).coerceAtLeast(100f),
                    center = Offset(w * 0.50f, h * 0.78f)
                )
            }
            content()
        }
    }
}

/**
 * Reusable frosted GlassCard:
 * - Translucent fill (~12-20% white in dark, ~55-70% white in light, thin border in AMOLED)
 * - 1dp light specular gradient border
 * - 24-28dp corner radius and soft shadow
 * - Sets LocalContentColor to MaterialTheme.colorScheme.onSurface so all child text/icons are readable in Light, Dark, and AMOLED
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    elevated: Boolean = false,
    accentTint: Color? = null,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val glass = LocalVanijyaGlass.current
    val view = LocalView.current
    val shape = RoundedCornerShape(cornerRadius)
    val baseFill = if (elevated) glass.cardFillElevated else glass.cardFill
    val borderStart = accentTint?.copy(alpha = 0.52f) ?: glass.borderGradientStart
    val borderEnd = accentTint?.copy(alpha = 0.16f) ?: glass.borderGradientEnd
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    CompositionLocalProvider(LocalContentColor provides onSurfaceColor) {
        Column(
            modifier = modifier
                .shadow(
                    elevation = if (glass.isAmoled) 0.dp else 10.dp,
                    shape = shape,
                    ambientColor = glass.shadowColor,
                    spotColor = glass.shadowColor
                )
                .clip(shape)
                .background(
                    brush = if (accentTint != null) {
                        Brush.linearGradient(
                            colors = listOf(
                                accentTint.copy(alpha = if (glass.isDark) 0.18f else 0.12f),
                                baseFill
                            )
                        )
                    } else {
                        Brush.linearGradient(colors = listOf(baseFill, baseFill))
                    }
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(colors = listOf(borderStart, borderEnd)),
                    shape = shape
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            onClick()
                        }
                    } else {
                        Modifier
                    }
                )
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Reusable frosted GlassSheet (ModalBottomSheet with glassmorphic surface, border, and LocalContentColor).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetContainer = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = sheetContainer,
        contentColor = onSurfaceColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalContentColor provides onSurfaceColor) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = onSurfaceColor
                    )
                }
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                content()
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Theme-aware OutlinedTextField colors ensuring >= 4.5:1 contrast for input text, label, placeholder,
 * supporting text, icons, cursor, and border in Light, Dark, and AMOLED themes.
 */
@Composable
fun vanijyaTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    errorTextColor = MaterialTheme.colorScheme.error,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    errorLabelColor = MaterialTheme.colorScheme.error,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    errorSupportingTextColor = MaterialTheme.colorScheme.error,
    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
    errorBorderColor = MaterialTheme.colorScheme.error
)

/**
 * Theme-aware FilterChip colors ensuring >= 4.5:1 contrast for selected and unselected chips
 * in Light, Dark, and AMOLED themes.
 */
@Composable
fun vanijyaFilterChipColors(): SelectableChipColors = FilterChipDefaults.filterChipColors(
    containerColor = LocalVanijyaGlass.current.pillFill,
    labelColor = MaterialTheme.colorScheme.onSurface,
    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
)

/**
 * Theme-aware Switch colors ensuring clear visibility in Light, Dark, and AMOLED themes.
 */
@Composable
fun vanijyaSwitchColors(): SwitchColors = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedBorderColor = MaterialTheme.colorScheme.primary,
    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    uncheckedBorderColor = MaterialTheme.colorScheme.outline
)

/**
 * Theme-aware Checkbox colors ensuring clear visibility in Light, Dark, and AMOLED themes.
 */
@Composable
fun vanijyaCheckboxColors(): CheckboxColors = CheckboxDefaults.colors(
    checkedColor = MaterialTheme.colorScheme.primary,
    checkmarkColor = MaterialTheme.colorScheme.onPrimary,
    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
)

/**
 * Reusable GlassTile for quick actions (Sale in amber/orange, Purchase in blue, Quotation in grey/silver)
 * and feature grids, with rounded icon, subtle gradient, spring press animation, and haptic feedback.
 */
@Composable
fun GlassTile(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badgeText: String? = null,
    testTag: String = "glass_tile_${label.lowercase().replace(" ", "_")}"
) {
    val glass = LocalVanijyaGlass.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "glass_tile_scale"
    )
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minWidth = 78.dp, minHeight = 88.dp)
            .shadow(
                elevation = if (glass.isAmoled) 0.dp else 8.dp,
                shape = shape,
                ambientColor = glass.shadowColor,
                spotColor = glass.shadowColor
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = if (glass.isDark) 0.24f else 0.16f),
                        glass.cardFill
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.55f),
                        glass.borderGradientEnd
                    )
                ),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 14.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.28f),
                            accentColor.copy(alpha = 0.12f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = 0.40f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
        if (!badgeText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = accentColor.copy(alpha = 0.18f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Animated circular RingChart showing Cash vs Bank share on the Hero Glass Card.
 */
@Composable
fun RingChart(
    cashAmount: Money,
    bankAmount: Money,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    strokeWidth: Dp = 10.dp,
    cashColor: Color = EmeraldPrimary,
    bankColor: Color = PurchaseBlue
) {
    val reduceMotion = rememberReduceMotionEnabled()
    val totalPaise = (cashAmount.paise + bankAmount.paise).coerceAtLeast(0L)
    val targetCashShare = if (totalPaise == 0L) 0.55f else (cashAmount.paise.toFloat() / totalPaise.toFloat()).coerceIn(0.08f, 0.92f)
    val animatedCashShare by animateFloatAsState(
        targetValue = targetCashShare,
        animationSpec = if (reduceMotion) tween(0) else tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "ring_cash_share"
    )
    val cashPercent = if (totalPaise == 0L) 55 else ((cashAmount.paise * 100.0) / totalPaise.toDouble()).roundToLong().toInt()

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Cash vs Bank Balance Share Chart: Cash $cashPercent percent" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f + 2f
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            val topLeft = Offset(inset, inset)

            // Track ring
            drawArc(
                color = bankColor.copy(alpha = 0.25f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Bank share arc
            val cashSweep = animatedCashShare * 336f
            val bankSweep = (336f - cashSweep).coerceAtLeast(18f)
            drawArc(
                color = bankColor,
                startAngle = -90f + cashSweep + 12f,
                sweepAngle = bankSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Cash share arc
            drawArc(
                color = cashColor,
                startAngle = -90f,
                sweepAngle = cashSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$cashPercent%",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = cashColor
            )
            Text(
                text = "Cash",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Small animated up-trend or down-trend sparkline badge for money-in (emerald) and money-out (coral).
 */
@Composable
fun MiniTrendLine(
    isPositive: Boolean = true,
    color: Color = if (isPositive) EmeraldPrimary else CoralError,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .width(34.dp)
            .height(14.dp)
            .semantics { contentDescription = if (isPositive) "Upward trend" else "Downward trend" }
    ) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            if (isPositive) {
                moveTo(0f, h * 0.85f)
                lineTo(w * 0.33f, h * 0.55f)
                lineTo(w * 0.62f, h * 0.68f)
                lineTo(w, h * 0.15f)
            } else {
                moveTo(0f, h * 0.20f)
                lineTo(w * 0.35f, h * 0.50f)
                lineTo(w * 0.65f, h * 0.38f)
                lineTo(w, h * 0.85f)
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )
        val endY = if (isPositive) h * 0.15f else h * 0.85f
        drawCircle(
            color = color,
            radius = 3.5f,
            center = Offset(w, endY)
        )
    }
}

/**
 * Reusable StatusChip for billing documents across all segments:
 * - Sales & Purchases: Paid (emerald), Partial (amber), Unpaid (coral), Overdue (red), Cancelled (grey), Draft (grey outline)
 * - Quotations: Open (blue), Accepted (emerald), Converted (violet with link icon), Rejected (coral), Expired (grey)
 * - Orders: Pending (amber), Partially fulfilled (amber), Completed (emerald)
 * - Challans: Pending (amber), Invoiced (emerald)
 * - Returns & Payments: Adjusted (emerald), Refunded (coral), Pending (amber), Cleared (emerald), Bounced (red)
 *
 * Strictly enforces maxLines = 1, softWrap = false, wrap-content width, 10dp horizontal & 4dp vertical padding,
 * 12sp medium text, small leading dot or link icon, and fully rounded corners (CircleShape).
 */
@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier,
    showLinkIcon: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val glass = LocalVanijyaGlass.current
    val normalized = status.trim().uppercase()
    val isDraftOutline = normalized == "DRAFT"
    val isConverted = normalized.contains("CONVERTED") || showLinkIcon

    val baseAccent: Color
    val textColor: Color
    when {
        isDraftOutline -> {
            baseAccent = MaterialTheme.colorScheme.outline
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
        normalized == "CANCELLED" || normalized == "EXPIRED" -> {
            baseAccent = MaterialTheme.colorScheme.onSurfaceVariant
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
        isConverted -> {
            baseAccent = VioletPrimary
            textColor = if (glass.isDark) Color(0xFFD8B4FE) else Color(0xFF5B21B6)
        }
        normalized == "OPEN" -> {
            baseAccent = PurchaseBlue
            textColor = if (glass.isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
        }
        (normalized.contains("PAID") && !normalized.contains("UNPAID")) ||
            normalized == "ACCEPTED" ||
            normalized == "COMPLETED" ||
            normalized == "INVOICED" ||
            normalized == "ADJUSTED" ||
            normalized == "CLEARED" -> {
            baseAccent = EmeraldPrimary
            textColor = if (glass.isDark) Color(0xFF34D399) else Color(0xFF047857)
        }
        normalized.contains("PARTIAL") ||
            normalized == "PENDING" ||
            normalized == "IN_HAND" ||
            normalized == "IN HAND" ||
            normalized == "DEPOSITED" -> {
            baseAccent = GoldPrimary
            textColor = if (glass.isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
        }
        normalized.contains("OVERDUE") ||
            normalized.contains("BOUNCED") ||
            normalized.contains("TO PAY") -> {
            baseAccent = ToPayRed
            textColor = if (glass.isDark) Color(0xFFF87171) else Color(0xFF991B1B)
        }
        normalized.contains("UNPAID") ||
            normalized == "REJECTED" ||
            normalized == "REFUNDED" -> {
            baseAccent = CoralError
            textColor = if (glass.isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
        }
        else -> {
            baseAccent = AmberSale
            textColor = if (glass.isDark) Color(0xFFFDBA74) else Color(0xFF9A3412)
        }
    }

    val fillColor = if (isDraftOutline) {
        Color.Transparent
    } else {
        baseAccent.copy(alpha = if (glass.isDark) 0.18f else 0.14f)
    }
    val borderColor = if (isDraftOutline) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)
    } else {
        baseAccent.copy(alpha = if (glass.isDark) 0.45f else 0.35f)
    }

    Surface(
        color = fillColor,
        shape = CircleShape,
        modifier = modifier
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = CircleShape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (isConverted) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(12.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(textColor)
                )
            }
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = textColor,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * Reusable SegmentedTabs bar with frosted glass container and animated pill selection indicator.
 */
@Composable
fun SegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    testTagPrefix: String = "segmented_tab"
) {
    val glass = LocalVanijyaGlass.current
    val view = LocalView.current
    val containerShape = RoundedCornerShape(20.dp)

    val rowModifier = if (scrollable) {
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    } else {
        Modifier.fillMaxWidth()
    }

    Box(
        modifier = modifier
            .clip(containerShape)
            .background(glass.pillFill)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(glass.borderGradientStart, glass.borderGradientEnd)
                ),
                shape = containerShape
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    animationSpec = tween(durationMillis = 220),
                    label = "segment_bg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 220),
                    label = "segment_fg"
                )
                Box(
                    modifier = (if (!scrollable) Modifier.weight(1f) else Modifier)
                        .defaultMinSize(minHeight = 40.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bgColor)
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            onTabSelected(index)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("${testTagPrefix}_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = textColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Data model for the 5-tab Floating Glass Bottom Navigation Bar.
 */
data class GlassNavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val matchRoutes: Set<String> = setOf(route)
)

/**
 * Floating pill-shaped glass bottom navigation bar with 5 tabs and filled/outlined icon states
 * with an animated selection indicator: Dashboard, Billing, Parties, Reports, Settings.
 */
@Composable
fun FloatingGlassNavBar(
    destinations: List<GlassNavDestination>,
    currentRoute: String,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalVanijyaGlass.current
    val view = LocalView.current
    val shape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (glass.isAmoled) 0.dp else 20.dp,
                shape = shape,
                ambientColor = glass.shadowColor,
                spotColor = glass.shadowColor
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        glass.cardFillElevated,
                        glass.cardFill
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glass.borderGradientStart,
                        VioletPrimary.copy(alpha = 0.35f),
                        EmeraldPrimary.copy(alpha = 0.28f)
                    )
                ),
                shape = shape
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .testTag("floating_glass_nav_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { dest ->
                val isSelected = currentRoute in dest.matchRoutes
                val pillAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "nav_pill_alpha"
                )
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "nav_icon_scale"
                )
                val activeTint = MaterialTheme.colorScheme.onSurface
                val inactiveTint = MaterialTheme.colorScheme.onSurfaceVariant

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 52.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        VioletPrimary.copy(alpha = 0.28f * pillAlpha),
                                        EmeraldPrimary.copy(alpha = 0.18f * pillAlpha)
                                    )
                                )
                            } else {
                                Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = glass.borderGradientStart.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            onSelectRoute(dest.route)
                        }
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                        .testTag(dest.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                        contentDescription = dest.label,
                        tint = if (isSelected) activeTint else inactiveTint,
                        modifier = Modifier
                            .size(22.dp)
                            .scale(iconScale)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) activeTint else inactiveTint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * In-app vector wordmark + brand mark ("Vanijya" + ascending emerald/white V + gold dot).
 */
@Composable
fun VanijyaWordmark(
    modifier: Modifier = Modifier,
    showTagline: Boolean = false,
    iconSize: Dp = 40.dp
) {
    Row(
        modifier = modifier.semantics { contentDescription = "Vanijya Brand Logo" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        VanijyaVectorLogoMark(size = iconSize)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Vanijya",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                )
            }
            if (showTagline) {
                Text(
                    text = "Your business, in your pocket. Fully offline.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Pure Compose Canvas vector rendering of the Vanijya rounded-square logo:
 * Indigo-to-violet gradient (#2A1B6B to #5B3FD9), left bar emerald (#0FB57F),
 * right bar white (#FFFFFF), and gold dot (#F2B134) above the right bar.
 */
@Composable
fun VanijyaVectorLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Vanijya V Logo" }
    ) {
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(IndigoDeep, VioletPrimary),
                start = Offset.Zero,
                end = Offset(w, h)
            ),
            cornerRadius = CornerRadius(w * 0.26f, h * 0.26f)
        )

        // Left ascending bar in Emerald (#0FB57F)
        val leftBar = Path().apply {
            moveTo(w * 0.26f, h * 0.30f)
            lineTo(w * 0.40f, h * 0.30f)
            lineTo(w * 0.52f, h * 0.72f)
            lineTo(w * 0.39f, h * 0.76f)
            close()
        }
        drawPath(path = leftBar, color = EmeraldPrimary)

        // Right ascending bar in White (#FFFFFF)
        val rightBar = Path().apply {
            moveTo(w * 0.46f, h * 0.76f)
            lineTo(w * 0.62f, h * 0.30f)
            lineTo(w * 0.76f, h * 0.30f)
            lineTo(w * 0.58f, h * 0.76f)
            close()
        }
        drawPath(path = rightBar, color = Color.White)

        // Gold dot (#F2B134) above right bar
        drawCircle(
            color = GoldPrimary,
            radius = w * 0.065f,
            center = Offset(w * 0.69f, h * 0.20f)
        )
    }
}

/**
 * Strips any accidental leading '+' (e.g. "+ Add Item" -> "Add Item") when an icon is already shown,
 * preventing double-plus ("+ +") bugs across all buttons, FABs, and headers.
 */
fun sanitizeLeadingPlus(label: String): String =
    label.trimStart().removePrefix("+").trimStart()

/**
 * Reusable VanijyaExtendedFab that takes an icon and a plain label, strips any accidental leading '+',
 * and enforces single-line ellipsis so labels are never clipped or wrapped awkwardly on small screens.
 */
@Composable
fun VanijyaExtendedFab(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Add,
    containerColor: Color = EmeraldPrimary,
    contentColor: Color = Color.Unspecified,
    expanded: Boolean = true,
    testTag: String = "vanijya_extended_fab"
) {
    val view = LocalView.current
    val cleanLabel = remember(text) { sanitizeLeadingPlus(text) }
    val resolvedContentColor = when {
        contentColor != Color.Unspecified -> contentColor
        containerColor.luminance() > 0.45f -> Color(0xFF14192D)
        else -> Color(0xFFFFFFFF)
    }

    ExtendedFloatingActionButton(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onClick()
        },
        expanded = expanded,
        containerColor = containerColor,
        contentColor = resolvedContentColor,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = cleanLabel,
                modifier = Modifier.size(20.dp)
            )
        },
        text = {
            Text(
                text = cleanLabel,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag)
    )
}

/**
 * Reusable VanijyaAddButton that always pairs an icon (default Icons.Default.Add) with a plain label
 * (automatically stripping any accidental leading '+'). Supports both TextButton and Outlined/Filled button styles.
 */
@Composable
fun VanijyaAddButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Add,
    isTextButton: Boolean = false,
    isOutlined: Boolean = true,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.Unspecified,
    testTag: String = "vanijya_add_button_${sanitizeLeadingPlus(text)}"
) {
    val cleanLabel = remember(text) { sanitizeLeadingPlus(text) }
    if (isTextButton) {
        val resolvedColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.primary
        TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .defaultMinSize(minHeight = 48.dp)
                .minimumInteractiveComponentSize()
                .testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = cleanLabel,
                tint = resolvedColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = cleanLabel,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = resolvedColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    } else {
        VanijyaButton(
            text = cleanLabel,
            onClick = onClick,
            modifier = modifier,
            icon = icon,
            enabled = enabled,
            isOutlined = isOutlined,
            containerColor = containerColor,
            contentColor = contentColor,
            testTag = testTag
        )
    }
}

/**
 * Reusable VanijyaButton with spring scale animation, haptic feedback, and minimum 48dp touch target.
 * Automatically strips any leading '+' when an icon is provided so icon + text never renders "+ +".
 */
@Composable
fun VanijyaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isOutlined: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.Unspecified,
    testTag: String = "vanijya_button_$text"
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "button_scale"
    )

    val displayLabel = remember(text, icon) {
        if (icon != null) sanitizeLeadingPlus(text) else text
    }

    val resolvedContentColor = when {
        contentColor != Color.Unspecified -> contentColor
        containerColor == MaterialTheme.colorScheme.primary -> MaterialTheme.colorScheme.onPrimary
        containerColor.luminance() > 0.45f -> Color(0xFF14192D)
        else -> Color(0xFFFFFFFF)
    }

    val triggerClick = {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        onClick()
    }

    if (isOutlined) {
        val outlinedTextColor = MaterialTheme.colorScheme.onSurface
        OutlinedButton(
            onClick = triggerClick,
            enabled = enabled,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = outlinedTextColor
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            modifier = modifier
                .scale(scale)
                .defaultMinSize(minHeight = 48.dp)
                .minimumInteractiveComponentSize()
                .testTag(testTag)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = displayLabel,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = displayLabel,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = outlinedTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    } else {
        Button(
            onClick = triggerClick,
            enabled = enabled,
            interactionSource = interactionSource,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = resolvedContentColor
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            modifier = modifier
                .scale(scale)
                .defaultMinSize(minHeight = 48.dp)
                .minimumInteractiveComponentSize()
                .testTag(testTag)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = displayLabel,
                    tint = resolvedContentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = displayLabel,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = resolvedContentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Reusable AmountText component using tabular numerals (`tnum`) and Indian digit grouping (`3,53,000.00`),
 * with smooth count-up animation that respects system reduce-motion.
 */
@Composable
fun AmountText(
    amount: Money,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    decimals: Int = 2,
    masked: Boolean = false,
    includeSymbol: Boolean = true,
    prefix: String = "",
    animateCountUp: Boolean = false
) {
    val reduceMotion = rememberReduceMotionEnabled()
    val animatedPaiseFloat by animateFloatAsState(
        targetValue = amount.paise.toFloat(),
        animationSpec = if (!animateCountUp || reduceMotion) {
            tween(durationMillis = 0)
        } else {
            tween(durationMillis = 550, easing = FastOutSlowInEasing)
        },
        label = "amount_count_up"
    )
    val displayMoney = if (!animateCountUp || reduceMotion) amount else Money(animatedPaiseFloat.roundToLong())
    val formatted = remember(displayMoney, decimals, masked, includeSymbol, prefix) {
        prefix + displayMoney.formatIndian(
            includeSymbol = includeSymbol,
            decimals = decimals,
            masked = masked
        )
    }
    Text(
        text = formatted,
        style = style.copy(
            fontFamily = PlusJakartaSansFamily,
            fontFeatureSettings = "tnum"
        ),
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.semantics {
            contentDescription = if (masked) "Amount hidden" else "Amount $formatted"
        }
    )
}

/**
 * Reusable StatTile glass card with tabular amount display, optional trend line, and change vs previous period.
 */
@Composable
fun StatTile(
    title: String,
    amount: Money,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    changeBadge: String? = null,
    showTrendLine: Boolean = false,
    isPositiveTrend: Boolean = true,
    masked: Boolean = false,
    decimals: Int = 2,
    onClick: (() -> Unit)? = null
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 24.dp,
        accentTint = accentColor,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            AmountText(
                amount = amount,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                decimals = decimals,
                masked = masked,
                animateCountUp = true
            )
            if (showTrendLine) {
                MiniTrendLine(
                    isPositive = isPositiveTrend,
                    color = accentColor
                )
            }
        }
        if (!subtitle.isNullOrBlank() || !changeBadge.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!changeBadge.isNullOrBlank()) {
                    Text(
                        text = changeBadge,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accentColor
                    )
                }
            }
        }
    }
}

/**
 * Reusable PartyCard component for customer/supplier balances (To Collect / To Pay).
 */
@Composable
fun PartyCard(
    partyName: String,
    phone: String,
    gstin: String?,
    balance: Money,
    isReceivable: Boolean,
    masked: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRemindClick: (() -> Unit)? = null
) {
    val accent = if (isReceivable) EmeraldPrimary else ToPayRed
    val badgeText = if (isReceivable) "To Collect" else "To Pay"

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f))
                        .border(1.dp, accent.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = partyName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = accent
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        text = partyName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val meta = buildString {
                        if (phone.isNotBlank()) append(phone)
                        if (!gstin.isNullOrBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append("GST: $gstin")
                        }
                    }
                    if (meta.isNotEmpty()) {
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isReceivable) {
                        MiniTrendLine(isPositive = true, color = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    AmountText(
                        amount = balance,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = accent,
                        masked = masked
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = accent
                    )
                    if (onRemindClick != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = GoldPrimary.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onRemindClick)
                        ) {
                            Text(
                                text = "Remind",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GoldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reusable DocumentActionsRow for the bottom of DocumentCard:
 * - Left: Status chips in a FlowRow so extra chips wrap cleanly to a new line (never squeezed into a narrow box).
 * - Right: Share PDF icon button, one contextual primary action button (Convert to Sale, Receive Payment, Pay, etc.),
 *   and the three-dot overflow menu (Edit, Duplicate, Print, Download PDF, Convert, Cancel, Delete with destructive items last).
 * - Automatically wraps the action buttons onto a second line below the chips when the card is narrow.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocumentActionsRow(
    statuses: List<String>,
    docNumber: String,
    modifier: Modifier = Modifier,
    primaryActionLabel: String? = null,
    onPrimaryActionClick: (() -> Unit)? = null,
    onSharePdfClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDuplicateClick: (() -> Unit)? = null,
    onPrintClick: (() -> Unit)? = null,
    onDownloadPdfClick: (() -> Unit)? = null,
    convertMenuLabel: String? = null,
    onConvertMenuClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    primaryActionTestTag: String = "convert_btn_$docNumber",
    moreMenuTestTag: String = "invoice_more_btn_$docNumber"
) {
    val glass = LocalVanijyaGlass.current
    var showOverflowMenu by remember { mutableStateOf(false) }
    val hasOverflowItems = onEditClick != null ||
        onDuplicateClick != null ||
        onPrintClick != null ||
        onDownloadPdfClick != null ||
        onConvertMenuClick != null ||
        onCancelClick != null ||
        onDeleteClick != null

    val primaryAccent = when {
        primaryActionLabel?.contains("Receive", ignoreCase = true) == true -> EmeraldPrimary
        primaryActionLabel?.equals("Pay", ignoreCase = true) == true -> PurchaseBlue
        else -> VioletPrimary
    }
    val primaryTextColor = when (primaryAccent) {
        EmeraldPrimary -> if (glass.isDark) Color(0xFF34D399) else Color(0xFF047857)
        PurchaseBlue -> if (glass.isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
        else -> if (glass.isDark) Color(0xFFD8B4FE) else Color(0xFF5B21B6)
    }

    val chipsContent: @Composable () -> Unit = {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            statuses.forEach { st ->
                StatusChip(
                    status = st,
                    showLinkIcon = st.equals("Converted", ignoreCase = true)
                )
            }
        }
    }

    val actionsContent: @Composable () -> Unit = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onSharePdfClick != null) {
                IconButton(
                    onClick = onSharePdfClick,
                    modifier = Modifier
                        .size(40.dp)
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(glass.pillFill)
                        .border(1.dp, glass.borderGradientStart, CircleShape)
                        .testTag("share_pdf_btn_$docNumber")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share PDF for $docNumber",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (!primaryActionLabel.isNullOrBlank() && onPrimaryActionClick != null) {
                Surface(
                    color = primaryAccent.copy(alpha = if (glass.isDark) 0.20f else 0.14f),
                    shape = CircleShape,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 36.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = primaryAccent.copy(alpha = 0.45f),
                            shape = CircleShape
                        )
                        .clickable(onClick = onPrimaryActionClick)
                        .testTag(primaryActionTestTag)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = primaryActionLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = primaryTextColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (hasOverflowItems) {
                Box {
                    IconButton(
                        onClick = { showOverflowMenu = true },
                        modifier = Modifier
                            .size(40.dp)
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(glass.pillFill)
                            .border(1.dp, glass.borderGradientStart, CircleShape)
                            .testTag(moreMenuTestTag)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More actions for $docNumber",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        if (onEditClick != null) {
                            DropdownMenuItem(
                                text = { Text("Edit", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PurchaseBlue) },
                                onClick = {
                                    showOverflowMenu = false
                                    onEditClick()
                                }
                            )
                        }
                        if (onDuplicateClick != null) {
                            DropdownMenuItem(
                                text = { Text("Duplicate", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = EmeraldPrimary) },
                                onClick = {
                                    showOverflowMenu = false
                                    onDuplicateClick()
                                }
                            )
                        }
                        if (onPrintClick != null) {
                            DropdownMenuItem(
                                text = { Text("Print", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = { Icon(Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                onClick = {
                                    showOverflowMenu = false
                                    onPrintClick()
                                }
                            )
                        }
                        if (onDownloadPdfClick != null) {
                            DropdownMenuItem(
                                text = { Text("Download PDF", color = MaterialTheme.colorScheme.onSurface) },
                                leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = GoldPrimary) },
                                onClick = {
                                    showOverflowMenu = false
                                    onDownloadPdfClick()
                                }
                            )
                        }
                        if (!convertMenuLabel.isNullOrBlank() && onConvertMenuClick != null) {
                            DropdownMenuItem(
                                text = { Text(convertMenuLabel, color = VioletPrimary) },
                                leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = VioletPrimary) },
                                onClick = {
                                    showOverflowMenu = false
                                    onConvertMenuClick()
                                }
                            )
                        }
                        if (onCancelClick != null || onDeleteClick != null) {
                            HorizontalDivider(color = glass.subtleDivider)
                        }
                        if (onCancelClick != null) {
                            DropdownMenuItem(
                                text = { Text("Cancel", color = CoralError) },
                                leadingIcon = { Icon(Icons.Default.Cancel, contentDescription = null, tint = CoralError) },
                                onClick = {
                                    showOverflowMenu = false
                                    onCancelClick()
                                }
                            )
                        }
                        if (onDeleteClick != null) {
                            DropdownMenuItem(
                                text = { Text("Delete", color = CoralError) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = CoralError) },
                                onClick = {
                                    showOverflowMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val stackVertically = maxWidth < 360.dp ||
            (statuses.size >= 2 && !primaryActionLabel.isNullOrBlank() && maxWidth < 420.dp) ||
            ((primaryActionLabel?.length ?: 0) >= 14 && maxWidth < 390.dp)

        if (stackVertically) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chipsContent()
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    actionsContent()
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(end = 8.dp)
                ) {
                    chipsContent()
                }
                actionsContent()
            }
        }
    }
}

/**
 * Reusable single-card DocumentCard for all Billing tab segments (Sales, Purchases, Quotations,
 * Orders, Challans, Returns, Payments) and recent document lists:
 * - 16dp internal padding, 24dp corner radius, single self-contained glass card (nothing drawn between cards).
 * - Top row: document number on left (bold, 1 line, ellipsis) | total amount on right (bold, never wraps).
 * - Second row: document type tag + date in muted text.
 * - Third row: party name (16sp, max 2 lines, ellipsis), then small muted line (B2B/B2C + Balance or Valid till).
 * - Optional tappable link badge inside the card ("Converted to SO/..." or "Linked: EST/...").
 * - Bottom row: DocumentActionsRow with StatusChips on the left and Share PDF + primary CTA + 3-dot menu on the right.
 */
@Composable
fun DocumentCard(
    docNumber: String,
    docTypeLabel: String,
    dateText: String,
    partyName: String,
    subtitleLine: String?,
    amount: Money,
    statuses: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPositiveAmount: Boolean = true,
    masked: Boolean = false,
    decimals: Int = 2,
    linkedBadgeText: String? = null,
    onLinkedBadgeClick: (() -> Unit)? = null,
    primaryActionLabel: String? = null,
    onPrimaryActionClick: (() -> Unit)? = null,
    onSharePdfClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDuplicateClick: (() -> Unit)? = null,
    onPrintClick: (() -> Unit)? = null,
    onDownloadPdfClick: (() -> Unit)? = null,
    convertMenuLabel: String? = null,
    onConvertMenuClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    primaryActionTestTag: String = "convert_btn_$docNumber",
    moreMenuTestTag: String = "invoice_more_btn_$docNumber"
) {
    val glass = LocalVanijyaGlass.current
    val isCancelled = statuses.any { it.equals("Cancelled", ignoreCase = true) }
    val amountColor = when {
        isCancelled -> MaterialTheme.colorScheme.onSurfaceVariant
        isPositiveAmount -> EmeraldPrimary
        else -> CoralError
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick
    ) {
        // 1. Top Row: Doc number (left, bold, 1 line, ellipsis) | Amount (right, bold, never wraps)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = docNumber,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            )
            AmountText(
                amount = amount,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = amountColor,
                masked = masked,
                decimals = decimals
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Second Row: Document type tag + date, muted
        Text(
            text = "$docTypeLabel  |  $dateText",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Third Row: Party name (16sp, max 2 lines, ellipsis) + contextual muted line
        Text(
            text = partyName.ifBlank { "Walk-in Customer" },
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (!subtitleLine.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitleLine,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 4. Optional Link Chip inside the card (tappable to open linked document)
        if (!linkedBadgeText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            val linkColor = if (glass.isDark) Color(0xFFD8B4FE) else Color(0xFF5B21B6)
            Surface(
                color = VioletPrimary.copy(alpha = if (glass.isDark) 0.16f else 0.10f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = 1.dp,
                        color = VioletPrimary.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .then(
                        if (onLinkedBadgeClick != null) {
                            Modifier.clickable(onClick = onLinkedBadgeClick)
                        } else {
                            Modifier
                        }
                    )
                    .testTag("doc_link_chip_$docNumber")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = linkColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = linkedBadgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = linkColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = glass.subtleDivider)
        Spacer(modifier = Modifier.height(10.dp))

        // 5. Bottom Row: Status chips left (FlowRow) + Actions right (Share PDF icon, primary CTA, 3-dot menu)
        DocumentActionsRow(
            statuses = statuses,
            docNumber = docNumber,
            primaryActionLabel = primaryActionLabel,
            onPrimaryActionClick = onPrimaryActionClick,
            onSharePdfClick = onSharePdfClick,
            onEditClick = onEditClick,
            onDuplicateClick = onDuplicateClick,
            onPrintClick = onPrintClick,
            onDownloadPdfClick = onDownloadPdfClick,
            convertMenuLabel = convertMenuLabel,
            onConvertMenuClick = onConvertMenuClick,
            onCancelClick = onCancelClick,
            onDeleteClick = onDeleteClick,
            primaryActionTestTag = primaryActionTestTag,
            moreMenuTestTag = moreMenuTestTag
        )
    }
}

/**
 * Reusable InvoiceRow glass component for sales/purchases/recent transactions.
 * Delegates to the clean 24dp glass card structure so StatusChip is never squeezed into a narrow box.
 */
@Composable
fun InvoiceRow(
    invoiceNumber: String,
    partyName: String,
    dateText: String,
    amount: Money,
    statusLabel: String,
    isSale: Boolean = true,
    masked: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSharePdf: (() -> Unit)? = null
) {
    val cleanDocNo = invoiceNumber.substringBefore(" (").trim().ifBlank { invoiceNumber }
    val extractedType = invoiceNumber.substringAfter("(", "").substringBefore(")").ifBlank {
        if (isSale) "Tax Invoice" else "Purchase Bill"
    }
    DocumentCard(
        docNumber = cleanDocNo,
        docTypeLabel = extractedType,
        dateText = dateText,
        partyName = partyName,
        subtitleLine = null,
        amount = amount,
        statuses = listOf(statusLabel),
        onClick = onClick,
        modifier = modifier,
        isPositiveAmount = isSale,
        masked = masked,
        onSharePdfClick = onSharePdf
    )
}

/**
 * Reusable SectionHeader component with optional trailing action button.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (actionText != null && onActionClick != null) {
            val hadLeadingPlus = actionText.trimStart().startsWith("+")
            val cleanActionText = sanitizeLeadingPlus(actionText)
            val resolvedIcon = actionIcon ?: if (hadLeadingPlus) Icons.Default.Add else null
            TextButton(
                onClick = onActionClick,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                if (resolvedIcon != null) {
                    Icon(
                        imageVector = resolvedIcon,
                        contentDescription = cleanActionText,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = cleanActionText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = GoldPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionIcon: ImageVector? = Icons.Default.Add,
    onActionClick: (() -> Unit)? = null
) {
    VanijyaEmptyState(
        icon = icon,
        title = title,
        description = description,
        modifier = modifier,
        actionLabel = actionLabel,
        actionIcon = actionIcon,
        onActionClick = onActionClick
    )
}

/**
 * Reusable EmptyState glass card component with icon, title, subtitle, and optional CTA button.
 */
@Composable
fun VanijyaEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionIcon: ImageVector? = Icons.Default.Add,
    onActionClick: (() -> Unit)? = null
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 26.dp,
        contentPadding = PaddingValues(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                VanijyaButton(
                    text = sanitizeLeadingPlus(actionLabel),
                    icon = actionIcon,
                    onClick = onActionClick
                )
            }
        }
    }
}

/**
 * Animated shimmer skeleton loader for cards while local encrypted queries initialize.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    height: Dp = 84.dp,
    cornerRadius: Dp = 24.dp
) {
    val glass = LocalVanijyaGlass.current
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(glass.cardFillElevated.copy(alpha = alpha))
            .border(1.dp, glass.borderGradientStart, RoundedCornerShape(cornerRadius))
    )
}

/**
 * 7-day sales sparkline Canvas chart using DrawScope dimensions.
 */
@Composable
fun SalesSparklineChart(
    dailyTotals: List<Money>,
    dayLabels: List<String>,
    modifier: Modifier = Modifier
) {
    val lineColor = EmeraldPrimary
    val dotColor = GoldPrimary
    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .semantics { contentDescription = "7-Day Sales Sparkline Chart" }
        ) {
            val w = size.width
            val h = size.height
            val maxPaise = (dailyTotals.maxOfOrNull { it.paise } ?: 0L).coerceAtLeast(100_00L)
            val points = dailyTotals.mapIndexed { idx, money ->
                val x = if (dailyTotals.size <= 1) w / 2f else (idx.toFloat() / (dailyTotals.size - 1)) * (w - 24f) + 12f
                val normalized = (money.paise.toFloat() / maxPaise.toFloat()).coerceIn(0f, 1f)
                val y = h - 16f - (normalized * (h - 32f))
                Offset(x, y)
            }

            // Baseline grid
            drawLine(
                color = lineColor.copy(alpha = 0.18f),
                start = Offset(0f, h - 16f),
                end = Offset(w, h - 16f),
                strokeWidth = 2f
            )

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
            }
            points.forEach { pt ->
                drawCircle(color = lineColor, radius = 7f, center = pt)
                drawCircle(color = dotColor, radius = 3.5f, center = pt)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dayLabels.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * In-screen sub-page container for 1-level and 2-level deep screens (Invoice Detail, Party Detail,
 * Item Detail, Staff Detail, Report Detail). Renders within the NavHost content area (not a dialog window)
 * so the Floating Glass Bottom Bar remains visible and clickable, and registers a BackHandler to go up one screen.
 */
@Composable
fun GlassSubScreen(
    onDismissRequest: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    screenTestTag: String = "glass_sub_screen",
    backButtonTestTag: String = "glass_sub_screen_back_btn",
    content: @Composable ColumnScope.() -> Unit
) {
    val glass = LocalVanijyaGlass.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    val handleBack = onBack ?: onDismissRequest
    BackHandler(onBack = handleBack)

    CompositionLocalProvider(LocalContentColor provides onSurface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            glass.backgroundGradientStart,
                            glass.backgroundGradientMid,
                            glass.backgroundGradientEnd
                        )
                    )
                )
                .testTag(screenTestTag)
        ) {
            Surface(
                color = glass.cardFillElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = handleBack,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(glass.pillFill)
                            .border(1.dp, glass.borderGradientStart, CircleShape)
                            .testTag(backButtonTestTag)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = glass.subtleDivider)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

/**
 * Full-screen editor container for Item Form, Party Form, and Payroll Run.
 * Hides the floating bottom bar while open (via parent callback) and registers BackHandler.
 */
@Composable
fun GlassFullScreenEditor(
    onDismissRequest: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    screenTestTag: String,
    backButtonTestTag: String,
    onTopSaveClick: (() -> Unit)? = null,
    topSaveButtonTestTag: String = "editor_topbar_save_btn",
    content: @Composable ColumnScope.() -> Unit
) {
    val glass = LocalVanijyaGlass.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    val handleBack = onBack ?: onDismissRequest
    BackHandler(onBack = handleBack)

    CompositionLocalProvider(LocalContentColor provides onSurface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            glass.backgroundGradientStart,
                            glass.backgroundGradientMid,
                            glass.backgroundGradientEnd
                        )
                    )
                )
                .imePadding()
                .navigationBarsPadding()
                .testTag(screenTestTag)
        ) {
            Surface(
                color = glass.cardFillElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = handleBack,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(glass.pillFill)
                                .border(1.dp, glass.borderGradientStart, CircleShape)
                                .testTag(backButtonTestTag)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!subtitle.isNullOrBlank()) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (onTopSaveClick != null) {
                        IconButton(
                            onClick = onTopSaveClick,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag(topSaveButtonTestTag)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                                tint = EmeraldPrimary
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = glass.subtleDivider)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

