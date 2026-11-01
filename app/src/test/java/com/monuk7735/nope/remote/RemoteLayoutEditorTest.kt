package com.monuk7735.nope.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.monuk7735.nope.remote.infrared.patterns.IRPattern
import com.monuk7735.nope.remote.infrared.patterns.IRPatternType
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteLayoutEditorTest {

    private val dummyPattern = IRPattern(IRPatternType.Intervals, 38000, intArrayOf(100, 100))

    @Test
    fun `isPlacedInGrid returns true for valid coordinates within grid bounds`() {
        val button = RemoteButtonDBModel(
            name = "POWER",
            offsetX = 100f,
            offsetY = 100f,
            irPattern = dummyPattern,
            gridCol = 2,
            gridRow = 3,
        )

        // For grid 5x10, valid cols are 0..4, valid rows are 0..9
        assertTrue(button.isPlacedInGrid(hCount = 5, vCount = 10))

        // Bottom row (row 9 in a 10-row grid) is valid
        val bottomRowButton = button.copy(gridRow = 9)
        assertTrue(bottomRowButton.isPlacedInGrid(hCount = 5, vCount = 10))
    }

    @Test
    fun `isPlacedInGrid returns false when grid coordinates are null or out of bounds`() {
        val legacyButton = RemoteButtonDBModel(
            name = "POWER",
            offsetX = 100f,
            offsetY = 100f,
            irPattern = dummyPattern,
            gridCol = null,
            gridRow = null,
        )
        assertFalse(legacyButton.isPlacedInGrid(hCount = 5, vCount = 10))

        val outOfBoundsCol = RemoteButtonDBModel(
            name = "VOL+",
            offsetX = 100f,
            offsetY = 100f,
            irPattern = dummyPattern,
            gridCol = 5, // 5 is out of bounds for hCount = 5 (0..4)
            gridRow = 2,
        )
        assertFalse(outOfBoundsCol.isPlacedInGrid(hCount = 5, vCount = 10))

        val outOfBoundsRow = RemoteButtonDBModel(
            name = "VOL-",
            offsetX = 100f,
            offsetY = 100f,
            irPattern = dummyPattern,
            gridCol = 2,
            gridRow = 10, // 10 is out of bounds for vCount = 10 (valid rows: 0..9)
        )
        assertFalse(outOfBoundsRow.isPlacedInGrid(hCount = 5, vCount = 10))
    }

    @Test
    fun `resizing grid without moving buttons correctly detects out-of-bounds buttons`() {
        val button1 = RemoteButtonDBModel(
            name = "POWER",
            offsetX = 0f,
            offsetY = 0f,
            irPattern = dummyPattern,
            gridCol = 0,
            gridRow = 0,
        )
        val button2 = RemoteButtonDBModel(
            name = "MENU",
            offsetX = 0f,
            offsetY = 0f,
            irPattern = dummyPattern,
            gridCol = 4, // Was valid in 5x10 grid
            gridRow = 1,
        )

        // In original 5x10 grid, both are placed
        assertTrue(button1.isPlacedInGrid(5, 10))
        assertTrue(button2.isPlacedInGrid(5, 10))

        // When user modifies grid to 4x10, positions are NOT moved automatically
        // button2 is at gridCol = 4, which is now invalid in a 4-column grid (valid: 0..3)
        assertTrue(button1.isPlacedInGrid(4, 10))
        assertFalse(button2.isPlacedInGrid(4, 10))
    }

    @Test
    fun `detects overlapping buttons sharing the same grid cell`() {
        val button1 = RemoteButtonDBModel(
            name = "POWER",
            offsetX = 0f,
            offsetY = 0f,
            irPattern = dummyPattern,
            gridCol = 2,
            gridRow = 3,
        )
        val button2 = RemoteButtonDBModel(
            name = "MUTE",
            offsetX = 0f,
            offsetY = 0f,
            irPattern = dummyPattern,
            gridCol = 2,
            gridRow = 3, // Collision!
        )
        val buttons = listOf(button1, button2)

        val collidingSlots = buttons.filter {
            it.gridCol != null && it.gridRow != null
        }.groupBy { it.gridCol to it.gridRow }.filter { it.value.size > 1 }

        assertEquals(1, collidingSlots.size)
        assertEquals(2 to 3, collidingSlots.keys.first())
        assertEquals(2, collidingSlots.values.first().size)
    }

    @Test
    fun `legacy JSON deserialization yields null for gridCol and gridRow`() {
        val gson = Gson()
        val legacyJson = """
            [
                {
                    "id": "test-id-1",
                    "offsetX": 250.5,
                    "offsetY": 400.0,
                    "name": "POWER",
                    "irPattern": {
                        "type": "Intervals",
                        "frequency": 38000,
                        "pattern": [100, 100]
                    }
                }
            ]
        """.trimIndent()

        val type = object : TypeToken<List<RemoteButtonDBModel>>() {}.type
        val buttons: List<RemoteButtonDBModel> = gson.fromJson(legacyJson, type)

        assertEquals(1, buttons.size)
        assertEquals("POWER", buttons[0].name)
        assertNull(buttons[0].gridCol)
        assertNull(buttons[0].gridRow)
        assertEquals(250.5f, buttons[0].offsetX, 0.01f)
    }

    @Test
    fun `roundtrip serialization preserves grid coordinates`() {
        val gson = Gson()
        val button = RemoteButtonDBModel(
            name = "OK",
            offsetX = 120f,
            offsetY = 340f,
            irPattern = dummyPattern,
            gridCol = 3,
            gridRow = 5,
        )

        val json = gson.toJson(listOf(button))
        val type = object : TypeToken<List<RemoteButtonDBModel>>() {}.type
        val deserialized: List<RemoteButtonDBModel> = gson.fromJson(json, type)

        assertEquals(1, deserialized.size)
        assertEquals("OK", deserialized[0].name)
        assertEquals(3, deserialized[0].gridCol)
        assertEquals(5, deserialized[0].gridRow)
    }

    @Test
    fun `RemoteDataDBModel default grid dimensions are 5 horizontal and 10 vertical`() {
        val remote = RemoteDataDBModel(
            id = 1,
            name = "Living Room TV",
            brand = "Sony",
            type = "TV",
            added = Date(),
            onScreenRemoteButtonDBS = emptyList(),
            offScreenRemoteButtonDBS = emptyList(),
        )

        assertEquals(5, remote.gridHCount)
        assertEquals(10, remote.gridVCount)
    }
}
