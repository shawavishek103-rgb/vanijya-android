package com.example.feature.security

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.core.designsystem.GlassBackgroundBox
import com.example.core.designsystem.GlassCard
import com.example.core.designsystem.VanijyaButton
import com.example.core.designsystem.VanijyaVectorLogoMark
import com.example.core.security.BiometricAuthHelper
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LocalVanijyaGlass

@Composable
fun LockScreen(
    companyName: String,
    activeUserName: String,
    biometricEnabled: Boolean,
    pinErrorMessage: String?,
    onPinSubmitted: (String) -> Unit,
    onBiometricUnlocked: () -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    var enteredPin by rememberSaveable { mutableStateOf("") }
    var biometricHint by remember { mutableStateOf<String?>(null) }

    // Automatically trigger BiometricPrompt on launch when enabled and running inside FragmentActivity
    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled) {
            val activity = context as? FragmentActivity
            if (activity != null) {
                BiometricAuthHelper.promptBiometric(
                    activity = activity,
                    title = "Unlock $companyName",
                    subtitle = "Authenticate with Fingerprint or Device PIN",
                    onSuccess = onBiometricUnlocked,
                    onUsePinFallback = { msg -> biometricHint = msg }
                )
            }
        }
    }

    val appendDigit: (String) -> Unit = { digit ->
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onClearError()
        if (enteredPin.length < 6) {
            val next = enteredPin + digit
            enteredPin = next
            if (next.length == 6) {
                onPinSubmitted(next)
                enteredPin = ""
            }
        }
    }

    GlassBackgroundBox(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VanijyaVectorLogoMark(size = 64.dp)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = companyName.ifBlank { stringResource(R.string.app_name) },
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${stringResource(R.string.lock_subtitle)} ($activeUserName)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 6-dot PIN Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(6) { index ->
                        val filled = index < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (filled) EmeraldPrimary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (filled) EmeraldPrimary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                if (!pinErrorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = pinErrorMessage,
                        style = MaterialTheme.typography.labelMedium,
                        color = CoralError,
                        textAlign = TextAlign.Center
                    )
                } else if (!biometricHint.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = biometricHint!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tip: Enter your Owner PIN (default 123456) or role PIN (Manager 222222, Sales 333333, Accountant 444444, Viewer 555555)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tactile 6-Digit Keypad
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    rows.forEach { rowDigits ->
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            rowDigits.forEach { digit ->
                                PinKeyButton(
                                    label = digit,
                                    onClick = { appendDigit(digit) },
                                    testTag = "pin_key_$digit"
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        // Biometric button on bottom-left
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                .clickable {
                                    val activity = context as? FragmentActivity
                                    if (activity != null) {
                                        BiometricAuthHelper.promptBiometric(
                                            activity = activity,
                                            title = "Unlock $companyName",
                                            subtitle = "Verify fingerprint or device credential",
                                            onSuccess = onBiometricUnlocked,
                                            onUsePinFallback = { msg -> biometricHint = msg }
                                        )
                                    } else {
                                        biometricHint = "Use 6-digit PIN on this device"
                                    }
                                }
                                .testTag("pin_key_biometric"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = stringResource(R.string.unlock_biometric),
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        PinKeyButton(
                            label = "0",
                            onClick = { appendDigit("0") },
                            testTag = "pin_key_0"
                        )

                        // Backspace button on bottom-right
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    if (enteredPin.isNotEmpty()) {
                                        enteredPin = enteredPin.dropLast(1)
                                    }
                                }
                                .testTag("pin_key_backspace"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "SQLCipher Encrypted",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.badge_offline_encrypted),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PinKeyButton(
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    val glass = LocalVanijyaGlass.current
    Box(
        modifier = Modifier
            .size(72.dp)
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(glass.cardFillElevated)
            .border(
                width = 1.dp,
                color = glass.borderGradientStart,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
