package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberOrange
import com.example.ui.theme.CyberRedAccent
import com.example.ui.theme.CyberRedDim
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWhite

enum class MayaPersona(val title: String, val desc: String, val icon: ImageVector) {
    CYBERPUNK("Cyberpunk Sentinel", "Futuristic HUD assistant with sci-fi edge", Icons.Default.Psychology),
    TECH_EXPERT("Tech Specialist", "Advanced code, science & deep logic", Icons.Default.Code),
    CREATIVE("Creative Mind", "Imaginative storytelling & poetry", Icons.Default.AutoAwesome),
    FAST_QA("Rapid Q&A", "High-speed concise direct answers", Icons.Default.Speed)
}

@Composable
fun MayaMenuDialog(
    currentPersona: MayaPersona,
    onSelectPersona: (MayaPersona) -> Unit,
    isTtsEnabled: Boolean,
    onToggleTts: (Boolean) -> Unit,
    onClearChat: () -> Unit,
    onRunDiagnostic: () -> Unit,
    onOpenSmartAppControl: () -> Unit,
    onOpenCyberVision: () -> Unit,
    onOpenOptimization: () -> Unit,
    onOpenScreenReader: () -> Unit,
    onOpenAutomation: () -> Unit,
    onOpenAppLock: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("maya_menu_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CyberBlack),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberOrange.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(CyberOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CyberBlack,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MAYA HUD CONTROL",
                                color = CyberOrange,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "System Settings & Neural Modules",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CyberWhite.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Core System Features
                Text(
                    text = "CORE SYSTEM CAPABILITIES",
                    color = CyberWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FeatureMenuTile(
                        icon = Icons.Default.Apps,
                        title = "Smart App Control",
                        desc = "Launch apps, query device packages & system toggles",
                        color = CyberCyan,
                        onClick = { onOpenSmartAppControl(); onDismiss() }
                    )
                    FeatureMenuTile(
                        icon = Icons.Default.CameraAlt,
                        title = "Cyber-Vision Optical Sensor",
                        desc = "Real-time CameraX AI scene scanner & recognition",
                        color = CyberRedAccent,
                        onClick = { onOpenCyberVision(); onDismiss() }
                    )
                    FeatureMenuTile(
                        icon = Icons.Default.Speed,
                        title = "Android System Optimizer",
                        desc = "Live RAM telemetry, memory booster & deep purge",
                        color = CyberOrange,
                        onClick = { onOpenOptimization(); onDismiss() }
                    )
                    FeatureMenuTile(
                        icon = Icons.Default.ScreenSearchDesktop,
                        title = "Screen & Clipboard Reader",
                        desc = "AI text summarizer, translator & vocal explainer",
                        color = CyberGreen,
                        onClick = { onOpenScreenReader(); onDismiss() }
                    )
                    FeatureMenuTile(
                        icon = Icons.Default.AutoAwesome,
                        title = "AI Automation Hub",
                        desc = "Morning Cyber Briefing, battery guardian & auto-clean",
                        color = CyberCyan,
                        onClick = { onOpenAutomation(); onDismiss() }
                    )
                    FeatureMenuTile(
                        icon = Icons.Default.Shield,
                        title = "App Lock Security Shield",
                        desc = "Configure 4-digit PIN security lock protocol",
                        color = CyberRedAccent,
                        onClick = { onOpenAppLock(); onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: AI Persona Modes
                Text(
                    text = "NEURAL PERSONALITY MODE",
                    color = CyberWhite,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                MayaPersona.values().forEach { persona ->
                    val isSelected = persona == currentPersona
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CyberOrange.copy(alpha = 0.15f) else Color(0xFF141418))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CyberOrange else CyberRedDim,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectPersona(persona) }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = persona.icon,
                                contentDescription = null,
                                tint = if (isSelected) CyberOrange else CyberTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = persona.title,
                                    color = if (isSelected) CyberOrange else CyberWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = persona.desc,
                                    color = CyberTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(CyberOrange, CircleShape)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Audio & Vocal Synthesis Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF141418))
                        .border(1.dp, CyberRedDim, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Speech Voice Synthesis",
                                color = CyberWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isTtsEnabled) "Vocal responses enabled" else "Muted (Text only)",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isTtsEnabled,
                        onCheckedChange = onToggleTts,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberOrange,
                            checkedTrackColor = CyberOrange.copy(alpha = 0.3f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF222228)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 4: Quick Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onRunDiagnostic()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("run_diagnostic_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Diagnostic", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            onClearChat()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("clear_chat_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberRedAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberRedAccent.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Logs", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureMenuTile(
    icon: ImageVector,
    title: String,
    desc: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x3318181F))
            .border(1.dp, Color(0x33475569), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(color.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, color = CyberWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, color = CyberTextSecondary, fontSize = 10.sp)
        }
    }
}
