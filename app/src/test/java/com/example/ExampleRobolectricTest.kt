package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MessageSender
import com.example.ui.MayaViewModel
import com.example.ui.components.MayaPersona
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Maya AI", appName)
    }

    @Test
    fun `voice command clear chat resets messages`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MayaViewModel(app)

        // Send a normal prompt first
        viewModel.onInputTextChanged("Hello Maya")
        viewModel.sendMessage()

        // Send clear command via voice
        viewModel.onInputTextChanged("clear chat")
        viewModel.sendMessage()

        // Verify message reset
        val messages = viewModel.messages.value
        assertTrue(messages.isNotEmpty())
        assertTrue(messages.last().text.contains("cleared") || messages.last().text.contains("re-initialized"))
    }

    @Test
    fun `voice command switches persona`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MayaViewModel(app)

        viewModel.onInputTextChanged("switch to tech expert")
        viewModel.sendMessage()

        assertEquals(MayaPersona.TECH_EXPERT, viewModel.currentPersona.value)
    }

    @Test
    fun `voice command toggles audio mute and unmute`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MayaViewModel(app)

        viewModel.onInputTextChanged("mute voice")
        viewModel.sendMessage()
        assertFalse(viewModel.isTtsEnabled.value)

        viewModel.onInputTextChanged("unmute voice")
        viewModel.sendMessage()
        assertTrue(viewModel.isTtsEnabled.value)
    }

    @Test
    fun `voice command triggers pip mode`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MayaViewModel(app)

        viewModel.onInputTextChanged("pip mode")
        viewModel.sendMessage()

        assertEquals("Enter PiP Mode", viewModel.lastVoiceCommand.value)
    }

    @Test
    fun `voice command triggers android optimization`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MayaViewModel(app)

        viewModel.onInputTextChanged("optimize device")
        viewModel.sendMessage()

        assertEquals("Android Optimization", viewModel.lastVoiceCommand.value)
    }

    @Test
    fun `voice command triggers cyber vision camera`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MayaViewModel(app)

        viewModel.onInputTextChanged("open camera")
        viewModel.sendMessage()

        assertEquals("Cyber Vision", viewModel.lastVoiceCommand.value)
        assertTrue(viewModel.isCyberVisionOpen.value)
    }

    @Test
    fun `voice command triggers morning briefing automation`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MayaViewModel(app)

        viewModel.onInputTextChanged("morning briefing")
        viewModel.sendMessage()

        assertEquals("AI Automation", viewModel.lastVoiceCommand.value)
    }

    @Test
    fun `app lock verify and unlock protocol`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MayaViewModel(app)

        viewModel.setAppLock("1234", true)
        assertTrue(viewModel.securityLockState.value.isLockEnabled)

        viewModel.lockAppNow()
        assertTrue(viewModel.securityLockState.value.isLocked)

        assertFalse(viewModel.verifyPin("9999"))
        assertTrue(viewModel.verifyPin("1234"))
        assertFalse(viewModel.securityLockState.value.isLocked)
    }
}
