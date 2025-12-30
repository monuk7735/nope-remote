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
