package com.monuk7735.nope.remote.infrared

import kotlin.collections.ArrayList

class IrCommandBuilder(val frequency: Int) {
    private val buffer = ArrayList<Int>()
    private var lastMark: Boolean? = null

    fun mark(interval: Int): IrCommandBuilder {
        return appendSymbol(true, interval)
    }

    fun space(interval: Int): IrCommandBuilder {
        return appendSymbol(false, interval)
    }

    fun pair(on: Int, off: Int): IrCommandBuilder {
        return mark(on).space(off)
    }

    fun reversePair(off: Int, on: Int): IrCommandBuilder {
        return space(off).mark(on)
    }

    fun delay(ms: Int): IrCommandBuilder {
        return space(ms * frequency / 1000)
    }

    private fun appendSymbol(mark: Boolean, interval: Int): IrCommandBuilder {
        if (lastMark == null || lastMark != mark) {
            buffer.add(interval)
            lastMark = mark
        } else {
            val lastIndex = buffer.size - 1
            buffer[lastIndex] = buffer[lastIndex] + interval
        }
        return this
    }

    interface SequenceDefinition {
        fun one(builder: IrCommandBuilder, index: Int)
        fun zero(builder: IrCommandBuilder, index: Int)
    }

    fun sequence(definition: SequenceDefinition, length: Int, data: Int): IrCommandBuilder {
        return sequence(definition, length, data.toLong())
    }

    fun sequence(definition: SequenceDefinition, length: Int, data: Long): IrCommandBuilder {
        return sequenceMSB(definition, length, data)
    }

    private fun sequenceMSB(definition: SequenceDefinition, length: Int, data: Long): IrCommandBuilder {
        var d = data
        val topBit = 1L shl 63
        return this 
    }
    
    fun sequence(definition: SequenceDefinition, topBit: Long, length: Int, data: Long): IrCommandBuilder {
        var d = data
        for (i in 0 until length) {
            if ((d and topBit) != 0L) {
                definition.one(this, i)
            } else {
                definition.zero(this, i)
            }
            d = d shl 1
        }
        return this
    }

    fun sequenceLSB(definition: SequenceDefinition, length: Int, data: Int): IrCommandBuilder {
        var d = data
        for (i in 0 until length) {
            if ((d and 1) != 0) {
                definition.one(this, i)
            } else {
                definition.zero(this, i)
            }
            d = d shr 1
        }
        return this
    }
    
    companion object {
        fun simpleSequence(oneMark: Int, oneSpace: Int, zeroMark: Int, zeroSpace: Int): SequenceDefinition {
            return object : SequenceDefinition {
                override fun one(builder: IrCommandBuilder, index: Int) {
                    builder.pair(oneMark, oneSpace)
                }

                override fun zero(builder: IrCommandBuilder, index: Int) {
                    builder.pair(zeroMark, zeroSpace)
                }
            }
        }
    }

    fun build(): List<Int> {
        return buffer
    }
}
