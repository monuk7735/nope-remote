package com.monuk7735.nope.remote

import com.monuk7735.nope.remote.infrared.IrCsvParser
import com.monuk7735.nope.remote.models.database.RemoteButtonDBModel
import org.junit.Assert.*
import org.junit.Test

class IrCsvParserTest {

    @Test
    fun testSony12Parsing() {
        val csv = """
            functionname,protocol,device,subdevice,function
            KEY 1,Sony12,1,-1,0
            POWER ON/OFF,Sony12,1,-1,21
        """.trimIndent()

        val results = IrCsvParser.parseCsvAndGenerateHex(csv)
        assertEquals(2, results.size)
        assertTrue(results.containsKey("KEY 1"))
        assertTrue(results.containsKey("POWER ON/OFF"))

        val powerHex = results["POWER ON/OFF"]!!
        assertTrue("Expected Pronto hex header", powerHex.startsWith("0000 "))
    }

    @Test
    fun testSony15And20Parsing() {
        val csv = """
            functionname,protocol,device,subdevice,function
            STOP,Sony15,151,-1,24
            VIDEO OFF,Sony20,26,42,80
        """.trimIndent()

        val results = IrCsvParser.parseCsvAndGenerateHex(csv)
        assertEquals(2, results.size)
        assertTrue(results["STOP"]!!.startsWith("0000 "))
        assertTrue(results["VIDEO OFF"]!!.startsWith("0000 "))
    }

    @Test
    fun testRC5Parsing() {
        val csv = """
            functionname,protocol,device,subdevice,function
            POWER,RC5,0,-1,12
        """.trimIndent()

        val results = IrCsvParser.parseCsvAndGenerateHex(csv)
        assertEquals(1, results.size)
        assertTrue(results["POWER"]!!.startsWith("0000 "))
    }

    @Test
    fun testSamsung20AirConditionerParsing() {
        val csv = """
            functionname,protocol,device,subdevice,function
            SLEEP,Samsung20,1,8,39
            COOL,Samsung20,1,8,60
            POWER,Samsung20,1,8,63
        """.trimIndent()

        val results = IrCsvParser.parseCsvAndGenerateHex(csv)
        assertEquals(3, results.size)
        assertTrue(results.containsKey("SLEEP"))
        assertTrue(results.containsKey("COOL"))
        assertTrue(results.containsKey("POWER"))
        assertTrue(results["POWER"]!!.startsWith("0000 "))
    }

    @Test
    fun testZoomIcons() {
        val zoomInButton = RemoteButtonDBModel(
            offsetX = 0f,
            offsetY = 0f,
            name = "ZOOM+",
            irPattern = com.monuk7735.nope.remote.infrared.patterns.IRPattern(
                com.monuk7735.nope.remote.infrared.patterns.IRPatternType.Intervals,
                38000,
                intArrayOf(100, 100)
            )
        )
        val zoomOutButton = RemoteButtonDBModel(
            offsetX = 0f,
            offsetY = 0f,
            name = "ZOOM OUT",
            irPattern = com.monuk7735.nope.remote.infrared.patterns.IRPattern(
                com.monuk7735.nope.remote.infrared.patterns.IRPatternType.Intervals,
                38000,
                intArrayOf(100, 100)
            )
        )
        val zoomUpButton = RemoteButtonDBModel(
            offsetX = 0f,
            offsetY = 0f,
            name = "ZOOM UP",
            irPattern = com.monuk7735.nope.remote.infrared.patterns.IRPattern(
                com.monuk7735.nope.remote.infrared.patterns.IRPatternType.Intervals,
                38000,
                intArrayOf(100, 100)
            )
        )
        val zoomDownButton = RemoteButtonDBModel(
            offsetX = 0f,
            offsetY = 0f,
            name = "ZOOM DOWN",
            irPattern = com.monuk7735.nope.remote.infrared.patterns.IRPattern(
                com.monuk7735.nope.remote.infrared.patterns.IRPatternType.Intervals,
                38000,
                intArrayOf(100, 100)
            )
        )

        assertNotNull(zoomInButton.getIcon())
        assertNotNull(zoomOutButton.getIcon())
        assertNotNull(zoomUpButton.getIcon())
        assertNotNull(zoomDownButton.getIcon())
        
        fun createBtn(name: String) = RemoteButtonDBModel(
            offsetX = 0f, offsetY = 0f, name = name,
            irPattern = com.monuk7735.nope.remote.infrared.patterns.IRPattern(
                com.monuk7735.nope.remote.infrared.patterns.IRPatternType.Intervals,
                38000, intArrayOf(100, 100)
            )
        )
        
        assertEquals(androidx.compose.material.icons.Icons.Outlined.ZoomIn, createBtn("ZM+").getIcon())
        assertEquals(androidx.compose.material.icons.Icons.Outlined.ZoomOut, createBtn("ZM-").getIcon())
        
        assertEquals(androidx.compose.material.icons.Icons.Outlined.NorthWest, createBtn("UP LEFT").getIcon())
        assertEquals(androidx.compose.material.icons.Icons.Outlined.NorthEast, createBtn("UP RIGHT").getIcon())
        assertEquals(androidx.compose.material.icons.Icons.Outlined.SouthWest, createBtn("DOWN LEFT").getIcon())
        assertEquals(androidx.compose.material.icons.Icons.Outlined.SouthEast, createBtn("DOWN RIGHT").getIcon())
    }
}
