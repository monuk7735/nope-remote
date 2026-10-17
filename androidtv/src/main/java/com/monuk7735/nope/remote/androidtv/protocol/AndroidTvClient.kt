package com.monuk7735.nope.remote.androidtv.protocol

import android.util.Log
import com.monuk7735.nope.remote.androidtv.proto.remote.Remotemessage
import com.monuk7735.nope.remote.androidtv.ssl.AndroidTvSslManager
import java.io.OutputStream
import java.net.InetSocketAddress
import javax.net.ssl.SSLSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AndroidTvClient(private val sslManager: AndroidTvSslManager) {

    companion object {
        private const val TAG = "AndroidTvClient"
    }

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        object NeedsPairing : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    data class VolumeState(val level: Int = 0, val max: Int = 100, val isMuted: Boolean = false)

    private val _connectionState =
        MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _currentApp = MutableStateFlow<String?>(null)
    val currentApp: StateFlow<String?> = _currentApp.asStateFlow()

    private val _volumeState = MutableStateFlow(VolumeState())
    val volumeState: StateFlow<VolumeState> = _volumeState.asStateFlow()

    private var socket: SSLSocket? = null
    private var outputStream: OutputStream? = null
    private var readJob: Job? = null
    private val clientScope = CoroutineScope(Dispatchers.IO + Job())

    @Volatile
    private var imeCounter: Int = 0

    @Volatile
    private var fieldCounter: Int = 0

    fun connect(host: String, port: Int = 6466) {
        if (_connectionState.value is ConnectionState.Connected ||
            _connectionState.value is ConnectionState.Connecting
        ) {
            disconnect()
        }

        _connectionState.value = ConnectionState.Connecting

        readJob = clientScope.launch {
            try {
                val sslContext = sslManager.createSslContext()
                val sslSocket = sslContext.socketFactory.createSocket() as SSLSocket
                sslSocket.connect(InetSocketAddress(host, port), 8000)
                sslSocket.soTimeout = 20000
                sslSocket.startHandshake()

                socket = sslSocket
                val input = sslSocket.inputStream
                val output = sslSocket.outputStream
                outputStream = output

                Log.d(TAG, "Connected and handshake complete for $host:$port")

                // Read loop and handshake
                while (isActive) {
                    val messageBytes = try {
                        VarintFraming.readFramedMessage(input)
                    } catch (e: Exception) {
                        if (isActive) {
                            Log.w(TAG, "Connection lost during read", e)
                            if (_connectionState.value !is ConnectionState.Connected) {
                                _connectionState.value = ConnectionState.NeedsPairing
                            } else {
                                _connectionState.value =
                                    ConnectionState.Error("Connection lost: ${e.message}")
                            }
                        }
                        break
                    }

                    val msg = Remotemessage.RemoteMessage.parseFrom(messageBytes)
                    Log.d(
                        TAG,
                        "Received msg: cfg=${msg.hasRemoteConfigure()} " +
                            "active=${msg.hasRemoteSetActive()} start=${msg.hasRemoteStart()} " +
                            "ping=${msg.hasRemotePingRequest()}",
                    )
                    handleIncomingMessage(msg, output)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed for $host:$port", e)
                val isPairingIssue = e is javax.net.ssl.SSLException ||
                    e.cause is javax.net.ssl.SSLException ||
                    e is java.io.EOFException ||
                    (
                        e is java.net.SocketException &&
                            (
                                e.message?.contains("reset", ignoreCase = true) == true ||
                                    e.message?.contains("closed", ignoreCase = true) == true
                                )
                        )
                if (isPairingIssue) {
                    _connectionState.value = ConnectionState.NeedsPairing
                } else {
                    _connectionState.value =
                        ConnectionState.Error("Failed to connect: ${e.message}")
                }
            } finally {
                disconnectInternal()
            }
        }
    }

    private fun handleIncomingMessage(msg: Remotemessage.RemoteMessage, output: OutputStream) {
        if (msg.hasRemoteConfigure()) {
            val reply = Remotemessage.RemoteMessage.newBuilder()
                .setRemoteConfigure(
                    Remotemessage.RemoteConfigure.newBuilder()
                        .setCode1(622) // PING | KEY | IME | POWER | VOLUME | APP_LINK
                        .setDeviceInfo(
                            Remotemessage.RemoteDeviceInfo.newBuilder()
                                .setModel("NopeRemote")
                                .setVendor("Nope")
                                .setUnknown1(1)
                                .setUnknown2("1")
                                .setPackageName("atvremote")
                                .setAppVersion("1.0.0"),
                        ),
                )
                .build()
            VarintFraming.sendFramedMessage(reply.toByteArray(), output)
            _connectionState.value = ConnectionState.Connected
        } else if (msg.hasRemoteSetActive()) {
            val reply = Remotemessage.RemoteMessage.newBuilder()
                .setRemoteSetActive(
                    Remotemessage.RemoteSetActive.newBuilder().setActive(622),
                )
                .build()
            VarintFraming.sendFramedMessage(reply.toByteArray(), output)
            _connectionState.value = ConnectionState.Connected
        } else if (msg.hasRemoteStart()) {
            _connectionState.value = ConnectionState.Connected
        } else if (msg.hasRemotePingRequest()) {
            val reply = Remotemessage.RemoteMessage.newBuilder()
                .setRemotePingResponse(
                    Remotemessage.RemotePingResponse.newBuilder()
                        .setVal1(msg.remotePingRequest.val1),
                )
                .build()
            VarintFraming.sendFramedMessage(reply.toByteArray(), output)
        } else if (msg.hasRemoteImeBatchEdit()) {
            imeCounter = msg.remoteImeBatchEdit.imeCounter
            fieldCounter = msg.remoteImeBatchEdit.fieldCounter
        } else if (msg.hasRemoteImeKeyInject()) {
            if (msg.remoteImeKeyInject.hasAppInfo()) {
                _currentApp.value = msg.remoteImeKeyInject.appInfo.appPackage
            }
        } else if (msg.hasRemoteSetVolumeLevel()) {
            val vol = msg.remoteSetVolumeLevel
            _volumeState.value = VolumeState(
                level = vol.volumeLevel,
                max = vol.volumeMax,
                isMuted = vol.volumeMuted,
            )
        }
    }

    fun sendKey(
        keyCode: Remotemessage.RemoteKeyCode,
        direction: Remotemessage.RemoteDirection = Remotemessage.RemoteDirection.SHORT,
    ) {
        val out = outputStream ?: return
        clientScope.launch {
            try {
                val keyMsg = Remotemessage.RemoteMessage.newBuilder()
                    .setRemoteKeyInject(
                        Remotemessage.RemoteKeyInject.newBuilder()
                            .setKeyCode(keyCode)
                            .setDirection(direction),
                    )
                    .build()
                VarintFraming.sendFramedMessage(keyMsg.toByteArray(), out)
            } catch (_: Exception) {}
        }
    }

    fun sendText(text: String) {
        val out = outputStream ?: return
        if (text.isEmpty()) return
        clientScope.launch {
            try {
                val cursor = text.length
                val textFieldStatus = Remotemessage.RemoteTextFieldStatus.newBuilder()
                    .setCounterField(fieldCounter)
                    .setValue(text)
                    .setStart(cursor)
                    .setEnd(cursor)
                    .build()

                val editInfo = Remotemessage.RemoteEditInfo.newBuilder()
                    .setInsert(1)
                    .setTextFieldStatus(textFieldStatus)
                    .build()

                val batchEdit = Remotemessage.RemoteImeBatchEdit.newBuilder()
                    .setImeCounter(imeCounter)
                    .setFieldCounter(fieldCounter)
                    .addEditInfo(editInfo)
                    .build()

                val msg = Remotemessage.RemoteMessage.newBuilder()
                    .setRemoteImeBatchEdit(batchEdit)
                    .build()

                VarintFraming.sendFramedMessage(msg.toByteArray(), out)
            } catch (_: Exception) {}
        }
    }

    fun sendEnter() {
        sendKey(Remotemessage.RemoteKeyCode.KEYCODE_DPAD_CENTER)
    }

    fun disconnect() {
        readJob?.cancel()
        readJob = null
        disconnectInternal()
    }

    private fun disconnectInternal() {
        try {
            socket?.session?.invalidate()
            socket?.close()
        } catch (_: Exception) {}
        socket = null
        outputStream = null
        if (_connectionState.value !is ConnectionState.Error &&
            _connectionState.value !is ConnectionState.NeedsPairing
        ) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }
}
