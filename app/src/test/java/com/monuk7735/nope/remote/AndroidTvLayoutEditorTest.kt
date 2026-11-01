package com.monuk7735.nope.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.monuk7735.nope.remote.androidtv.proto.remote.Remotemessage
import com.monuk7735.nope.remote.models.database.AndroidTvButtonCatalog
import com.monuk7735.nope.remote.models.database.AndroidTvButtonDBModel
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AndroidTvLayoutEditorTest {

    private val gson = Gson()

    @Test
    fun testAndroidTvButtonCatalog_containsAllStandardKeys() {
        val allButtons = AndroidTvButtonCatalog.createAllAvailableButtons()
        assertTrue("Catalog should contain comprehensive set of buttons", allButtons.size >= 35)

        val keyCodes = allButtons.map { it.keyCodeName }.toSet()
        val expectedKeys = listOf(
            "KEYCODE_POWER",
            "KEYCODE_TV_POWER",
            "ACTION_KEYBOARD",
            "KEYCODE_DPAD_UP",
            "KEYCODE_DPAD_DOWN",
            "KEYCODE_DPAD_LEFT",
            "KEYCODE_DPAD_RIGHT",
            "KEYCODE_DPAD_CENTER",
            "KEYCODE_BACK",
            "KEYCODE_HOME",
            "KEYCODE_MENU",
            "KEYCODE_SETTINGS",
            "KEYCODE_TV_INPUT",
            "KEYCODE_VOLUME_UP",
            "KEYCODE_VOLUME_DOWN",
            "KEYCODE_VOLUME_MUTE",
            "KEYCODE_CHANNEL_UP",
            "KEYCODE_CHANNEL_DOWN",
            "KEYCODE_MEDIA_PLAY_PAUSE",
            "KEYCODE_MEDIA_PLAY",
            "KEYCODE_MEDIA_PAUSE",
            "KEYCODE_MEDIA_STOP",
            "KEYCODE_MEDIA_REWIND",
            "KEYCODE_MEDIA_FAST_FORWARD",
            "KEYCODE_MEDIA_PREVIOUS",
            "KEYCODE_MEDIA_NEXT",
            "KEYCODE_VOICE_ASSIST",
            "KEYCODE_SEARCH",
            "KEYCODE_CAPTIONS",
            "KEYCODE_0",
            "KEYCODE_1",
            "KEYCODE_9",
        )

        for (expected in expectedKeys) {
            assertTrue("Expected key $expected in catalog", keyCodes.contains(expected))
        }
    }

    @Test
    fun testAndroidTvDefaultButtons_validPlacement() {
        val defaultButtons = AndroidTvButtonCatalog.getDefaultOnScreenButtons()
        assertFalse(defaultButtons.isEmpty())

        val hCount = 5
        val vCount = 10

        // Check each button is within bounds
        for (btn in defaultButtons) {
            assertTrue(
                "Button ${btn.name} must be placed within bounds",
                btn.isPlacedInGrid(hCount, vCount),
            )
        }

        // Check no two buttons occupy the same cell
        val slots = defaultButtons.map { it.gridCol to it.gridRow }
        assertEquals("No overlapping buttons in default layout", slots.size, slots.toSet().size)
    }

    @Test
    fun testAndroidTvButton_gridPlacementAndValidation() {
        val inBounds = AndroidTvButtonDBModel(
            name = "Power",
            keyCodeName = "KEYCODE_POWER",
            gridCol = 0,
            gridRow = 0,
        )
        assertTrue(inBounds.isPlacedInGrid(5, 10))

        val bottomRow = AndroidTvButtonDBModel(
            name = "Vol Down",
            keyCodeName = "KEYCODE_VOLUME_DOWN",
            gridCol = 4,
            gridRow = 9,
        )
        assertTrue("Row 9 is valid in 10-row grid (0..9)", bottomRow.isPlacedInGrid(5, 10))

        val outOfBoundsRow = AndroidTvButtonDBModel(
            name = "Out",
            keyCodeName = "KEYCODE_INFO",
            gridCol = 2,
            gridRow = 10,
        )
        assertFalse("Row 10 is out of bounds in 10-row grid", outOfBoundsRow.isPlacedInGrid(5, 10))

        val outOfBoundsCol = AndroidTvButtonDBModel(
            name = "Out",
            keyCodeName = "KEYCODE_INFO",
            gridCol = 5,
            gridRow = 2,
        )
        assertFalse("Col 5 is out of bounds in 5-col grid", outOfBoundsCol.isPlacedInGrid(5, 10))

        val nullCoords = AndroidTvButtonDBModel(
            name = "Unplaced",
            keyCodeName = "KEYCODE_INFO",
        )
        assertFalse(nullCoords.isPlacedInGrid(5, 10))
    }

    @Test
    fun testSavedTvDeviceDBModel_effectiveButtonsAndDigits() {
        val device = SavedTvDeviceDBModel(
            name = "Living Room TV",
            host = "192.168.1.100",
        )

        val onScreen = device.getEffectiveOnScreenButtons()
        val offScreen = device.getEffectiveOffScreenButtons()
        assertFalse(onScreen.isEmpty())
        assertFalse(offScreen.isEmpty())

        val allDigits = device.getAllDigits()
        assertEquals("Should have all 10 digits 0..9", 10, allDigits.size)
        for (i in 0..9) {
            assertEquals(i, allDigits[i].getDigitValue())
        }

        val nonDigits = device.getAllOffScreenNonDigits()
        for (btn in nonDigits) {
            assertNull("Non-digits list should not contain digits", btn.getDigitValue())
        }
    }

    @Test
    fun testAndroidTvButtonDBModel_jsonRoundtrip() {
        val buttons = listOf(
            AndroidTvButtonDBModel(
                name = "Power",
                keyCodeName = "KEYCODE_POWER",
                gridCol = 0,
                gridRow = 0,
                offsetX = 10f,
                offsetY = 20f,
            ),
            AndroidTvButtonDBModel(
                name = "Keyboard",
                keyCodeName = "ACTION_KEYBOARD",
                gridCol = 4,
                gridRow = 0,
            ),
            AndroidTvButtonDBModel(
                name = "OK",
                keyCodeName = "KEYCODE_DPAD_CENTER",
                gridCol = 2,
                gridRow = 3,
            ),
        )

        val json = gson.toJson(buttons)
        val deserialized: List<AndroidTvButtonDBModel> =
            gson.fromJson(json, object : TypeToken<List<AndroidTvButtonDBModel>>() {}.type)

        assertEquals(buttons.size, deserialized.size)
        for (i in buttons.indices) {
            assertEquals(buttons[i].id, deserialized[i].id)
            assertEquals(buttons[i].name, deserialized[i].name)
            assertEquals(buttons[i].keyCodeName, deserialized[i].keyCodeName)
            assertEquals(buttons[i].gridCol, deserialized[i].gridCol)
            assertEquals(buttons[i].gridRow, deserialized[i].gridRow)
            assertEquals(buttons[i].offsetX, deserialized[i].offsetX, 0.01f)
            assertEquals(buttons[i].offsetY, deserialized[i].offsetY, 0.01f)
        }
    }

    @Test
    fun testAndroidTvButton_remoteKeyCodeResolution() {
        val power = AndroidTvButtonDBModel(name = "Power", keyCodeName = "KEYCODE_POWER")
        assertEquals(Remotemessage.RemoteKeyCode.KEYCODE_POWER, power.getRemoteKeyCode())
        assertFalse(power.isKeyboardAction())

        val keyboard = AndroidTvButtonDBModel(name = "Keyboard", keyCodeName = "ACTION_KEYBOARD")
        assertTrue(keyboard.isKeyboardAction())
        assertNull(keyboard.getRemoteKeyCode())

        val ok = AndroidTvButtonDBModel(name = "OK", keyCodeName = "KEYCODE_DPAD_CENTER")
        assertEquals("OK", ok.getTextIcon())
        assertNull(ok.getIcon())

        val home = AndroidTvButtonDBModel(name = "Home", keyCodeName = "KEYCODE_HOME")
        assertNotNull(home.getIcon())
        assertNull(home.getTextIcon())
    }
}
