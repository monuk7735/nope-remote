package com.monuk7735.nope.remote.infrared

import com.monuk7735.nope.remote.infrared.patterns.*
import kotlin.math.roundToInt

object IrCsvParser {
    fun parseCsvAndGenerateHex(csvContent: String, functionQuery: String? = null): Map<String, String> {
        val lines = csvContent.lines()
        if (lines.isEmpty()) {
            return emptyMap()
        }

        val header = lines[0].split(",").map { it.trim() }

        val funcIndex = header.indexOfFirst { it.equals("functionname", ignoreCase = true) || it.equals("function", ignoreCase = true) }
        val protocolIndex = header.indexOfFirst { it.equals("protocol", ignoreCase = true) }
        val deviceIndex = header.indexOfFirst { it.equals("device", ignoreCase = true) }
        val subdeviceIndex = header.indexOfFirst { it.equals("subdevice", ignoreCase = true) }
        val functionCodeIndex = header.indexOfLast { it.equals("function", ignoreCase = true) }

        if (funcIndex == -1 || protocolIndex == -1 || deviceIndex == -1 || functionCodeIndex == -1) {
            return emptyMap()
        }

        val results = mutableMapOf<String, String>()

        for (i in 1 until lines.size) {
            val line = lines[i]
            if (line.isBlank()) continue
            val row = line.split(",").map { it.trim() }

            if (row.size != header.size) {
                continue
            }

            val funcName = row[funcIndex]
            if (functionQuery != null && !funcName.equals(functionQuery, ignoreCase = true)) {
                continue
            }

            val protocolName = row[protocolIndex]
            val device = row[deviceIndex].toIntOrNull() ?: 0
            val subdevice = row[subdeviceIndex].toIntOrNull() ?: 0
            val function = row[functionCodeIndex].toIntOrNull() ?: 0

            val (generator: Protocol?, frequency: Int) = when {
                protocolName.startsWith("48-NEC", ignoreCase = true) -> Pair(NEC48k(), 48000)
                protocolName.equals("NECx2", ignoreCase = true) -> Pair(NECSamsung(), 38000)
                protocolName.startsWith("NEC", ignoreCase = true) -> Pair(NECStandard(), 38000)
                protocolName.equals("Sony12", ignoreCase = true) -> Pair(SonySIRC(12), 40000)
                protocolName.equals("Sony15", ignoreCase = true) -> Pair(SonySIRC(15), 40000)
                protocolName.equals("Sony20", ignoreCase = true) -> Pair(SonySIRC(20), 40000)
                protocolName.startsWith("Sony", ignoreCase = true) || protocolName.startsWith("SIRC", ignoreCase = true) -> Pair(SonySIRC(12), 40000)
                protocolName.startsWith("RC5", ignoreCase = true) || protocolName.startsWith("RC-5", ignoreCase = true) -> Pair(RC5(), 36000)
                protocolName.startsWith("Panasonic", ignoreCase = true) || protocolName.startsWith("Kaseikyo", ignoreCase = true) -> Pair(Panasonic(), 36700)
                else -> Pair(null, 38000)
            }

            if (generator != null) {
                val timings = generator.generate(device, subdevice, function)
                val hex = encodeToProntoHex(frequency, timings)
                results[funcName] = hex
            }
        }
        return results
    }

    fun encodeToProntoHex(frequency: Int, timings: List<Int>): String {
        val sb = StringBuilder()
        sb.append("0000 ")

        val freqCode = (1000000.0 / (frequency * 0.241246)).roundToInt()
        sb.append(String.format("%04X ", freqCode))

        val period = 1000000.0 / frequency
        val burstPairs = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until timings.size - 1 step 2) {
             val mark = (timings[i] / period).roundToInt()
             val space = (timings[i+1] / period).roundToInt()
             burstPairs.add(Pair(mark, space))
        }

        var pairsCount = burstPairs.size
        val lastIsMark = timings.size % 2 != 0
        if (lastIsMark) {
            pairsCount++
        }

        sb.append(String.format("%04X ", pairsCount))
        sb.append("0000 ")

        for (pair in burstPairs) {
            sb.append(String.format("%04X %04X ", pair.first, pair.second))
        }

        if (lastIsMark) {
             val mark = (timings.last() / period).roundToInt()
             val space = 0x06C3
             sb.append(String.format("%04X %04X ", mark, space)) 
        }

        return sb.toString().trim()
    }
}
