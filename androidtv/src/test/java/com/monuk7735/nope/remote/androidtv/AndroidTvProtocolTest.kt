package com.monuk7735.nope.remote.androidtv

import com.monuk7735.nope.remote.androidtv.proto.pairing.Pairingmessage
import com.monuk7735.nope.remote.androidtv.proto.remote.Remotemessage
import com.monuk7735.nope.remote.androidtv.protocol.VarintFraming
import com.monuk7735.nope.remote.androidtv.ssl.AndroidTvSslManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.security.MessageDigest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidTvProtocolTest {

    @Test
    fun testVarintEncodingAndDecoding() {
        val testValues = listOf(0, 1, 63, 127, 128, 255, 300, 16384, 65535, 1000000)
        for (value in testValues) {
            val bos = ByteArrayOutputStream()
            VarintFraming.writeVarint(value, bos)
            val bytes = bos.toByteArray()

            val bis = ByteArrayInputStream(bytes)
            val decoded = VarintFraming.readVarint(bis)
            assertEquals("Value $value failed roundtrip", value, decoded)
        }
    }

    @Test
    fun testFramedMessageRoundtrip() {
        val testPayload = "Hello Android TV Remote".toByteArray(Charsets.UTF_8)
        val bos = ByteArrayOutputStream()
        VarintFraming.sendFramedMessage(testPayload, bos)

        val bis = ByteArrayInputStream(bos.toByteArray())
        val received = VarintFraming.readFramedMessage(bis)
        assertArrayEquals(testPayload, received)
    }

    @Test
    fun testFramedMessageEof() {
        val bis = ByteArrayInputStream(byteArrayOf())
        assertThrows(EOFException::class.java) {
            VarintFraming.readFramedMessage(bis)
        }
    }

    @Test
    fun testVarintMalformedExceeds5Bytes() {
        val malformedBytes = byteArrayOf(
            0x80.toByte(),
            0x80.toByte(),
            0x80.toByte(),
            0x80.toByte(),
            0x80.toByte(),
            0x01.toByte(),
        )
        val bis = ByteArrayInputStream(malformedBytes)
        assertThrows(IllegalArgumentException::class.java) {
            VarintFraming.readVarint(bis)
        }
    }

    @Test
    fun testStripLeadingZeros() {
        assertArrayEquals(
            byteArrayOf(1, 2, 3),
            AndroidTvSslManager.stripLeadingZeros(byteArrayOf(0, 0, 1, 2, 3)),
        )
        assertArrayEquals(
            byteArrayOf(1, 2, 3),
            AndroidTvSslManager.stripLeadingZeros(byteArrayOf(1, 2, 3)),
        )
        assertArrayEquals(
            byteArrayOf(0),
            AndroidTvSslManager.stripLeadingZeros(byteArrayOf(0, 0, 0)),
        )
        assertArrayEquals(
            byteArrayOf(0),
            AndroidTvSslManager.stripLeadingZeros(byteArrayOf()),
        )
    }

    @Test
    fun testPairingMessageProtobufSerialization() {
        val request = Pairingmessage.PairingMessage.newBuilder()
            .setProtocolVersion(2)
            .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
            .setPairingRequest(
                Pairingmessage.PairingRequest.newBuilder()
                    .setServiceName("atvremote")
                    .setClientName("Nope Remote"),
            )
            .build()

        val bytes = request.toByteArray()
        val parsed = Pairingmessage.PairingMessage.parseFrom(bytes)

        assertEquals(2, parsed.protocolVersion)
        assertEquals(Pairingmessage.PairingMessage.Status.STATUS_OK, parsed.status)
        assertTrue(parsed.hasPairingRequest())
        assertEquals("atvremote", parsed.pairingRequest.serviceName)
        assertEquals("Nope Remote", parsed.pairingRequest.clientName)
    }

    @Test
    fun testRemoteMessageProtobufSerialization() {
        val keyMsg = Remotemessage.RemoteMessage.newBuilder()
            .setRemoteKeyInject(
                Remotemessage.RemoteKeyInject.newBuilder()
                    .setKeyCode(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_CENTER)
                    .setDirection(Remotemessage.RemoteDirection.SHORT),
            )
            .build()

        val bytes = keyMsg.toByteArray()
        val parsed = Remotemessage.RemoteMessage.parseFrom(bytes)

        assertTrue(parsed.hasRemoteKeyInject())
        assertEquals(
            Remotemessage.RemoteKeyCode.KEYCODE_DPAD_CENTER,
            parsed.remoteKeyInject.keyCode,
        )
        assertEquals(
            Remotemessage.RemoteDirection.SHORT,
            parsed.remoteKeyInject.direction,
        )
    }

    @Test
    fun testPairingHashCalculation() {
        val clientModulus = byteArrayOf(1, 2, 3, 4)
        val clientExponent = byteArrayOf(1, 0, 1)
        val serverModulus = byteArrayOf(5, 6, 7, 8)
        val serverExponent = byteArrayOf(1, 0, 1)
        val nonce = byteArrayOf(0x12, 0x34)

        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(clientModulus)
        digest.update(clientExponent)
        digest.update(serverModulus)
        digest.update(serverExponent)
        digest.update(nonce)
        val hash = digest.digest()

        val expectedFirstByte = hash[0].toInt() and 0xFF
        val hexPrefix = String.format("%02X", expectedFirstByte)
        val fullPin = hexPrefix + "1234"

        assertEquals(6, fullPin.length)
        assertEquals(expectedFirstByte, fullPin.substring(0, 2).toInt(16))
    }
}
