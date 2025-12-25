package com.monuk7735.nope.remote.infrared.patterns

abstract class Protocol {
    abstract fun generate(device: Int, subdevice: Int, function: Int): List<Int>

    protected fun lsbToBits(value: Int, length: Int): List<Int> {
        val bits = mutableListOf<Int>()
        for (i in 0 until length) {
            bits.add((value shr i) and 1)
        }
        return bits
    }
}

open class BaseNEC(
    private val leaderMark: Int,
    private val leaderSpace: Int
) : Protocol() {

    override fun generate(device: Int, subdevice: Int, function: Int): List<Int> {
        val bits = mutableListOf<Int>()

        // Address
        bits.addAll(lsbToBits(device, 8))

        if (subdevice == -1) {
            // Standard NEC (Address, ~Address)
            bits.addAll(lsbToBits(device.inv() and 0xFF, 8))
        } else {
            // Extended NEC (Address, Subdevice)
            bits.addAll(lsbToBits(subdevice, 8))
        }

        // Command
        bits.addAll(lsbToBits(function, 8))
        bits.addAll(lsbToBits(function.inv() and 0xFF, 8))

        val timings = mutableListOf<Int>()
        // Leader
        timings.add(leaderMark)
        timings.add(leaderSpace)

        // Data
        for (b in bits) {
            if (b == 1) {
                timings.add(560)
                timings.add(1690)
            } else {
                timings.add(560)
                timings.add(560)
            }
        }
        // Stop
        timings.add(560)
        return timings
    }
}

class NECStandard : BaseNEC(9000, 4500)
class NECSamsung : BaseNEC(4500, 4500)
class NEC48k : BaseNEC(9000, 4500)
