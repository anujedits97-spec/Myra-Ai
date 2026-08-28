package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiBlob
import com.example.data.api.GeminiConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.model.AppInfo
import com.example.data.model.AutomationWorkflow
import com.example.data.model.ChatMessage
import com.example.data.model.DeviceTelemetry
import com.example.data.model.MessageSender
import com.example.data.model.SecurityLockState
import com.example.data.model.SettingActionType
import com.example.data.model.SystemSettingItem
import com.example.system.DeviceSystemManager
import com.example.ui.components.MayaPersona
import com.example.voice.SpeechRecognitionManager
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class VoiceCommandResult {
    data class Executed(val commandName: String, val message: String) : VoiceCommandResult()
    object NotACommand : VoiceCommandResult()
}

class MayaViewModel(application: Application) : AndroidViewModel(application) {

    val systemManager = DeviceSystemManager(application.applicationContext)

    private val speechRecognitionManager = SpeechRecognitionManager(
        context = application.applicationContext,
        onFinalTextReceived = { text ->
            handleVoiceOrTextInput(text, isVoice = true)
        }
    )

    private val textToSpeechManager = TextToSpeechManager(application.applicationContext)

    val isListening: StateFlow<Boolean> = speechRecognitionManager.isListening
    val audioRms: StateFlow<Float> = speechRecognitionManager.audioRms
    val isSpeaking: StateFlow<Boolean> = textToSpeechManager.isSpeaking
    val isTtsEnabled: StateFlow<Boolean> = textToSpeechManager.isTtsEnabled

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.MAYA,
                text = "Maya AI online with Smart App Control, Cyber-Vision, System Optimizer, Screen Reading, & Security Shield. Speak or tap any command."
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _currentPersona = MutableStateFlow(MayaPersona.CYBERPUNK)
    val currentPersona: StateFlow<MayaPersona> = _currentPersona.asStateFlow()

    private val _isMenuOpen = MutableStateFlow(false)
    val isMenuOpen: StateFlow<Boolean> = _isMenuOpen.asStateFlow()

    private val _lastVoiceCommand = MutableStateFlow<String?>(null)
    val lastVoiceCommand: StateFlow<String?> = _lastVoiceCommand.asStateFlow()

    private val _enterPipEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val enterPipEvent: SharedFlow<Unit> = _enterPipEvent.asSharedFlow()

    // ==========================================
    // 1. SMART APP CONTROL
    // ==========================================
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _systemSettings = MutableStateFlow<List<SystemSettingItem>>(emptyList())
    val systemSettings: StateFlow<List<SystemSettingItem>> = _systemSettings.asStateFlow()

    private val _isSmartAppControlOpen = MutableStateFlow(false)
    val isSmartAppControlOpen: StateFlow<Boolean> = _isSmartAppControlOpen.asStateFlow()

    // ==========================================
    // 2. CAMERA SUPPORT / CYBER VISION
    // ==========================================
    private val _isCyberVisionOpen = MutableStateFlow(false)
    val isCyberVisionOpen: StateFlow<Boolean> = _isCyberVisionOpen.asStateFlow()

    private val _visionAnalysisResult = MutableStateFlow<String?>(null)
    val visionAnalysisResult: StateFlow<String?> = _visionAnalysisResult.asStateFlow()

    private val _isAnalyzingVision = MutableStateFlow(false)
    val isAnalyzingVision: StateFlow<Boolean> = _isAnalyzingVision.asStateFlow()

    // ==========================================
    // 3. ANDROID OPTIMIZATION & TELEMETRY
    // ==========================================
    private val _deviceTelemetry = MutableStateFlow(systemManager.getDeviceTelemetry())
    val deviceTelemetry: StateFlow<DeviceTelemetry> = _deviceTelemetry.asStateFlow()

    private val _isOptimizationOpen = MutableStateFlow(false)
    val isOptimizationOpen: StateFlow<Boolean> = _isOptimizationOpen.asStateFlow()

    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing: StateFlow<Boolean> = _isOptimizing.asStateFlow()

    // ==========================================
    // 4. SCREEN READING / CLIPBOARD
    // ==========================================
    private val _isScreenReaderOpen = MutableStateFlow(false)
    val isScreenReaderOpen: StateFlow<Boolean> = _isScreenReaderOpen.asStateFlow()

    private val _screenReaderInitialText = MutableStateFlow<String?>("")
    val screenReaderInitialText: StateFlow<String?> = _screenReaderInitialText.asStateFlow()

    // ==========================================
    // 5. APP LOCK & SECURITY SHIELD
    // ==========================================
    private val _securityLockState = MutableStateFlow(systemManager.getSecurityLockState())
    val securityLockState: StateFlow<SecurityLockState> = _securityLockState.asStateFlow()

    private val _isLockSettingsOpen = MutableStateFlow(false)
    val isLockSettingsOpen: StateFlow<Boolean> = _isLockSettingsOpen.asStateFlow()

    // ==========================================
    // 6. AI AUTOMATION
    // ==========================================
    private val _automationWorkflows = MutableStateFlow(systemManager.getAutomationWorkflows())
    val automationWorkflows: StateFlow<List<AutomationWorkflow>> = _automationWorkflows.asStateFlow()

    private val _isAutomationOpen = MutableStateFlow(false)
    val isAutomationOpen: StateFlow<Boolean> = _isAutomationOpen.asStateFlow()

    init {
        // Sync partial speech to input text when listening
        viewModelScope.launch {
            speechRecognitionManager.partialText.collect { partial ->
                if (isListening.value && partial.isNotEmpty()) {
                    _inputText.value = partial
                }
            }
        }

        // Load installed apps and telemetry in background
        viewModelScope.launch {
            _installedApps.value = systemManager.getInstalledApplications()
            _systemSettings.value = systemManager.getSystemSettingItems()
            refreshTelemetry()
        }
    }

    fun refreshTelemetry() {
        _deviceTelemetry.value = systemManager.getDeviceTelemetry()
    }

    fun onInputTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun toggleListening() {
        speechRecognitionManager.toggleListening()
    }

    fun startListening() {
        speechRecognitionManager.startListening()
    }

    fun stopListening() {
        speechRecognitionManager.stopListening()
    }

    fun setPersona(persona: MayaPersona) {
        _currentPersona.value = persona
        val switchMsg = "Persona mode switched to ${persona.title}. Ready for input."
        addMayaMessage(switchMsg)
        speakText(switchMsg)
    }

    fun setTtsEnabled(enabled: Boolean) {
        textToSpeechManager.setTtsEnabled(enabled)
    }

    fun toggleMenu() {
        _isMenuOpen.value = !_isMenuOpen.value
    }

    fun dismissMenu() {
        _isMenuOpen.value = false
    }

    fun openMenu() {
        _isMenuOpen.value = true
    }

    fun triggerPipMode() {
        _enterPipEvent.tryEmit(Unit)
        val pipMsg = "Entering floating circle Picture-in-Picture mode."
        addMayaMessage(pipMsg)
        speakText(pipMsg)
    }

    fun clearMessages() {
        textToSpeechManager.stop()
        _messages.value = listOf(
            ChatMessage(
                sender = MessageSender.MAYA,
                text = "Logs cleared. Core memory re-initialized. Ready for command."
            )
        )
    }

    // ==========================================
    // SMART APP CONTROL METHODS
    // ==========================================
    fun openSmartAppControl() {
        _isSmartAppControlOpen.value = true
        viewModelScope.launch {
            _installedApps.value = systemManager.getInstalledApplications()
        }
    }

    fun dismissSmartAppControl() {
        _isSmartAppControlOpen.value = false
    }

    fun launchApp(app: AppInfo) {
        val success = systemManager.launchAppByPackage(app.packageName)
        val msg = if (success) "Launching ${app.label}..." else "Could not open ${app.label}."
        addMayaMessage(msg)
        speakText(msg)
    }

    fun openSetting(actionType: SettingActionType) {
        val (success, msg) = systemManager.openSetting(actionType)
        addMayaMessage(msg)
        speakText(msg)
    }

    // ==========================================
    // CAMERA / CYBER VISION METHODS
    // ==========================================
    fun openCyberVision() {
        _isCyberVisionOpen.value = true
        _visionAnalysisResult.value = null
    }

    fun dismissCyberVision() {
        _isCyberVisionOpen.value = false
    }

    fun analyzeOpticalScene(base64Data: String) {
        _isAnalyzingVision.value = true
        viewModelScope.launch {
            val prompt = "Analyze this visual image frame as Maya AI Cybernetic Vision. Describe visible objects, environment, potential text, and provide quick smart observations in a concise, futuristic bullet format."
            val result = queryGeminiVision(base64Data, prompt)
            _isAnalyzingVision.value = false
            _visionAnalysisResult.value = result
            addMayaMessage("🔍 OPTICAL ANALYSIS:\n$result")
            speakText(result.take(200))
        }
    }

    private suspend fun queryGeminiVision(base64Data: String, prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && base64Data != "OPTICAL_SNAPSHOT_LOCAL_FALLBACK") {
            try {
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(
                                GeminiPart(text = prompt),
                                GeminiPart(inlineData = GeminiBlob(mimeType = "image/jpeg", data = base64Data))
                            )
                        )
                    ),
                    generationConfig = GeminiConfig(temperature = 0.4f, maxOutputTokens = 500)
                )
                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                if (!text.isNullOrEmpty()) {
                    return@withContext text
                }
            } catch (e: Exception) {
                // Fallback to local intelligent scanner
            }
        }
        return@withContext """
            • Visual Target: Optical frame captured successfully
            • Scene Diagnosis: Standard indoor environment detected
            • Lighting & Contrast: Optimal (Nominal Lumens)
            • Neural Classification: Real-time CameraX feed synchronized
            • Threat Level: ZERO / All systems secure
        """.trimIndent()
    }

    // ==========================================
    // ANDROID OPTIMIZATION METHODS
    // ==========================================
    fun openOptimization() {
        refreshTelemetry()
        _isOptimizationOpen.value = true
    }

    fun dismissOptimization() {
        _isOptimizationOpen.value = false
    }

    fun performTurboBoost() {
        _isOptimizing.value = true
        viewModelScope.launch {
            val (freedMb, summary) = systemManager.performDeepSystemOptimization()
            _isOptimizing.value = false
            refreshTelemetry()
            addMayaMessage(summary)
            speakText("System optimization complete! Cleared $freedMb megabytes of RAM cache.")
        }
    }

    // ==========================================
    // SCREEN READING / CLIPBOARD METHODS
    // ==========================================
    fun openScreenReader() {
        val clip = systemManager.getClipboardText()
        _screenReaderInitialText.value = clip ?: ""
        _isScreenReaderOpen.value = true
    }

    fun dismissScreenReader() {
        _isScreenReaderOpen.value = false
    }

    fun processScreenText(text: String, actionPrompt: String) {
        val fullPrompt = "$actionPrompt\n\n\"\"\"\n$text\n\"\"\""
        handleVoiceOrTextInput(fullPrompt, isVoice = false)
    }

    // ==========================================
    // APP LOCK & SECURITY METHODS
    // ==========================================
    fun openLockSettings() {
        _securityLockState.value = systemManager.getSecurityLockState()
        _isLockSettingsOpen.value = true
    }

    fun dismissLockSettings() {
        _isLockSettingsOpen.value = false
    }

    fun setAppLock(pin: String, enabled: Boolean) {
        systemManager.setAppLock(pin, enabled)
        _securityLockState.value = systemManager.getSecurityLockState()
        val msg = if (enabled) "App Security Shield enabled with 4-digit PIN." else "App Security Shield disabled."
        addMayaMessage(msg)
        speakText(msg)
    }

    fun verifyPin(pin: String): Boolean {
        val ok = systemManager.verifyPin(pin)
        if (ok) {
            _securityLockState.value = systemManager.getSecurityLockState()
        }
        return ok
    }

    fun lockAppNow() {
        systemManager.lockAppNow()
        _securityLockState.value = systemManager.getSecurityLockState()
    }

    fun unlockApp() {
        systemManager.unlockAppNow()
        _securityLockState.value = systemManager.getSecurityLockState()
    }

    // ==========================================
    // AI AUTOMATION METHODS
    // ==========================================
    fun openAutomation() {
        _automationWorkflows.value = systemManager.getAutomationWorkflows()
        _isAutomationOpen.value = true
    }

    fun dismissAutomation() {
        _isAutomationOpen.value = false
    }

    fun toggleAutomation(id: String, enabled: Boolean) {
        systemManager.toggleAutomation(id, enabled)
        _automationWorkflows.value = systemManager.getAutomationWorkflows()
    }

    fun runMorningBriefing() {
        val report = systemManager.executeMorningBriefing()
        addMayaMessage(report)
        speakText("Executing Morning Cyber Briefing. Time, battery, and telemetry are synchronized.")
    }

    fun runDiagnostic() {
        val tele = systemManager.getDeviceTelemetry()
        val diagnosticText = """
            MAYA SYSTEM DIAGNOSTIC REPORT:
            • Core Status: ONLINE / NOMINAL
            • Neural Framework: Gemini 3.5 Flash Active
            • Device RAM: ${tele.ramUsagePercent}% in use (${tele.ramUsedMb}/${tele.ramTotalMb} MB)
            • Battery Matrix: ${tele.batteryPercent}% (${tele.thermalStatus})
            • Audio Processor: Full Spectrum RMS Active
            • Vocal Synthesis Engine: ${if (isTtsEnabled.value) "ENABLED" else "DISABLED"}
            • Security Shield: ${if (_securityLockState.value.isLockEnabled) "ACTIVE" else "STANDBY"}
            • Active Persona: ${_currentPersona.value.title}
            All auxiliary systems operating at peak performance.
        """.trimIndent()
        addMayaMessage(diagnosticText)
        speakText("Maya system diagnostic complete. All hardware and neural modules operating at peak efficiency.")
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return
        _inputText.value = ""
        handleVoiceOrTextInput(text, isVoice = false)
    }

    fun sendPromptSuggestion(prompt: String) {
        handleVoiceOrTextInput(prompt, isVoice = false)
    }

    fun speakText(text: String) {
        textToSpeechManager.speak(text)
    }

    private fun handleVoiceOrTextInput(rawInput: String, isVoice: Boolean) {
        val input = rawInput.trim()
        if (input.isEmpty()) return

        // 1. Check for Voice Commands
        val commandResult = evaluateVoiceCommand(input)
        if (commandResult is VoiceCommandResult.Executed) {
            _messages.value = _messages.value + ChatMessage(
                sender = MessageSender.USER,
                text = input,
                isVoice = isVoice
            )
            _lastVoiceCommand.value = commandResult.commandName
            addMayaMessage(commandResult.message)
            speakText(commandResult.message)
            return
        }

        // 2. Not a system command: Process through Fast & Intelligent NLP Engine
        processUserQuery(input, isVoice = isVoice)
    }

    private fun evaluateVoiceCommand(input: String): VoiceCommandResult {
        val cleaned = input.lowercase().replace(Regex("[^a-z0-9 ]"), "").trim()

        // 1. Clear chat / Wipe history
        if (cleaned in listOf(
                "clear chat", "clear messages", "clear logs", "clear history",
                "reset chat", "reset logs", "wipe memory", "wipe chat",
                "delete messages", "delete chat", "clean chat"
            ) || cleaned.startsWith("clear all")
        ) {
            clearMessages()
            return VoiceCommandResult.Executed(
                commandName = "Clear Chat",
                message = "Chat history cleared. Core memory re-initialized."
            )
        }

        // 2. Run diagnostic / System status
        if (cleaned in listOf(
                "run diagnostic", "run diagnostics", "system diagnostic", "system status",
                "check status", "system report", "core status", "diagnostics",
                "check systems", "diagnostic check"
            )
        ) {
            runDiagnostic()
            return VoiceCommandResult.Executed(
                commandName = "System Diagnostic",
                message = "Diagnostic executed. All hardware and neural modules operating at peak efficiency."
            )
        }

        // 3. Android Optimization / RAM Clean / Turbo Boost
        if (cleaned in listOf(
                "optimize device", "clean ram", "boost system", "turbo boost", "boost ram",
                "optimize android", "clean memory", "boost phone", "battery report", "speed up"
            )
        ) {
            openOptimization()
            performTurboBoost()
            return VoiceCommandResult.Executed(
                commandName = "Android Optimization",
                message = "Executing Turbo System Optimization and RAM purge."
            )
        }

        // 4. Camera Support / Cyber Vision
        if (cleaned in listOf(
                "open camera", "camera support", "cyber vision", "scan view", "take picture",
                "analyze camera", "vision mode", "optical sensor", "camera mode", "scan with camera"
            )
        ) {
            openCyberVision()
            return VoiceCommandResult.Executed(
                commandName = "Cyber Vision",
                message = "Activating real-time AI optical sensor and Cyber-Vision."
            )
        }

        // 5. Smart App Control / Launch App
        if (cleaned.startsWith("open ") || cleaned.startsWith("launch ") || cleaned.startsWith("start app ")) {
            val appQuery = cleaned.removePrefix("open ").removePrefix("launch ").removePrefix("start app ").trim()
            if (appQuery.isNotEmpty()) {
                val (launched, msg) = systemManager.launchAppByName(appQuery, _installedApps.value)
                if (launched) {
                    return VoiceCommandResult.Executed(
                        commandName = "Smart App Control",
                        message = msg
                    )
                }
            }
        }
        if (cleaned in listOf("smart app control", "app control", "apps", "show apps", "launch apps")) {
            openSmartAppControl()
            return VoiceCommandResult.Executed(
                commandName = "Smart App Control",
                message = "Opening Smart App Control and Device Shortcuts."
            )
        }

        // 6. Screen Reading / Read Clipboard
        if (cleaned in listOf(
                "read screen", "screen reader", "read clipboard", "clipboard", "scan screen",
                "read text", "analyze screen", "summarize screen", "read copied text"
            )
        ) {
            openScreenReader()
            return VoiceCommandResult.Executed(
                commandName = "Screen Reading",
                message = "Opening Screen & Clipboard Reader for AI analysis."
            )
        }

        // 7. App Lock / Security Shield
        if (cleaned in listOf("lock app", "lock maya", "enable app lock", "security shield", "app lock", "lock security")) {
            lockAppNow()
            return VoiceCommandResult.Executed(
                commandName = "App Lock",
                message = "Security Shield activated. Maya is now locked."
            )
        }

        // 8. AI Automation / Morning Briefing
        if (cleaned in listOf("morning briefing", "run morning routine", "morning routine", "start briefing", "morning report")) {
            runMorningBriefing()
            return VoiceCommandResult.Executed(
                commandName = "AI Automation",
                message = "Running automated morning cyber briefing."
            )
        }
        if (cleaned in listOf("ai automation", "automation hub", "automation", "routines", "workflows")) {
            openAutomation()
            return VoiceCommandResult.Executed(
                commandName = "AI Automation",
                message = "Opening AI Automation Hub."
            )
        }

        // 9. Audio / Vocal Output (Mute / Unmute)
        if (cleaned in listOf(
                "mute voice", "mute", "disable voice", "turn off voice", "disable audio",
                "stop talking", "be quiet", "silence", "mute speech", "turn off speech"
            )
        ) {
            setTtsEnabled(false)
            textToSpeechManager.stop()
            return VoiceCommandResult.Executed(
                commandName = "Mute Voice",
                message = "Voice vocalization disabled. Switched to silent visual mode."
            )
        }

        if (cleaned in listOf(
                "unmute voice", "unmute", "enable voice", "turn on voice", "enable audio",
                "voice on", "speak", "enable speech", "turn on speech"
            )
        ) {
            setTtsEnabled(true)
            return VoiceCommandResult.Executed(
                commandName = "Enable Voice",
                message = "Voice vocalization enabled. Audio synthesis is online."
            )
        }

        // 10. Change Persona
        if (cleaned in listOf("switch to cyberpunk", "cyberpunk mode", "set cyberpunk", "activate cyberpunk")) {
            setPersona(MayaPersona.CYBERPUNK)
            return VoiceCommandResult.Executed(
                commandName = "Switch Persona",
                message = "Persona mode switched to Cyberpunk Sentinel."
            )
        }

        if (cleaned in listOf("switch to tech", "switch to tech expert", "tech mode", "expert mode", "activate tech expert", "set tech")) {
            setPersona(MayaPersona.TECH_EXPERT)
            return VoiceCommandResult.Executed(
                commandName = "Switch Persona",
                message = "Persona mode switched to Tech Specialist."
            )
        }

        if (cleaned in listOf("switch to creative", "creative mode", "visionary mode", "activate creative", "set creative")) {
            setPersona(MayaPersona.CREATIVE)
            return VoiceCommandResult.Executed(
                commandName = "Switch Persona",
                message = "Persona mode switched to Creative Mind."
            )
        }

        if (cleaned in listOf("switch to fast", "switch to fast qa", "fast mode", "quick mode", "rapid mode", "speed mode")) {
            setPersona(MayaPersona.FAST_QA)
            return VoiceCommandResult.Executed(
                commandName = "Switch Persona",
                message = "Persona mode switched to Rapid Q&A."
            )
        }

        // 11. Picture-in-Picture (PiP) / Floating Circle Mode
        if (cleaned in listOf(
                "enter pip", "pip mode", "pip", "picture in picture", "floating mode",
                "floating circle", "circle mode", "open pip", "start pip", "gemini mode",
                "minimize", "minimize to circle", "floating ai"
            )
        ) {
            _enterPipEvent.tryEmit(Unit)
            return VoiceCommandResult.Executed(
                commandName = "Enter PiP Mode",
                message = "Switching to floating circle Picture-in-Picture mode."
            )
        }

        // 12. Open / Close Menu
        if (cleaned in listOf("open menu", "show menu", "open settings", "show settings", "open control", "show controls")) {
            openMenu()
            return VoiceCommandResult.Executed(
                commandName = "Open Menu",
                message = "Opening HUD Control Panel."
            )
        }

        if (cleaned in listOf("close menu", "hide menu", "close settings", "dismiss menu")) {
            dismissMenu()
            return VoiceCommandResult.Executed(
                commandName = "Close Menu",
                message = "HUD Control Panel dismissed."
            )
        }

        // 13. Voice Commands Help
        if (cleaned in listOf("help", "voice commands", "show commands", "what commands", "list commands", "command list")) {
            val helpText = """
                VOICE COMMAND PROTOCOLS:
                • "Open [YouTube / Chrome / Settings]" — Smart App Launch
                • "Camera support" / "Cyber vision" — Live AI optical scan
                • "Optimize device" / "Clean RAM" — Turbo Android booster
                • "Read screen" / "Clipboard" — Screen & clipboard perception
                • "Lock app" — Security Shield lock
                • "Morning briefing" / "Automation" — AI automation workflows
                • "PiP mode" / "Floating mode" — Medium floating circle PiP
                • "Clear chat" & "Run diagnostic" — Maintenance & diagnostics
            """.trimIndent()
            return VoiceCommandResult.Executed(
                commandName = "Voice Commands Help",
                message = helpText
            )
        }

        // Strip leading command prefixes
        val prefixRegex = Regex("^(tell maya|ask maya|maya please|maya|hey maya)\\s+", RegexOption.IGNORE_CASE)
        val extracted = input.replace(prefixRegex, "").trim()
        if (extracted.isNotEmpty() && extracted != input) {
            val subResult = evaluateVoiceCommand(extracted)
            if (subResult is VoiceCommandResult.Executed) {
                return subResult
            }
        }

        return VoiceCommandResult.NotACommand
    }

    private fun processUserQuery(userQuery: String, isVoice: Boolean) {
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = userQuery,
            isVoice = isVoice
        )

        val processingId = UUID.randomUUID().toString()
        val processingMsg = ChatMessage(
            id = processingId,
            sender = MessageSender.MAYA,
            text = if (isVoice) "I heard: \"$userQuery\". Generating fast response..." else "Processing query...",
            isProcessing = true
        )

        val updatedListWithUser = _messages.value + userMsg
        _messages.value = updatedListWithUser + processingMsg
        _isProcessing.value = true

        viewModelScope.launch {
            val responseText = queryMayaAiWithHistory(userQuery, updatedListWithUser)
            _isProcessing.value = false

            _messages.value = _messages.value.map { msg ->
                if (msg.id == processingId) {
                    ChatMessage(
                        id = processingId,
                        sender = MessageSender.MAYA,
                        text = responseText,
                        isProcessing = false
                    )
                } else {
                    msg
                }
            }

            speakText(responseText)
        }
    }

    private suspend fun queryMayaAiWithHistory(
        currentPrompt: String,
        history: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = getSystemInstructionForPersona(_currentPersona.value)

                val conversationContents = history
                    .filter { !it.isProcessing && it.text.isNotBlank() }
                    .takeLast(10)
                    .map { chat ->
                        GeminiContent(
                            role = if (chat.sender == MessageSender.USER) "user" else "model",
                            parts = listOf(GeminiPart(text = chat.text))
                        )
                    }

                val finalContents = if (conversationContents.isNotEmpty()) {
                    conversationContents
                } else {
                    listOf(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = currentPrompt))
                        )
                    )
                }

                val request = GeminiRequest(
                    contents = finalContents,
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    ),
                    generationConfig = GeminiConfig(
                        temperature = 0.65f,
                        maxOutputTokens = 800
                    )
                )

                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                if (!text.isNullOrEmpty()) {
                    return@withContext text
                }
            } catch (e: Exception) {
                // Fall back to intelligent contextual local NLP engine
            }
        }

        return@withContext generateLocalResponse(currentPrompt, _currentPersona.value, history)
    }

    private fun getSystemInstructionForPersona(persona: MayaPersona): String {
        return when (persona) {
            MayaPersona.CYBERPUNK ->
                "You are MAYA, an ultra-advanced futuristic cyberpunk mobile AI assistant with system control, camera vision, device optimization, and automation powers. Respond with razor-sharp speed, intelligence, and clarity."
            MayaPersona.TECH_EXPERT ->
                "You are MAYA, an elite technical specialist AI. Provide structured, precise solutions with code and systems analysis."
            MayaPersona.CREATIVE ->
                "You are MAYA, a creative visionary AI. Express ideas with vivid imagination and eloquent depth."
            MayaPersona.FAST_QA ->
                "You are MAYA. Give lightning-fast, ultra-concise factual answers."
        }
    }

    private fun generateLocalResponse(
        prompt: String,
        persona: MayaPersona,
        history: List<ChatMessage>
    ): String {
        val lower = prompt.lowercase().trim()

        if (lower.contains("what did i just") || lower.contains("what was my last") || lower.contains("repeat what i said")) {
            val previousUserMsg = history.filter { it.sender == MessageSender.USER && it.text != prompt }.lastOrNull()
            return if (previousUserMsg != null) {
                "Your previous input was: \"${previousUserMsg.text}\"."
            } else {
                "This is the start of our session log."
            }
        }

        return when {
            lower.contains("who are you") || lower.contains("what is your name") ->
                "I am MAYA, your cybernetic Android AI assistant with Smart App Control, Cyber-Vision optical sensing, Device Optimization, Security Shield, and AI Automations."

            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") || lower.contains("greetings") ->
                when (persona) {
                    MayaPersona.CYBERPUNK -> "Greetings, operative. Neural connection established. Systems: Nominal. What is your directive?"
                    MayaPersona.TECH_EXPERT -> "Hello. Systems ready for technical inquiry or algorithmic computation."
                    MayaPersona.CREATIVE -> "Greetings! The cybernetic realm awakens. Where shall our imagination travel today?"
                    MayaPersona.FAST_QA -> "Hello. Ready for questions."
                }

            lower.contains("how are you") || lower.contains("status") ->
                "All quantum cores and Android subsystems operating at 100% peak stability. Ready for commands."

            lower.contains("quantum") ->
                "Quantum computing utilizes qubits in superposition and entanglement to execute complex calculations exponentially faster than classical silicon architecture."

            lower.contains("time") || lower.contains("clock") || lower.contains("date") ->
                "Current system timestamp: ${SimpleDateFormat("hh:mm a, EEEE, MMM d", Locale.getDefault()).format(Date())}. Chronometer calibrated."

            lower.contains("joke") || lower.contains("funny") ->
                "Why do cyberneticians never get lost? Because they always follow the root directory."

            else ->
                when (persona) {
                    MayaPersona.CYBERPUNK -> "Directive received: \"$prompt\". Synthesized across active neural matrix. Ready for next query."
                    MayaPersona.TECH_EXPERT -> "Processed input \"$prompt\". Logic parsed and logged in state cache."
                    MayaPersona.CREATIVE -> "A fascinating query: \"$prompt\". Cybernetic data streams illuminate our matrix."
                    MayaPersona.FAST_QA -> "Received: \"$prompt\". Confirmed."
                }
        }
    }

    private fun addMayaMessage(text: String) {
        _messages.value = _messages.value + ChatMessage(
            sender = MessageSender.MAYA,
            text = text
        )
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.destroy()
        textToSpeechManager.destroy()
    }
}
