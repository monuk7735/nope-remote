package com.monuk7735.nope.remote.androidtv.pairing

import com.google.protobuf.ByteString
import com.monuk7735.nope.remote.androidtv.proto.pairing.Pairingmessage
import com.monuk7735.nope.remote.androidtv.protocol.VarintFraming
import com.monuk7735.nope.remote.androidtv.ssl.AndroidTvSslManager
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import javax.net.ssl.SSLSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidTvPairingManager(private val sslManager: AndroidTvSslManager) {

    class ActivePairingSession internal constructor(
        private val socket: SSLSocket,
        private val inputStream: InputStream,
        private val outputStream: OutputStream,
        private val sslManager: AndroidTvSslManager,
    ) : AutoCloseable {

        fun submitPin(pin: String): Boolean {
            val cleanPin = pin.trim().uppercase()
            if (cleanPin.length != 6) {
                throw IllegalArgumentException("PIN must be exactly 6 hexadecimal characters")
            }
            if (!cleanPin.matches(Regex("^[0-9A-F]{6}$"))) {
                throw IllegalArgumentException("PIN must be hexadecimal (0-9, A-F)")
            }

            val peerCerts = socket.session.peerCertificates
            if (peerCerts.isEmpty() || peerCerts[0] !is X509Certificate) {
                throw IllegalStateException("No valid server certificate found")
            }
            val serverCert = peerCerts[0] as X509Certificate
            val serverPubKey = serverCert.publicKey as? RSAPublicKey
                ?: throw IllegalStateException("Server public key is not RSA")
            val clientPubKey = sslManager.getClientPublicKey()

            val clientModulus = AndroidTvSslManager.stripLeadingZeros(
                clientPubKey.modulus.abs().toByteArray(),
            )
            val clientExponent = AndroidTvSslManager.stripLeadingZeros(
                clientPubKey.publicExponent.abs().toByteArray(),
            )
            val serverModulus = AndroidTvSslManager.stripLeadingZeros(
                serverPubKey.modulus.abs().toByteArray(),
            )
            val serverExponent = AndroidTvSslManager.stripLeadingZeros(
                serverPubKey.publicExponent.abs().toByteArray(),
            )

            val nonce = hexToBytes(cleanPin.substring(2))

            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(clientModulus)
            digest.update(clientExponent)
            digest.update(serverModulus)
            digest.update(serverExponent)
            digest.update(nonce)
            val hash = digest.digest()

            val expectedPrefix = cleanPin.substring(0, 2).toInt(16)
            val actualPrefix = hash[0].toInt() and 0xFF
            if (actualPrefix != expectedPrefix) {
                throw IllegalArgumentException("Invalid PIN: hash checksum does not match")
            }

            val secretMsg = Pairingmessage.PairingMessage.newBuilder()
                .setProtocolVersion(2)
                .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
                .setPairingSecret(
                    Pairingmessage.PairingSecret.newBuilder()
                        .setSecret(ByteString.copyFrom(hash)),
                )
                .build()

            VarintFraming.sendFramedMessage(secretMsg.toByteArray(), outputStream)

            val ackBytes = VarintFraming.readFramedMessage(inputStream)
            val ackMsg = Pairingmessage.PairingMessage.parseFrom(ackBytes)
            if (ackMsg.status != Pairingmessage.PairingMessage.Status.STATUS_OK) {
                throw IllegalStateException("Pairing was rejected by TV: status ${ackMsg.status}")
            }
            return true
        }

        override fun close() {
            try {
                socket.close()
            } catch (_: Exception) {}
        }

        private fun hexToBytes(hex: String): ByteArray {
            val result = ByteArray(hex.length / 2)
            for (i in result.indices) {
                val index = i * 2
                result[i] = hex.substring(index, index + 2).toInt(16).toByte()
            }
            return result
        }
    }

    suspend fun startPairing(host: String, port: Int = 6467): ActivePairingSession =
        withContext(Dispatchers.IO) {
            val sslContext = sslManager.createSslContext()
            val socket = sslContext.socketFactory.createSocket() as SSLSocket
            socket.connect(InetSocketAddress(host, port), 8000)
            socket.soTimeout = 30000
            socket.startHandshake()

            val inputStream = socket.inputStream
            val outputStream = socket.outputStream

            // 1. Send PairingRequest
            val pairingRequest = Pairingmessage.PairingMessage.newBuilder()
                .setProtocolVersion(2)
                .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
                .setPairingRequest(
                    Pairingmessage.PairingRequest.newBuilder()
                        .setServiceName("atvremote")
                        .setClientName("Nope Remote"),
                )
                .build()
            VarintFraming.sendFramedMessage(pairingRequest.toByteArray(), outputStream)

            // Read PairingRequestAck
            val reqAckBytes = VarintFraming.readFramedMessage(inputStream)
            val reqAckMsg = Pairingmessage.PairingMessage.parseFrom(reqAckBytes)
            if (reqAckMsg.status != Pairingmessage.PairingMessage.Status.STATUS_OK) {
                socket.close()
                throw IllegalStateException("Pairing request failed: ${reqAckMsg.status}")
            }

            // 2. Send PairingOption
            val encoding = Pairingmessage.PairingEncoding.newBuilder()
                .setType(Pairingmessage.PairingEncoding.EncodingType.ENCODING_TYPE_HEXADECIMAL)
                .setSymbolLength(6)
                .build()
            val pairingOption = Pairingmessage.PairingMessage.newBuilder()
                .setProtocolVersion(2)
                .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
                .setPairingOption(
                    Pairingmessage.PairingOption.newBuilder()
                        .setPreferredRole(Pairingmessage.RoleType.ROLE_TYPE_INPUT)
                        .addInputEncodings(encoding),
                )
                .build()
            VarintFraming.sendFramedMessage(pairingOption.toByteArray(), outputStream)

            // Read Option response
            val optAckBytes = VarintFraming.readFramedMessage(inputStream)
            val optAckMsg = Pairingmessage.PairingMessage.parseFrom(optAckBytes)
            if (optAckMsg.status != Pairingmessage.PairingMessage.Status.STATUS_OK) {
                socket.close()
                throw IllegalStateException("Pairing options failed: ${optAckMsg.status}")
            }

            // 3. Send Configuration
            val configMsg = Pairingmessage.PairingMessage.newBuilder()
                .setProtocolVersion(2)
                .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
                .setPairingConfiguration(
                    Pairingmessage.PairingConfiguration.newBuilder()
                        .setClientRole(Pairingmessage.RoleType.ROLE_TYPE_INPUT)
                        .setEncoding(encoding),
                )
                .build()
            VarintFraming.sendFramedMessage(configMsg.toByteArray(), outputStream)

            // Read ConfigurationAck
            val cfgAckBytes = VarintFraming.readFramedMessage(inputStream)
            val cfgAckMsg = Pairingmessage.PairingMessage.parseFrom(cfgAckBytes)
            if (cfgAckMsg.status != Pairingmessage.PairingMessage.Status.STATUS_OK) {
                socket.close()
                throw IllegalStateException("Pairing configuration failed: ${cfgAckMsg.status}")
            }

            // Now TV is displaying the PIN code
            ActivePairingSession(socket, inputStream, outputStream, sslManager)
        }
}
