package com.monuk7735.nope.remote

import android.content.Context
import android.hardware.ConsumerIrManager
import androidx.test.core.app.ApplicationProvider
import com.monuk7735.nope.remote.infrared.IRController
import com.monuk7735.nope.remote.infrared.patterns.IRPattern
import com.monuk7735.nope.remote.infrared.patterns.IRPatternType
import com.monuk7735.nope.remote.models.custom.macros.MacroTransmit
import com.monuk7735.nope.remote.models.database.MacroDataDBModel
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.utils.VibrationHelper
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowVibrator
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class VibrationHelperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        clearPreferences()
    }

    @After
    fun tearDown() {
        clearPreferences()
    }

    private fun clearPreferences() {
        val prefs = context.getSharedPreferences(
            context.getString(R.string.shared_pref_app_settings),
            Context.MODE_PRIVATE,
        )
        prefs.edit().clear().commit()
    }

    private fun setVibrationSetting(enabled: Boolean) {
        val prefs = context.getSharedPreferences(
            context.getString(R.string.shared_pref_app_settings),
            Context.MODE_PRIVATE,
        )
        prefs.edit().putBoolean(
            context.getString(R.string.pref_settings_vibration),
            enabled,
        ).commit()
    }

    @Test
    fun testShouldVibrateDefaultsToTrue() {
        assertTrue(VibrationHelper.shouldVibrate(context))
    }

    @Test
    fun testShouldVibrateWhenDisabled() {
        setVibrationSetting(false)
        assertFalse(VibrationHelper.shouldVibrate(context))
    }

    @Test
    fun testShouldVibrateWhenEnabled() {
        setVibrationSetting(true)
        assertTrue(VibrationHelper.shouldVibrate(context))
    }

    @Test
    fun testGetVibratorNotNull() {
        val vibrator = VibrationHelper.getVibrator(context)
        assertNotNull("Vibrator should not be null", vibrator)
    }

    @Test
    fun testVibrateDoesNotVibrateWhenDisabled() {
        setVibrationSetting(false)
        val vibrator = VibrationHelper.getVibrator(context)!!
        val shadowVibrator: ShadowVibrator = shadowOf(vibrator)

        VibrationHelper.vibrate(context, vibrator, 100)

        assertFalse("Should not vibrate when setting is false", shadowVibrator.isVibrating)
        assertEquals(0L, shadowVibrator.milliseconds)
    }

    @Test
    fun testVibrateVibratesWhenEnabled() {
        setVibrationSetting(true)
        val vibrator = VibrationHelper.getVibrator(context)!!
        val shadowVibrator: ShadowVibrator = shadowOf(vibrator)

        VibrationHelper.vibrate(context, vibrator, 100)

        assertTrue("Should vibrate when setting is true", shadowVibrator.isVibrating)
        assertEquals(100L, shadowVibrator.milliseconds)
    }

    @Test
    fun testIrControllerTransmitDoesNotVibrateWhenDisabled() {
        setVibrationSetting(false)
        val consumerIrManager = ReflectionHelpers.newInstance(ConsumerIrManager::class.java)
        val irController = IRController(consumerIrManager, context)
        val vibrator = VibrationHelper.getVibrator(context)!!
        val shadowVibrator: ShadowVibrator = shadowOf(vibrator)
        val pattern = IRPattern(IRPatternType.Intervals, 38000, intArrayOf(100, 100))

        irController.transmit(pattern, vibrator)

        assertFalse(
            "IRController should not trigger vibration when setting is false",
            shadowVibrator.isVibrating,
        )
        assertEquals(0L, shadowVibrator.milliseconds)
    }

    @Test
    fun testIrControllerTransmitVibratesWhenEnabled() {
        setVibrationSetting(true)
        val consumerIrManager = ReflectionHelpers.newInstance(ConsumerIrManager::class.java)
        val irController = IRController(consumerIrManager, context)
        val vibrator = VibrationHelper.getVibrator(context)!!
        val shadowVibrator: ShadowVibrator = shadowOf(vibrator)
        val pattern = IRPattern(IRPatternType.Intervals, 38000, intArrayOf(100, 100))

        irController.transmit(pattern, vibrator)

        assertTrue(
            "IRController should trigger vibration when setting is true",
            shadowVibrator.isVibrating,
        )
        assertEquals(100L, shadowVibrator.milliseconds)
    }

    @Test
    fun testIrControllerTransmitWithoutExplicitVibratorRespectsDisabledSetting() {
        setVibrationSetting(false)
        val consumerIrManager = ReflectionHelpers.newInstance(ConsumerIrManager::class.java)
        val irController = IRController(consumerIrManager, context)
        val vibrator = VibrationHelper.getVibrator(context)!!
        val shadowVibrator: ShadowVibrator = shadowOf(vibrator)
        val pattern = IRPattern(IRPatternType.Intervals, 38000, intArrayOf(100, 100))

        irController.transmit(pattern)

        assertFalse(
            "IRController should not vibrate when setting is disabled",
            shadowVibrator.isVibrating,
        )
        assertEquals(0L, shadowVibrator.milliseconds)
    }

    @Test
    fun testIrControllerTransmitWithoutExplicitVibratorVibratesWhenEnabled() {
        setVibrationSetting(true)
        val consumerIrManager = ReflectionHelpers.newInstance(ConsumerIrManager::class.java)
        val irController = IRController(consumerIrManager, context)
        val vibrator = VibrationHelper.getVibrator(context)!!
        val shadowVibrator: ShadowVibrator = shadowOf(vibrator)
        val pattern = IRPattern(IRPatternType.Intervals, 38000, intArrayOf(100, 100))

        irController.transmit(pattern)

        assertTrue(
            "IRController should vibrate when setting is enabled",
            shadowVibrator.isVibrating,
        )
        assertEquals(100L, shadowVibrator.milliseconds)
    }

    @Test
    fun testRemoteButtonTransmitDoesNotVibrateWhenDisabled() {
        setVibrationSetting(false)
        val consumerIrManager = ReflectionHelpers.newInstance(ConsumerIrManager::class.java)
        val irController = IRController(consumerIrManager, context)
        val vibrator = VibrationHelper.getVibrator(context)!!
        val shadowVibrator: ShadowVibrator = shadowOf(vibrator)
        val pattern = IRPattern(IRPatternType.Intervals, 38000, intArrayOf(100, 100))
        val button =
            RemoteButtonDBModel(offsetX = 0f, offsetY = 0f, name = "POWER", irPattern = pattern)

        button.transmit(irController, vibrator)

        assertFalse(
            "RemoteButton should not vibrate when setting is false",
            shadowVibrator.isVibrating,
        )
        assertEquals(0L, shadowVibrator.milliseconds)
    }

    @Test
    fun testRemoteButtonTransmitVibratesWhenEnabled() {
        setVibrationSetting(true)
        val consumerIrManager = ReflectionHelpers.newInstance(ConsumerIrManager::class.java)
        val irController = IRController(consumerIrManager, context)
        val vibrator = VibrationHelper.getVibrator(context)!!
        val shadowVibrator: ShadowVibrator = shadowOf(vibrator)
        val pattern = IRPattern(IRPatternType.Intervals, 38000, intArrayOf(100, 100))
        val button =
            RemoteButtonDBModel(offsetX = 0f, offsetY = 0f, name = "POWER", irPattern = pattern)

        button.transmit(irController, vibrator)

        assertTrue("RemoteButton should vibrate when setting is true", shadowVibrator.isVibrating)
        assertEquals(100L, shadowVibrator.milliseconds)
    }

    @Test
    fun testMacroDataExecuteDoesNotVibrateWhenDisabled() = runBlocking {
        setVibrationSetting(false)
        val consumerIrManager = ReflectionHelpers.newInstance(ConsumerIrManager::class.java)
        val irController = IRController(consumerIrManager, context)
        val vibrator = VibrationHelper.getVibrator(context)!!
        val shadowVibrator: ShadowVibrator = shadowOf(vibrator)
        val pattern = IRPattern(IRPatternType.Intervals, 38000, intArrayOf(100, 100))
        val macro = MacroDataDBModel(
            id = 1,
            name = "Test Macro",
            macroUnits = listOf(
                MacroTransmit("POWER", 1, "POWER", pattern),
            ),
        )

        macro.execute(irController, vibrator)

        assertFalse(
            "MacroData should not vibrate when setting is false",
            shadowVibrator.isVibrating,
        )
        assertEquals(0L, shadowVibrator.milliseconds)
    }

    @Test
    fun testMacroDataExecuteVibratesWhenEnabled() = runBlocking {
        setVibrationSetting(true)
        val consumerIrManager = ReflectionHelpers.newInstance(ConsumerIrManager::class.java)
        val irController = IRController(consumerIrManager, context)
        val vibrator = VibrationHelper.getVibrator(context)!!
        val shadowVibrator: ShadowVibrator = shadowOf(vibrator)
        val pattern = IRPattern(IRPatternType.Intervals, 38000, intArrayOf(100, 100))
        val macro = MacroDataDBModel(
            id = 1,
            name = "Test Macro",
            macroUnits = listOf(
                MacroTransmit("POWER", 1, "POWER", pattern),
            ),
        )

        macro.execute(irController, vibrator)

        assertTrue("MacroData should vibrate when setting is true", shadowVibrator.isVibrating)
        assertEquals(100L, shadowVibrator.milliseconds)
    }
}
