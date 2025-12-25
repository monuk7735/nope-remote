package com.monuk7735.nope.remote.infrared.patterns

class IRPatternDecoder(hexData: String) {

    var irPattern: IRPattern

    init {
        val dec = mutableListOf<Int>()
        // Handle optional "0x" prefix and split by whitespace or fixed width if no spaces
        var cleanHex = hexData.replace("0x", "").replace("\\s+".toRegex(), "")

        cleanHex.chunked(4).forEach {
            dec.add(it.toInt(16))
        }

        dec.removeAt(0)
        var frequency = dec.removeAt(0)
        frequency = (1000000.0 / (frequency * 0.241246)).toInt()
        dec.removeAt(0)
        dec.removeAt(0)

        irPattern = IRPattern(IRPatternType.Cycles, frequency, dec.toIntArray())
    }

}