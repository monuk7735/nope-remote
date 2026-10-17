package com.monuk7735.nope.remote.androidtv.protocol

import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream

object VarintFraming {

    fun writeVarint(value: Int, outputStream: OutputStream) {
        var v = value
        while (true) {
            if ((v and 0x7F.inv()) == 0) {
                outputStream.write(v)
                return
            } else {
                outputStream.write((v and 0x7F) or 0x80)
                v = v ushr 7
            }
        }
    }

    fun readVarint(inputStream: InputStream): Int {
        var result = 0
        var shift = 0
        while (true) {
            val b = inputStream.read()
            if (b < 0) {
                throw EOFException("End of stream reached while reading varint")
            }
            result = result or ((b and 0x7F) shl shift)
            if ((b and 0x80) == 0) {
                return result
            }
            shift += 7
            if (shift >= 35) {
                throw IllegalArgumentException("Malformed varint: exceeds 5 bytes")
            }
        }
    }

    fun sendFramedMessage(payload: ByteArray, outputStream: OutputStream) {
        synchronized(outputStream) {
            writeVarint(payload.size, outputStream)
            outputStream.write(payload)
            outputStream.flush()
        }
    }

    fun readFramedMessage(inputStream: InputStream): ByteArray {
        val length = readVarint(inputStream)
        if (length < 0) {
            throw EOFException("Invalid payload length: $length")
        }
        val buffer = ByteArray(length)
        var totalRead = 0
        while (totalRead < length) {
            val read = inputStream.read(buffer, totalRead, length - totalRead)
            if (read < 0) {
                throw EOFException(
                    "End of stream before reading full payload of $length bytes",
                )
            }
            totalRead += read
        }
        return buffer
    }
}
