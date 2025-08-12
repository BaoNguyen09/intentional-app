package com.example.intentional_app

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel // for flutter-native communication

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent // for opening settings
import android.provider.Settings // for checking settings permission
import android.net.Uri // for navigating settings
import android.os.Build
import android.view.accessibility.AccessibilityManager

class MainActivity : FlutterActivity() {

    private val CHANNEL = "com.example.intentional_app/app_manager" // Unique channel name

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { 
            call, result ->
            when (call.method) {
                "isAppMonitoringServiceEnabled" -> {
                    val isEnabled = isAccessibilityServiceEnabled("com.example.intentional_app.AppMonitoringService")
                    result.success(isEnabled)
                }
                "openAccessibilitySettings" -> {
                    openAccessibilitySettings()
                    result.success(null) // No direct return value needed
                }
                "checkOverlayPermission" -> {
                    val hasPermission = checkOverlayPermission()
                    result.success(hasPermission)
                }
                "openOverlaySettings" -> {
                    openOverlaySettings()
                    result.success(null) // No direct return value needed
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun isAccessibilityServiceEnabled(serviceClassName: String): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val targetServiceComponent = ComponentName(packageName, serviceClassName).flattenToString()


        for (service in enabledServices) {
            if (service.id == targetServiceComponent) {
                return true
            }
        }

        return false
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            // Permission is implicitly granted on older Android versions
            true 
        }
    }

    private fun openOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

}

