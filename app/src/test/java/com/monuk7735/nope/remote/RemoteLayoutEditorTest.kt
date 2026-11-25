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

    @Test
    fun `legacy button with null coordinates maps correctly to grid based on pixel center`() {
        // Screen width = 1000f, height = 2000f, grid 5x10 -> cell width = 200f, height = 200f
        val cellWidth = 200f
        val cellHeight = 200f
        val buttonSize = 72f

        // Top-left button: center = (10 + 36, 10 + 36) = (46, 46) -> cell (0, 0)
        val topLeftButton = RemoteButtonDBModel(
            name = "POWER",
            offsetX = 10f,
            offsetY = 10f,
            irPattern = dummyPattern,
            gridCol = null,
            gridRow = null,
        )
        val (col0, row0) = topLeftButton.resolveGridCoordinates(
            gridSizeXPx = cellWidth,
            gridSizeYPx = cellHeight,
            buttonSizePx = buttonSize,
            hCount = 5,
            vCount = 10,
        )
        assertEquals(0, col0)
        assertEquals(0, row0)

        // Middle button: center = (450 + 36, 650 + 36) = (486, 686)
        // 486 / 200 = 2, 686 / 200 = 3 -> cell (2, 3)
        val middleButton = RemoteButtonDBModel(
            name = "OK",
            offsetX = 450f,
            offsetY = 650f,
            irPattern = dummyPattern,
            gridCol = null,
            gridRow = null,
        )
        val (colMiddle, rowMiddle) = middleButton.resolveGridCoordinates(
            gridSizeXPx = cellWidth,
            gridSizeYPx = cellHeight,
            buttonSizePx = buttonSize,
            hCount = 5,
            vCount = 10,
        )
        assertEquals(2, colMiddle)
        assertEquals(3, rowMiddle)

        // Bottom-right button: center = (850 + 36, 1850 + 36) = (886, 1886)
        // 886 / 200 = 4, 1886 / 200 = 9 -> cell (4, 9)
        val bottomRightButton = RemoteButtonDBModel(
            name = "INFO",
            offsetX = 850f,
            offsetY = 1850f,
            irPattern = dummyPattern,
            gridCol = null,
            gridRow = null,
        )
        val (colBR, rowBR) = bottomRightButton.resolveGridCoordinates(
            gridSizeXPx = cellWidth,
            gridSizeYPx = cellHeight,
            buttonSizePx = buttonSize,
            hCount = 5,
            vCount = 10,
        )
        assertEquals(4, colBR)
        assertEquals(9, rowBR)

        // Negative offsets clamp to (0, 0)
        val negativeButton = RemoteButtonDBModel(
            name = "NEGATIVE",
            offsetX = -50f,
            offsetY = -20f,
            irPattern = dummyPattern,
            gridCol = null,
            gridRow = null,
        )
        val (colNeg, rowNeg) = negativeButton.resolveGridCoordinates(
            gridSizeXPx = cellWidth,
            gridSizeYPx = cellHeight,
            buttonSizePx = buttonSize,
            hCount = 5,
            vCount = 10,
        )
        assertEquals(0, colNeg)
        assertEquals(0, rowNeg)

        // Out-of-bounds large offsets clamp to (hCount - 1, vCount - 1)
        val oobButton = RemoteButtonDBModel(
            name = "OOB",
            offsetX = 1500f,
            offsetY = 3000f,
            irPattern = dummyPattern,
            gridCol = null,
            gridRow = null,
        )
        val (colOob, rowOob) = oobButton.resolveGridCoordinates(
            gridSizeXPx = cellWidth,
            gridSizeYPx = cellHeight,
            buttonSizePx = buttonSize,
            hCount = 5,
            vCount = 10,
        )
        assertEquals(4, colOob)
        assertEquals(9, rowOob)
    }

    @Test
    fun `legacy button center-based grid mapping renders button centered in grid cell`() {
        val cellWidth = 200f
        val cellHeight = 200f
        val buttonSize = 72f

        // A button at offset (450f, 650f) maps to col 2, row 3
        val button = RemoteButtonDBModel(
            name = "OK",
            offsetX = 450f,
            offsetY = 650f,
            irPattern = dummyPattern,
            gridCol = null,
            gridRow = null,
        )

        // Expected centered position in cell (2, 3):
        // posX = 200 * 2 + (200 - 72) / 2 = 400 + 64 = 464f
        // posY = 200 * 3 + (200 - 72) / 2 = 600 + 64 = 664f
        val (posX, posY) = button.getRenderPosition(
            gridSizeXPx = cellWidth,
            gridSizeYPx = cellHeight,
            buttonSizePx = buttonSize,
            hCount = 5,
            vCount = 10,
        )

        assertEquals(464f, posX, 0.01f)
        assertEquals(664f, posY, 0.01f)
    }

    @Test
    fun `button with explicit grid coordinates ignores legacy pixel offsets`() {
        val button = RemoteButtonDBModel(
            name = "EXPLICIT",
            offsetX = 9999f,
            offsetY = 9999f,
            irPattern = dummyPattern,
            gridCol = 1,
            gridRow = 2,
        )

        val (col, row) = button.resolveGridCoordinates(
            gridSizeXPx = 200f,
            gridSizeYPx = 200f,
            buttonSizePx = 72f,
            hCount = 5,
            vCount = 10,
        )
        assertEquals(1, col)
        assertEquals(2, row)

        val (posX, posY) = button.getRenderPosition(
            gridSizeXPx = 200f,
            gridSizeYPx = 200f,
            buttonSizePx = 72f,
            hCount = 5,
            vCount = 10,
        )
        // posX = 200 * 1 + (200 - 72) / 2 = 264f
        // posY = 200 * 2 + (200 - 72) / 2 = 464f
        assertEquals(264f, posX, 0.01f)
        assertEquals(464f, posY, 0.01f)
    }

    @Test
    fun `isReservedCorner correctly identifies bottom-left and bottom-right grid corners`() {
        val bottomLeft = RemoteButtonDBModel(
            name = "DIGITS",
            offsetX = 0f,
            offsetY = 0f,
            irPattern = dummyPattern,
            gridCol = 0,
            gridRow = 9,
        )
        assertTrue(bottomLeft.isReservedCorner(hCount = 5, vCount = 10))

        val bottomRight = RemoteButtonDBModel(
            name = "MORE",
            offsetX = 0f,
            offsetY = 0f,
            irPattern = dummyPattern,
            gridCol = 4,
            gridRow = 9,
        )
        assertTrue(bottomRight.isReservedCorner(hCount = 5, vCount = 10))

        val topLeft = RemoteButtonDBModel(
            name = "POWER",
            offsetX = 0f,
            offsetY = 0f,
            irPattern = dummyPattern,
            gridCol = 0,
            gridRow = 0,
        )
        assertFalse(topLeft.isReservedCorner(hCount = 5, vCount = 10))

        val middle = RemoteButtonDBModel(
            name = "CENTER",
            offsetX = 0f,
            offsetY = 0f,
            irPattern = dummyPattern,
            gridCol = 2,
            gridRow = 5,
        )
        assertFalse(middle.isReservedCorner(hCount = 5, vCount = 10))

        val bottomCenter = RemoteButtonDBModel(
            name = "BOTTOM_MID",
            offsetX = 0f,
            offsetY = 0f,
            irPattern = dummyPattern,
            gridCol = 2,
            gridRow = 9,
        )
        assertFalse(bottomCenter.isReservedCorner(hCount = 5, vCount = 10))
    }

    @Test
    fun `collision detection catches legacy buttons mapping to the same grid cell`() {
        val cellWidth = 200f
        val cellHeight = 200f
        val buttonSize = 72f

        // Two legacy buttons placed close to each other in pixel space
        val button1 = RemoteButtonDBModel(
            name = "POWER",
            offsetX = 10f,
            offsetY = 10f,
            irPattern = dummyPattern,
            gridCol = null,
            gridRow = null,
        )
        val button2 = RemoteButtonDBModel(
            name = "MUTE",
            offsetX = 60f,
            offsetY = 40f,
            irPattern = dummyPattern,
            gridCol = null,
            gridRow = null,
        )
        val button3 = RemoteButtonDBModel(
            name = "VOL+",
            offsetX = 250f,
            offsetY = 10f,
            irPattern = dummyPattern,
            gridCol = null,
            gridRow = null,
        )

        val buttons = listOf(button1, button2, button3)
        val mappedSlots = buttons.groupBy {
            it.resolveGridCoordinates(cellWidth, cellHeight, buttonSize, 5, 10)
        }

        val collidingSlots = mappedSlots.filter { it.value.size > 1 }
        assertEquals(1, collidingSlots.size)
        assertEquals(0 to 0, collidingSlots.keys.first())
        assertEquals(2, collidingSlots.values.first().size)
    }

    @Test
    fun `fallback to raw offsets when grid dimensions are zero or invalid`() {
        val button = RemoteButtonDBModel(
            name = "RAW",
            offsetX = 123.4f,
            offsetY = 567.8f,
            irPattern = dummyPattern,
            gridCol = null,
            gridRow = null,
        )

        val (posX, posY) = button.getRenderPosition(
            gridSizeXPx = 0f,
            gridSizeYPx = 0f,
            buttonSizePx = 72f,
            hCount = 5,
            vCount = 10,
        )
        assertEquals(123.4f, posX, 0.01f)
        assertEquals(567.8f, posY, 0.01f)
    }
}
