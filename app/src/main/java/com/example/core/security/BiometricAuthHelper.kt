package com.example.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Wraps AndroidX BiometricPrompt for app launch unlock and sensitive action re-authentication,
 * with automatic fallback to device credential or Vanijya's 6-digit PIN when running on an
 * emulator or device without enrolled biometric hardware.
 */
object BiometricAuthHelper {

    enum class BiometricStatus {
        AVAILABLE,
        NO_HARDWARE,
        NONE_ENROLLED,
        UNAVAILABLE
    }

    fun checkBiometricStatus(context: Context): BiometricStatus {
        val manager = BiometricManager.from(context)
        val result = manager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        return when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            else -> BiometricStatus.UNAVAILABLE
        }
    }

    fun promptBiometric(
        activity: FragmentActivity,
        title: String = "Unlock Vanijya Vault",
        subtitle: String = "Verify identity using fingerprint or device lock",
        onSuccess: () -> Unit,
        onUsePinFallback: (String?) -> Unit
    ) {
        val status = checkBiometricStatus(activity)
        if (status != BiometricStatus.AVAILABLE) {
            onUsePinFallback("Biometric hardware not enrolled on this device/emulator. Use 6-digit PIN.")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onUsePinFallback(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()

        try {
            prompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onUsePinFallback(e.localizedMessage ?: "Use 6-digit PIN")
        }
    }
}
