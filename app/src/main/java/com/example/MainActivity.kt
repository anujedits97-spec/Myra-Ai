package com.example

import android.Manifest
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.MayaViewModel
import com.example.ui.components.MayaAppLockScreen
import com.example.ui.components.MayaAppLockSettingsDialog
import com.example.ui.components.MayaAutomationDialog
import com.example.ui.components.MayaChatSection
import com.example.ui.components.MayaCyberVisionDialog
import com.example.ui.components.MayaMenuDialog
import com.example.ui.components.MayaOptimizationDialog
import com.example.ui.components.MayaPipCircleView
import com.example.ui.components.MayaScreenReaderDialog
import com.example.ui.components.MayaSmartAppControlDialog
import com.example.ui.components.RedParticleHudCore
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberOrange
import com.example.ui.theme.CyberOrangeGlow
import com.example.ui.theme.CyberRedAccent
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberWhite
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val viewModel: MayaViewModel by viewModels()
    private val _isInPipMode = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isInPip by _isInPipMode.collectAsState()
            MyApplicationTheme {
                MayaAppScreen(
                    viewModel = viewModel,
                    isInPipMode = isInPip,
                    onEnterPip = { enterPipMode() }
                )
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        _isInPipMode.value = isInPictureInPictureMode
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPipMode()
    }

    fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val aspectRatio = Rational(1, 1)
                val builder = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    builder.setAutoEnterEnabled(true)
                }
                enterPictureInPictureMode(builder.build())
            } catch (e: Exception) {
                // Ignore if PiP unsupported
            }
        }
    }
}

@Composable
fun MayaAppScreen(
    viewModel: MayaViewModel,
    isInPipMode: Boolean = false,
    onEnterPip: () -> Unit = {}
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val isTtsEnabled by viewModel.isTtsEnabled.collectAsState()
    val currentPersona by viewModel.currentPersona.collectAsState()
    val isMenuOpen by viewModel.isMenuOpen.collectAsState()

    // 7 Features States
    val installedApps by viewModel.installedApps.collectAsState()
    val systemSettings by viewModel.systemSettings.collectAsState()
    val isSmartAppControlOpen by viewModel.isSmartAppControlOpen.collectAsState()

    val isCyberVisionOpen by viewModel.isCyberVisionOpen.collectAsState()
    val visionAnalysisResult by viewModel.visionAnalysisResult.collectAsState()
    val isAnalyzingVision by viewModel.isAnalyzingVision.collectAsState()

    val deviceTelemetry by viewModel.deviceTelemetry.collectAsState()
    val isOptimizationOpen by viewModel.isOptimizationOpen.collectAsState()
    val isOptimizing by viewModel.isOptimizing.collectAsState()

    val isScreenReaderOpen by viewModel.isScreenReaderOpen.collectAsState()
    val screenReaderInitialText by viewModel.screenReaderInitialText.collectAsState()

    val securityLockState by viewModel.securityLockState.collectAsState()
    val isLockSettingsOpen by viewModel.isLockSettingsOpen.collectAsState()

    val automationWorkflows by viewModel.automationWorkflows.collectAsState()
    val isAutomationOpen by viewModel.isAutomationOpen.collectAsState()

    // Handle voice or UI trigger to enter PiP
    LaunchedEffect(Unit) {
        viewModel.enterPipEvent.collect {
            onEnterPip()
        }
    }

    // Permission launcher for audio recording
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleListening()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is required for voice recognition.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.openCyberVision()
        } else {
            Toast.makeText(
                context,
                "Camera permission is required for Cyber-Vision optical sensing.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val onMicClick: () -> Unit = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.toggleListening()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val onCameraLaunchClick: () -> Unit = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.openCyberVision()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // 1. If currently in App Lock mode, show Security Shield Pin screen
    if (securityLockState.isLocked && !isInPipMode) {
        MayaAppLockScreen(
            lockState = securityLockState,
            onVerifyPin = viewModel::verifyPin,
            onUnlockSuccess = viewModel::unlockApp
        )
        return
    }

    // 2. If currently in PiP mode, show high particle circle view (Gemini-style)
    if (isInPipMode) {
        MayaPipCircleView(
            isListening = isListening,
            isSpeaking = isSpeaking,
            isProcessing = isProcessing,
            audioRms = audioRms,
            onClickCore = onMicClick
        )
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("maya_main_scaffold"),
        containerColor = CyberBlack
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberBlack)
                .drawBehind {
                    val centerGradient = Offset(size.width * 0.5f, size.height * 0.3f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CyberRedAccent.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = centerGradient,
                            radius = size.width * 0.85f
                        ),
                        center = centerGradient,
                        radius = size.width * 0.85f
                    )
                }
        ) {
            val totalHeight = maxHeight
            val hudHeight = (totalHeight * 0.36f).coerceIn(210.dp, 290.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .align(Alignment.Center)
            ) {
                // Top Header Section: MAYA MOBILE AI + PiP Button
                MayaHeader(
                    personaName = currentPersona.title,
                    onEnterPip = onEnterPip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )

                // Center Red Particle / Core HUD Animation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(hudHeight),
                    contentAlignment = Alignment.Center
                ) {
                    RedParticleHudCore(
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        isProcessing = isProcessing,
                        audioRms = audioRms,
                        onClickCore = onMicClick
                    )
                }

                // Chat Interface Section
                MayaChatSection(
                    messages = messages,
                    inputText = inputText,
                    onInputChange = viewModel::onInputTextChanged,
                    onSendMessage = viewModel::sendMessage,
                    isListening = isListening,
                    onToggleMic = onMicClick,
                    onSpeakMessage = viewModel::speakText,
                    onPromptChipClicked = viewModel::sendPromptSuggestion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Orange Floating Menu Button
            val menuTopOffset = (hudHeight + 36.dp).coerceAtMost(totalHeight * 0.42f)
            Box(
                modifier = Modifier
                    .offset(x = 16.dp, y = menuTopOffset)
                    .size(48.dp)
                    .shadow(
                        elevation = 15.dp,
                        shape = CircleShape,
                        ambientColor = CyberOrangeGlow,
                        spotColor = CyberOrangeGlow
                    )
                    .clip(CircleShape)
                    .background(CyberOrange)
                    .clickable { viewModel.toggleMenu() }
                    .testTag("floating_menu_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Maya AI Menu",
                    tint = CyberWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    // 1. Menu / Control Center Dialog
    if (isMenuOpen) {
        MayaMenuDialog(
            currentPersona = currentPersona,
            onSelectPersona = viewModel::setPersona,
            isTtsEnabled = isTtsEnabled,
            onToggleTts = viewModel::setTtsEnabled,
            onClearChat = viewModel::clearMessages,
            onRunDiagnostic = viewModel::runDiagnostic,
            onOpenSmartAppControl = viewModel::openSmartAppControl,
            onOpenCyberVision = onCameraLaunchClick,
            onOpenOptimization = viewModel::openOptimization,
            onOpenScreenReader = viewModel::openScreenReader,
            onOpenAutomation = viewModel::openAutomation,
            onOpenAppLock = viewModel::openLockSettings,
            onDismiss = viewModel::dismissMenu
        )
    }

    // 2. Smart App Control Dialog
    if (isSmartAppControlOpen) {
        MayaSmartAppControlDialog(
            installedApps = installedApps,
            systemSettings = systemSettings,
            onLaunchApp = viewModel::launchApp,
            onOpenSetting = viewModel::openSetting,
            onDismiss = viewModel::dismissSmartAppControl
        )
    }

    // 3. Cyber-Vision Camera Dialog
    if (isCyberVisionOpen) {
        MayaCyberVisionDialog(
            onDismiss = viewModel::dismissCyberVision,
            onAnalyzeImage = viewModel::analyzeOpticalScene,
            analysisResult = visionAnalysisResult,
            isAnalyzing = isAnalyzingVision
        )
    }

    // 4. Android Optimization Dialog
    if (isOptimizationOpen) {
        MayaOptimizationDialog(
            telemetry = deviceTelemetry,
            isOptimizing = isOptimizing,
            onPerformBoost = viewModel::performTurboBoost,
            onDismiss = viewModel::dismissOptimization
        )
    }

    // 5. Screen & Clipboard Reader Dialog
    if (isScreenReaderOpen) {
        MayaScreenReaderDialog(
            initialText = screenReaderInitialText,
            onProcessScreenText = viewModel::processScreenText,
            onDismiss = viewModel::dismissScreenReader
        )
    }

    // 6. AI Automation Hub Dialog
    if (isAutomationOpen) {
        MayaAutomationDialog(
            workflows = automationWorkflows,
            onToggleWorkflow = viewModel::toggleAutomation,
            onRunMorningBriefing = viewModel::runMorningBriefing,
            onDismiss = viewModel::dismissAutomation
        )
    }

    // 7. App Lock Settings Dialog
    if (isLockSettingsOpen) {
        MayaAppLockSettingsDialog(
            lockState = securityLockState,
            onDismiss = viewModel::dismissLockSettings,
            onSaveLockSettings = viewModel::setAppLock
        )
    }
}

@Composable
fun MayaHeader(
    personaName: String,
    onEnterPip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag("maya_header"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = "MAYA MOBILE AI",
                    color = CyberRedAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(1.dp)
                        .background(CyberRedBorder)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(CyberRedAccent, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NEURAL LINK • $personaName",
                    color = CyberTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        // PiP Mode Trigger Icon Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0x3318181F))
                .clickable { onEnterPip() }
                .testTag("pip_mode_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PictureInPictureAlt,
                contentDescription = "Enter Picture-in-Picture Floating Circle Mode",
                tint = CyberOrange,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
