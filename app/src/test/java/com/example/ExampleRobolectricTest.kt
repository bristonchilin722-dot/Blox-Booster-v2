package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Blox Booster", appName)
  }

  @Test
  fun `device hardware info gets specs without throwing on Application context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val spec = com.example.util.DeviceHardwareInfo.getSpecs(context)
    org.junit.Assert.assertNotNull(spec)
    org.junit.Assert.assertTrue(spec.screenWidth > 0)
    org.junit.Assert.assertTrue(spec.screenHeight > 0)
    org.junit.Assert.assertTrue(spec.refreshRateHz > 0)
  }

  @Test
  fun `viewmodel instantiates successfully with Application context`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.viewmodel.BloxBoosterViewModel(app)
    org.junit.Assert.assertNotNull(vm)
    org.junit.Assert.assertNotNull(vm.deviceSpec.value)
    org.junit.Assert.assertNotNull(vm.visualConfig.value)
    org.junit.Assert.assertNotNull(vm.shizukuState.value)
  }

  @Test
  fun `potato visual engine toggles and creates color matrix`() {
    com.example.engine.PotatoVisualEngine.togglePotatoVisualMode(true)
    val cfg = com.example.engine.PotatoVisualEngine.config.value
    org.junit.Assert.assertTrue(cfg.potatoModeActive)
    org.junit.Assert.assertTrue(cfg.saturation > 1.0f)
    org.junit.Assert.assertTrue(cfg.gammaLift > 1.0f)

    val matrix = com.example.engine.PotatoVisualEngine.buildColorMatrix(cfg)
    org.junit.Assert.assertNotNull(matrix)

    com.example.engine.PotatoVisualEngine.restoreDefaults()
    val restored = com.example.engine.PotatoVisualEngine.config.value
    org.junit.Assert.assertFalse(restored.potatoModeActive)
    org.junit.Assert.assertEquals(1.0f, restored.saturation, 0.001f)
  }

  @Test
  fun `resolution manager generates valid adb commands and presets`() {
    val presets = com.example.util.ResolutionManager.getPresets(1080, 2400, 400)
    org.junit.Assert.assertEquals(4, presets.size)

    val adbCmd = com.example.util.ResolutionManager.generateAdbCommand(720, 1600, 267)
    org.junit.Assert.assertTrue(adbCmd.contains("wm size 720x1600"))
    org.junit.Assert.assertTrue(adbCmd.contains("wm density 267"))

    val resetCmd = com.example.util.ResolutionManager.generateResetAdbCommand()
    org.junit.Assert.assertEquals("adb shell wm size reset && adb shell wm density reset", resetCmd)
  }

  @Test
  fun `shizuku manager reports correct initial status without crashing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.util.ShizukuManager.init(context)
    val state = com.example.util.ShizukuManager.state.value
    org.junit.Assert.assertNotNull(state)
    org.junit.Assert.assertNotNull(state.status)
  }
}
