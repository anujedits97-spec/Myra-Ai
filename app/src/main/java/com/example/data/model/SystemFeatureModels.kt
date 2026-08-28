package com.example.data.model

data class AppInfo(
    val label: String,
    val packageName: String,
    val isSystemApp: Boolean = false
)

enum class SettingActionType {
    WIFI,
    BLUETOOTH,
    SOUND,
    DISPLAY,
    BATTERY,
    DATE_TIME,
    SETTINGS
}

data class SystemSettingItem(
    val title: String,
    val subtitle: String,
    val actionType: SettingActionType
)

data class DeviceTelemetry(
    val ramUsedMb: Long,
    val ramTotalMb: Long,
    val ramUsagePercent: Int,
    val storageUsedGb: Double,
    val storageTotalGb: Double,
    val storagePercent: Int,
    val batteryPercent: Int,
    val isCharging: Boolean,
    val thermalStatus: String,
    val cpuFrequencyGhz: Double,
    val isOptimized: Boolean = false
)

enum class AutomationType {
    MORNING_BRIEFING,
    NIGHT_PROTOCOL,
    BATTERY_SAVER_BOOST,
    SMART_RAM_AUTOCLEAN,
    VOICE_ALARM_SYNC
}

data class AutomationWorkflow(
    val id: String,
    val type: AutomationType,
    val title: String,
    val description: String,
    val isEnabled: Boolean,
    val intervalOrTrigger: String,
    val lastRunText: String = "Ready"
)

data class SecurityLockState(
    val isLockEnabled: Boolean = false,
    val pinCode: String = "",
    val isLocked: Boolean = false,
    val autoLockOnBackground: Boolean = true
)
