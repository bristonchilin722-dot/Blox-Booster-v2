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
  }
}
