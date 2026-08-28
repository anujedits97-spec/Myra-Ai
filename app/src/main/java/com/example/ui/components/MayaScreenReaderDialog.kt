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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberOrange
import com.example.ui.theme.CyberRedAccent
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWhite

@Composable
fun MayaScreenReaderDialog(
    initialText: String?,
    onProcessScreenText: (text: String, actionPrompt: String) -> Unit,
    onDismiss: () -> Unit
) {
    var screenText by remember { mutableStateOf(initialText ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CyberSurface)
                .border(1.5.dp, CyberRedBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0x33EF4444), CircleShape)
                                .border(1.dp, CyberRedAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenSearchDesktop,
                                contentDescription = null,
                                tint = CyberRedAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SCREEN & CLIPBOARD READER",
                                color = CyberWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "AI TEXT PERCEPTION & SUMMARY",
                                color = CyberTextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = CyberTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Editable Screen/Clipboard Text Area
                OutlinedTextField(
                    value = screenText,
                    onValueChange = { screenText = it },
                    placeholder = {
                        Text(
                            "Paste copied text from any screen/app or let Maya read your clipboard...",
                            color = CyberTextMuted,
                            fontSize = 11.5.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberRedAccent,
                        unfocusedBorderColor = Color(0x33475569),
                        focusedContainerColor = Color(0x2218181F),
                        unfocusedContainerColor = Color(0x2218181F),
                        focusedTextColor = CyberWhite,
                        unfocusedTextColor = CyberWhite
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SELECT AI ACTION PROTOCOL",
                    color = CyberOrange,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Action Protocol Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ScreenActionRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Summarize & Extract Key Points",
                        subtitle = "Fast executive summary",
                        color = CyberRedAccent,
                        onClick = {
                            if (screenText.isNotBlank()) {
                                onProcessScreenText(screenText, "Please summarize the following screen text concisely:")
                                onDismiss()
                            }
                        }
                    )
                    ScreenActionRow(
                        icon = Icons.Default.RecordVoiceOver,
                        title = "Explain & Speak Aloud",
                        subtitle = "Natural AI voice reading",
                        color = CyberOrange,
                        onClick = {
                            if (screenText.isNotBlank()) {
                                onProcessScreenText(screenText, "Please explain this content in simple terms and speak it aloud:")
                                onDismiss()
                            }
                        }
                    )
                    ScreenActionRow(
                        icon = Icons.Default.Translate,
                        title = "Translate / Convert",
                        subtitle = "Translate into clear Urdu/English",
                        color = CyberCyan,
                        onClick = {
                            if (screenText.isNotBlank()) {
                                onProcessScreenText(screenText, "Translate and explain this text clearly in both Urdu and English:")
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x3318181F))
            .border(1.dp, Color(0x33475569), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, color = CyberWhite, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = CyberTextMuted, fontSize = 9.5.sp)
        }
    }
}
