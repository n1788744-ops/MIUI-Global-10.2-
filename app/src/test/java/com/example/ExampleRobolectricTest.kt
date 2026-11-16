package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.system.SystemTelemetryEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MIUI 10 OS", appName)
    }

    @Test
    fun `telemetry engine enforces 256MB OS profile and 1_5GHz cpu target`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = SystemTelemetryEngine(context)
        engine.primeOptimizationMemoryPool()
        val telemetry = engine.runBackgroundOptimizationSweep()

        assertEquals(256, telemetry.osTargetRamMb)
        assertEquals(64, telemetry.bgOptimizationPoolMb)
        assertTrue(telemetry.optimizationBufferAllocatedKb > 0L)
        assertTrue(telemetry.cpuCurrentGhz >= 1.50f)
        assertTrue(telemetry.isCpuSmoothReady)
        assertEquals(1, telemetry.optimizationCyclesRun)
    }
}
