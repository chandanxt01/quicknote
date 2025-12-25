package com.ck.quicknote.core.common

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class BiometricPromptManager(
    private val activity: AppCompatActivity
) {
    fun promptBiometricAuth(
        title: String,
        subTitle: String,
        negativeButtonText: String,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )

        // API 30+ (Android 11) par hum Device Credential (PIN/Pattern) bhi allowed kar sakte hain
        // agar Biometric setup nahi hai.
        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subTitle)

        // Allowed authenticators set karna
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val allowedAuthenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            promptInfoBuilder.setAllowedAuthenticators(allowedAuthenticators)
        } else {
            // Android 10 aur usse niche ke liye Negative Button zaroori hai
            promptInfoBuilder.setNegativeButtonText(negativeButtonText)
        }

        biometricPrompt.authenticate(promptInfoBuilder.build())
    }
}