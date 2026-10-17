package com.monuk7735.nope.remote

import com.monuk7735.nope.remote.composables.ProtocolOption
import com.monuk7735.nope.remote.composables.generateFrequencyPattern
import com.monuk7735.nope.remote.composables.generateProtocolPattern
import com.monuk7735.nope.remote.infrared.patterns.IRPatternType
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomRemoteTest {

    @Test
    fun testCustomRemoteModelDefaults() {
        val customRemote = RemoteDataDBModel(
            id = 1,
            name = "Bedroom Custom TV",
            brand = "Custom",
            type = "TV",
            added = Date(),
            onScreenRemoteButtonDBS = emptyList(),
            offScreenRemoteButtonDBS = emptyList(),
            isCustom = true,
        )

        assertTrue(customRemote.isCustom)
        assertTrue(customRemote.onScreenRemoteButtonDBS.isEmpty())
        assertTrue(customRemote.offScreenRemoteButtonDBS.isEmpty())
        assertNull(customRemote.getByName("POWER"))
    }

    @Test
    fun testProtocolPatternGenerationForAllProtocols() {
        ProtocolOption.values().forEach { protocol ->
            val pattern = generateProtocolPattern(
                protocol = protocol,
                device = 1,
                subdevice = 0,
                function = 18,
            )

            assertNotNull("Pattern should not be null for $protocol", pattern)
            assertEquals(
                "Pattern type should be Intervals",
                IRPatternType.Intervals,
                pattern.irPatternType,
            )
            assertEquals(
                "Frequency should match protocol default",
                protocol.defaultFrequency,
                pattern.frequency,
            )
            assertTrue("Intervals must not be empty for $protocol", pattern.intervals.isNotEmpty())
            assertTrue(
                "All pulse intervals should be positive for $protocol",
                pattern.intervals.all {
                    it >
                        0
                },
            )
        }
    }

    @Test
    fun testFrequencyPatternGenerationIntervals() {
        val rawInput = "9000, 4500, 560, 1690, 560, 560, 560, 1690"
        val pattern = generateFrequencyPattern("38000", rawInput)

        assertEquals(38000, pattern.frequency)
        assertEquals(IRPatternType.Intervals, pattern.irPatternType)
        assertEquals(8, pattern.intervals.size)
        assertEquals(9000, pattern.intervals[0])
        assertEquals(4500, pattern.intervals[1])
        assertEquals(560, pattern.intervals[2])
        assertEquals(1690, pattern.intervals[3])
    }

    @Test
    fun testFrequencyPatternGenerationProntoHex() {
        val prontoHex = "0000 006D 0002 0000 000A 000A 000A 000A"
        val pattern = generateFrequencyPattern("38000", prontoHex)

        assertNotNull(pattern)
        assertTrue(pattern.intervals.isNotEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testFrequencyPatternEmptyThrows() {
        generateFrequencyPattern("38000", "   ")
    }
}
