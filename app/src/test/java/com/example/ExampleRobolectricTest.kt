package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.OsPreferences
import com.example.system.CallFilterManager
import com.example.system.CallScreenActionType
import com.example.system.SystemTelemetryEngine
import com.example.system.VirtualRam8GbEngine
import com.example.system.VirtualSpaceManager
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

    @Test
    fun `virtual 8GB RAM engine allocates separate 8192MB virtual memory pool`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vramEngine = VirtualRam8GbEngine(context)
        val info = vramEngine.allocateOrVerifyPool(enabled = true)

        assertEquals(8, info.virtualRamTotalGb)
        assertEquals(8192, info.virtualRamTotalMb)
        assertTrue(info.committedPagesCount > 0)
        assertTrue(info.committedSwapMb > 0f)
    }

    @Test
    fun `virtual space manager creates second account with valid virtual MAC`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vspace = VirtualSpaceManager(context)
        val account = vspace.createAccount("Работа", "work@miui.virtual")

        assertEquals("Работа", account.displayName)
        assertTrue(vspace.isValidMacAddress(account.virtualMac))
    }

    @Test
    fun `call filter evaluates unknown allowed and blocked numbers`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val filter = CallFilterManager(context)

        val unknownDecision = filter.evaluateNumber("+79990001122", OsPreferences())
        assertEquals(CallScreenActionType.PENDING_UNKNOWN, unknownDecision.first)

        val allowedPrefs = OsPreferences(allowedCallNumbers = setOf("+79990001122"))
        val allowedDecision = filter.evaluateNumber("+79990001122", allowedPrefs)
        assertEquals(CallScreenActionType.ALLOWED_BY_USER, allowedDecision.first)

        val blockedPrefs = OsPreferences(blockedCallNumbers = setOf("+79990001122"))
        val blockedDecision = filter.evaluateNumber("+79990001122", blockedPrefs)
        assertEquals(CallScreenActionType.REJECTED_BY_USER, blockedDecision.first)
    }
}
