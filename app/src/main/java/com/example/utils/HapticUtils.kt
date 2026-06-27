package com.example.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticUtils {
    private const val PREFS_NAME = "app_settings"
    private const val PREF_VIBRATION_ENABLED = "vibration_enabled"

    fun isVibrationEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(PREF_VIBRATION_ENABLED, true)
    }

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(PREF_VIBRATION_ENABLED, enabled).apply()
    }

    /**
     * Vibrate for a standard duration (in milliseconds).
     */
    fun vibrate(context: Context, milliseconds: Long = 50) {
        if (!isVibrationEnabled(context)) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            milliseconds,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(milliseconds)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Vibrates with a custom waveform pattern (e.g., [off, on, off, on] in milliseconds).
     */
    fun vibratePattern(context: Context, pattern: LongArray, repeat: Int = -1) {
        if (!isVibrationEnabled(context)) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, repeat)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
