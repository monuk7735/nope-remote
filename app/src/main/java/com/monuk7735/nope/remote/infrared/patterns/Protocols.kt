package com.monuk7735.nope.remote.infrared.patterns

import com.monuk7735.nope.remote.infrared.IrCommandBuilder

interface Protocol {
    fun generate(device: Int, subdevice: Int, function: Int): List<Int>
}

class NECStandard : Protocol {
    companion object {
        private const val FREQUENCY = 38028
        private const val HDR_MARK = 9000
        private const val HDR_SPACE = 4500
        private const val BIT_MARK = 560
        private const val ONE_SPACE = 1690
        private const val ZERO_SPACE = 560
        
        private val SEQUENCE_DEF = IrCommandBuilder.simpleSequence(BIT_MARK, ONE_SPACE, BIT_MARK, ZERO_SPACE)
    }

    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        return IrCommandBuilder(FREQUENCY)
            .pair(HDR_MARK, HDR_SPACE)
            .sequenceLSB(SEQUENCE_DEF, 8, device)
            .sequenceLSB(SEQUENCE_DEF, 8, device.inv())
            .sequenceLSB(SEQUENCE_DEF, 8, function)
            .sequenceLSB(SEQUENCE_DEF, 8, function.inv())
            .mark(BIT_MARK)
            .build()
    }
}

class NECSamsung : Protocol {
    companion object {
        private const val FREQUENCY = 38028
        private const val HDR_MARK = 4500
        private const val HDR_SPACE = 4500
        private const val BIT_MARK = 560
        private const val ONE_SPACE = 1690
        private const val ZERO_SPACE = 560
        
        private val SEQUENCE_DEF = IrCommandBuilder.simpleSequence(BIT_MARK, ONE_SPACE, BIT_MARK, ZERO_SPACE)
    }

    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        return IrCommandBuilder(FREQUENCY)
            .pair(HDR_MARK, HDR_SPACE)
            .sequenceLSB(SEQUENCE_DEF, 8, device)
            .sequenceLSB(SEQUENCE_DEF, 8, device)
            .sequenceLSB(SEQUENCE_DEF, 8, function)
            .sequenceLSB(SEQUENCE_DEF, 8, function.inv())
            .mark(BIT_MARK)
            .build()
    }
}

class NEC48k : Protocol {
    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        return NECStandard().generate(device, subdevice, function)
    }
}

class Nokia32 : Protocol {
    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        val list = mutableListOf<Int>()
        // Header
        list.add(417)
        list.add(278)
        
        val d = (device and 0xFF).toLong()
        val s = (subdevice and 0xFF).toLong()
        val f = (function and 0xFF).toLong()
        val t = 0L
        val x = 0L
        
        val payload = (d shl 24) or (s shl 16) or (t shl 15) or (x shl 8) or f
        
        // 32 bits encoded as 16 symbols of 2 bits each, MSB first
        for (i in 15 downTo 0) {
            val twoBits = (payload shr (i * 2)) and 0b11
            val offTime = when (twoBits) {
                0L -> 278
                1L -> 444
                2L -> 611
                3L -> 778
                else -> 278
            }
            list.add(167)
            list.add(offTime.toInt())
        }
        
        list.add(167)
        return list
    }
}

class NECExtended : Protocol {
    companion object {
        private const val FREQUENCY = 38028
        private const val HDR_MARK = 9000
        private const val HDR_SPACE = 4500
        private const val BIT_MARK = 560
        private const val ONE_SPACE = 1690
        private const val ZERO_SPACE = 560
        
        private val SEQUENCE_DEF = IrCommandBuilder.simpleSequence(BIT_MARK, ONE_SPACE, BIT_MARK, ZERO_SPACE)
    }

    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        return IrCommandBuilder(FREQUENCY)
            .pair(HDR_MARK, HDR_SPACE)
            .sequenceLSB(SEQUENCE_DEF, 8, device)
            .sequenceLSB(SEQUENCE_DEF, 8, subdevice)
            .sequenceLSB(SEQUENCE_DEF, 8, function)
            .sequenceLSB(SEQUENCE_DEF, 8, function.inv())
            .mark(BIT_MARK)
            .build()
    }
}

class Samsung20 : Protocol {
    companion object {
        const val FREQUENCY = 38400
        private const val HDR_MARK = 4512
        private const val HDR_SPACE = 4512
        private const val BIT_MARK = 564
        private const val ONE_SPACE = 1692
        private const val ZERO_SPACE = 564

        private val SEQUENCE_DEF = IrCommandBuilder.simpleSequence(BIT_MARK, ONE_SPACE, BIT_MARK, ZERO_SPACE)
    }

    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        val dev = if (device >= 0) device else 0
        val sub = if (subdevice >= 0) subdevice else 0
        val func = if (function >= 0) function else 0

        return IrCommandBuilder(FREQUENCY)
            .pair(HDR_MARK, HDR_SPACE)
            .sequenceLSB(SEQUENCE_DEF, 6, dev)
            .sequenceLSB(SEQUENCE_DEF, 6, sub)
            .sequenceLSB(SEQUENCE_DEF, 8, func)
            .mark(BIT_MARK)
            .build()
    }
}

class Samsung36 : Protocol {
    companion object {
        const val FREQUENCY = 38000
        private const val HDR_MARK = 4500
        private const val HDR_SPACE = 4500
        private const val BIT_MARK = 500
        private const val ONE_SPACE = 1500
        private const val ZERO_SPACE = 500

        private val SEQUENCE_DEF = IrCommandBuilder.simpleSequence(BIT_MARK, ONE_SPACE, BIT_MARK, ZERO_SPACE)
    }

    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        val dev = if (device >= 0) device else 0
        val sub = if (subdevice >= 0) subdevice else 0
        val func = if (function >= 0) function else 0

        return IrCommandBuilder(FREQUENCY)
            .pair(HDR_MARK, HDR_SPACE)
            .sequenceLSB(SEQUENCE_DEF, 8, dev)
            .sequenceLSB(SEQUENCE_DEF, 8, sub)
            .pair(BIT_MARK, 4500)
            .sequenceLSB(SEQUENCE_DEF, 4, 0)
            .sequenceLSB(SEQUENCE_DEF, 8, func)
            .sequenceLSB(SEQUENCE_DEF, 8, func.inv())
            .mark(BIT_MARK)
            .build()
    }
}

class SonySIRC(private val bits: Int = 12) : Protocol {
    companion object {
        const val FREQUENCY = 40000
        private const val HDR_MARK = 2400
        private const val HDR_SPACE = 600
        private const val ONE_MARK = 1200
        private const val ONE_SPACE = 600
        private const val ZERO_MARK = 600
        private const val ZERO_SPACE = 600
        private const val REPEAT_COUNT = 3
        private const val FRAME_PERIOD = 45000

        private val SEQUENCE_DEF = IrCommandBuilder.simpleSequence(ONE_MARK, ONE_SPACE, ZERO_MARK, ZERO_SPACE)
    }

    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        val builder = IrCommandBuilder(FREQUENCY)
        val dev = if (device >= 0) device else 0
        val sub = if (subdevice >= 0) subdevice else 0
        val func = if (function >= 0) function else 0

        val (devBits, subBits) = when (bits) {
            15 -> Pair(8, 0)
            20 -> Pair(5, 8)
            else -> Pair(5, 0)
        }

        fun bitDuration(value: Int, len: Int): Int {
            var d = 0
            for (i in 0 until len) {
                val isOne = (value and (1 shl i)) != 0
                d += if (isOne) (ONE_MARK + ONE_SPACE) else (ZERO_MARK + ZERO_SPACE)
            }
            return d
        }

        val packetDuration = (HDR_MARK + HDR_SPACE) +
                bitDuration(func, 7) +
                bitDuration(dev, devBits) +
                (if (subBits > 0) bitDuration(sub, subBits) else 0)

        val gap = (FRAME_PERIOD - packetDuration).coerceAtLeast(10000)

        for (rep in 0 until REPEAT_COUNT) {
            builder.pair(HDR_MARK, HDR_SPACE)
                .sequenceLSB(SEQUENCE_DEF, 7, func)
                .sequenceLSB(SEQUENCE_DEF, devBits, dev)
            if (subBits > 0) {
                builder.sequenceLSB(SEQUENCE_DEF, subBits, sub)
            }
            builder.space(gap)
        }

        return builder.build()
    }
}

class RC5 : Protocol {
    companion object {
        const val FREQUENCY = 36000
        private const val HALF_BIT = 889
    }

    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        val builder = IrCommandBuilder(FREQUENCY)
        val dev = (if (device >= 0) device else 0) and 0x1F
        val func = (if (function >= 0) function else 0) and 0x3F
        val toggle = 0

        // Physical transmission begins with the mark phase of start bit 1
        builder.mark(HALF_BIT)

        val remainingBits = mutableListOf<Int>()
        remainingBits.add(1) // Start bit 2
        remainingBits.add(toggle) // Toggle bit
        for (i in 4 downTo 0) {
            remainingBits.add((dev shr i) and 1)
        }
        for (i in 5 downTo 0) {
            remainingBits.add((func shr i) and 1)
        }

        for (bit in remainingBits) {
            if (bit == 1) {
                builder.space(HALF_BIT).mark(HALF_BIT)
            } else {
                builder.mark(HALF_BIT).space(HALF_BIT)
            }
        }
        builder.space(20000)

        return builder.build()
    }
}

class Panasonic : Protocol {
    companion object {
        const val FREQUENCY = 36700
        private const val HDR_MARK = 3456
        private const val HDR_SPACE = 1728
        private const val BIT_MARK = 432
        private const val ONE_SPACE = 1296
        private const val ZERO_SPACE = 432

        private val SEQUENCE_DEF = IrCommandBuilder.simpleSequence(BIT_MARK, ONE_SPACE, BIT_MARK, ZERO_SPACE)
    }

    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        val builder = IrCommandBuilder(FREQUENCY)
        val dev = if (device >= 0) device else 0
        val sub = if (subdevice >= 0) subdevice else 0
        val func = if (function >= 0) function else 0

        val oem1 = 0x02
        val oem2 = 0x20
        val oemXor = (oem1 xor oem2) and 0x0F
        val devByte = dev and 0xFF
        val subByte = sub and 0xFF
        val funcByte = func and 0xFF
        val checksum = (devByte xor subByte xor funcByte) and 0xFF

        builder.pair(HDR_MARK, HDR_SPACE)
            .sequenceLSB(SEQUENCE_DEF, 8, oem1)
            .sequenceLSB(SEQUENCE_DEF, 8, oem2)
            .sequenceLSB(SEQUENCE_DEF, 4, oemXor)
            .sequenceLSB(SEQUENCE_DEF, 8, devByte)
            .sequenceLSB(SEQUENCE_DEF, 8, subByte)
            .sequenceLSB(SEQUENCE_DEF, 8, funcByte)
            .sequenceLSB(SEQUENCE_DEF, 8, checksum)
            .mark(BIT_MARK)
            .space(20000)

        return builder.build()
    }
}

class JVC : Protocol {
    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        val def = object : IrCommandBuilder.SequenceDefinition {
            override fun one(builder: IrCommandBuilder, index: Int) { builder.mark(527).space(1581) }
            override fun zero(builder: IrCommandBuilder, index: Int) { builder.mark(527).space(527) }
        }
        return IrCommandBuilder(38000)
            .mark(8400).space(4200)
            .sequenceLSB(def, 8, device)
            .sequenceLSB(def, 8, function)
            .mark(527).space(15000) // Lead out
            .build()
    }
}

class Denon : Protocol {
    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        val def = object : IrCommandBuilder.SequenceDefinition {
            override fun one(builder: IrCommandBuilder, index: Int) { builder.mark(275).space(1900) }
            override fun zero(builder: IrCommandBuilder, index: Int) { builder.mark(275).space(780) }
        }
        val f = function.toLong() and 0xFF
        val invF = f.inv() and 0xFF
        return IrCommandBuilder(38000)
            .sequence(def, 5, device.toLong())
            .sequence(def, 10, f shl 2)
            .mark(275).space(45000)
            // Send inverted
            .sequence(def, 5, device.toLong())
            .sequence(def, 10, (invF shl 2) or 0b11)
            .mark(275).space(45000)
            .build()
    }
}

class Mitsubishi : Protocol {
    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        val def = object : IrCommandBuilder.SequenceDefinition {
            override fun one(builder: IrCommandBuilder, index: Int) { builder.mark(900).space(300) }
            override fun zero(builder: IrCommandBuilder, index: Int) { builder.mark(300).space(900) }
        }
        return IrCommandBuilder(38000)
            .mark(4800).space(2400)
            .sequenceLSB(def, 8, device)
            .sequenceLSB(def, 8, function)
            .mark(300).space(24000)
            .build()
    }
}

class RC6Mode0 : Protocol {
    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        val t = 444
        val def = object : IrCommandBuilder.SequenceDefinition {
            override fun one(builder: IrCommandBuilder, index: Int) { builder.mark(t).space(t) }
            override fun zero(builder: IrCommandBuilder, index: Int) { builder.space(t).mark(t) }
        }
        return IrCommandBuilder(36000)
            .mark(2666).space(889) // Header
            .mark(t).space(t) // Start bit 1
            // Mode 000
            .space(t).mark(t).space(t).mark(t).space(t).mark(t)
            // Toggle bit 0 (double time)
            .space(t*2).mark(t*2)
            // Device 8, Function 8 MSB
            .sequence(def, 8, device.toLong())
            .sequence(def, 8, function.toLong())
            .space(15000)
            .build()
    }
}
