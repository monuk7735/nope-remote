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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AndroidTvClient(private val sslManager: AndroidTvSslManager) {

    companion object {
        private const val TAG = "AndroidTvClient"

        // Android TV Remote v2 feature flags
        const val FEATURE_PING = 1 // 2^0
        const val FEATURE_KEY = 2 // 2^1
        const val FEATURE_IME = 4 // 2^2
        const val FEATURE_VOICE = 8 // 2^3
        const val FEATURE_POWER = 32 // 2^5
        const val FEATURE_VOLUME = 64 // 2^6
        const val FEATURE_APP_LINK = 512 // 2^9

        const val SUPPORTED_FEATURES =
            FEATURE_PING or FEATURE_KEY or FEATURE_IME or FEATURE_VOICE or
                FEATURE_POWER or FEATURE_VOLUME or FEATURE_APP_LINK // 623

        private const val HANDSHAKE_TIMEOUT_MS = 10000
        private const val READ_TIMEOUT_MS = 60000
        private const val MAX_AUTO_RECONNECT_RETRIES = 3

        fun formatConnectionError(e: Throwable, host: String): Pair<String, String> {
            val msg = e.message ?: ""
            val isMalformed = e is IllegalArgumentException ||
                (
                    msg.contains("port", ignoreCase = true) &&
                        msg.contains("out of range", ignoreCase = true)
                    )

            val isNotFound = e is java.net.NoRouteToHostException ||
                e is java.net.UnknownHostException ||
                e is java.net.SocketTimeoutException ||
                msg.contains("EHOSTUNREACH", ignoreCase = true) ||
                msg.contains("No route to host", ignoreCase = true) ||
                msg.contains("ENETUNREACH", ignoreCase = true) ||
                msg.contains("timed out", ignoreCase = true) ||
                msg.contains("ETIMEDOUT", ignoreCase = true) ||
                msg.contains("UnknownHost", ignoreCase = true)

            val isRefused = e is java.net.ConnectException ||
                msg.contains("ECONNREFUSED", ignoreCase = true) ||
                msg.contains("Connection refused", ignoreCase = true)

            val title = when {
                isMalformed -> "Invalid Address"
                isNotFound -> "Device Not Found"
                isRefused -> "Connection Refused"
                else -> "Connection Failed"
            }

            val description = when {
                isMalformed ->
                    "The IP address \"$host\" is invalid. Please check the address and try again."
                isNotFound ->
                    "Could not find or reach the device at $host.\n\n" +
                        "Make sure the TV is turned on, connected to the same Wi-Fi network, " +
                        "and the IP address is correct."
                isRefused ->
                    "The device at $host refused the connection.\n\n" +
                        "Make sure the TV is turned on and Android TV remote service is active."
                else ->
                    "Could not connect to $host. Please check your network connection and try again."
            }

            return Pair(title, description)
        }
    }

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        object NeedsPairing : ConnectionState()
        data class Error(val message: String, val title: String = "Connection Failed") :
            ConnectionState()
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
    private var connectionJob: Job? = null
    private val clientScope = CoroutineScope(Dispatchers.IO + Job())

    @Volatile
    private var imeCounter: Int = 0

    @Volatile
    private var fieldCounter: Int = 0

    @Volatile
    private var isExplicitDisconnect: Boolean = false

    fun connect(host: String, port: Int = 6466) {
        isExplicitDisconnect = false
        connectionJob?.cancel()
        disconnectInternal(keepState = false)

        _connectionState.value = ConnectionState.Connecting

        connectionJob = clientScope.launch {
            var retryCount = 0
            var lastException: Throwable? = null

            while (isActive && !isExplicitDisconnect) {
                var wasConnected = false
                try {
                    lastException = null
                    val sslContext = sslManager.createSslContext()
                    val sslSocket = sslContext.socketFactory.createSocket() as SSLSocket
                    sslSocket.keepAlive = true
                    sslSocket.tcpNoDelay = true
                    sslSocket.connect(InetSocketAddress(host, port), 8000)
                    sslSocket.soTimeout = HANDSHAKE_TIMEOUT_MS
                    sslSocket.startHandshake()
                    sslSocket.soTimeout = READ_TIMEOUT_MS

                    socket = sslSocket
                    val input = sslSocket.inputStream
                    val output = sslSocket.outputStream
                    outputStream = output

                    Log.d(TAG, "Connected and handshake complete for $host:$port")
                    retryCount = 0

                    // Read loop
                    while (isActive && !isExplicitDisconnect) {
                        val messageBytes = try {
                            VarintFraming.readFramedMessage(input)
                        } catch (e: Exception) {
                            if (isActive && !isExplicitDisconnect) {
                                Log.w(TAG, "Connection lost during read", e)
                            }
                            break
                        }

                        val msg = Remotemessage.RemoteMessage.parseFrom(messageBytes)
                        Log.d(
                            TAG,
                            "Received msg: cfg=${msg.hasRemoteConfigure()} " +
                                "act=${msg.hasRemoteSetActive()} start=${msg.hasRemoteStart()} " +
                                "ping=${msg.hasRemotePingRequest()}/${msg.hasRemotePingResponse()}",
                        )
                        handleIncomingMessage(msg, output)
                        if (_connectionState.value is ConnectionState.Connected) {
                            wasConnected = true
                        }
                    }
                } catch (e: Exception) {
                    lastException = e
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
                    if (isPairingIssue && !wasConnected) {
                        _connectionState.value = ConnectionState.NeedsPairing
                        break
                    }
                } finally {
                    disconnectInternal(keepState = true)
                }

                if (isExplicitDisconnect || !isActive) {
                    break
                }

                if (wasConnected && retryCount < MAX_AUTO_RECONNECT_RETRIES) {
                    retryCount++
                    val backoffMs = (retryCount * 1000L).coerceAtMost(3000L)
                    Log.d(
                        TAG,
                        "Connection lost. Reconnecting in ${backoffMs}ms " +
                            "(attempt $retryCount/$MAX_AUTO_RECONNECT_RETRIES)...",
                    )
                    _connectionState.value = ConnectionState.Connecting
                    delay(backoffMs)
                } else {
                    if (_connectionState.value !is ConnectionState.NeedsPairing) {
                        val (title, description) = if (lastException != null && !wasConnected) {
                            formatConnectionError(lastException, host)
                        } else {
                            Pair(
                                "Connection Lost",
                                "Connection to $host was lost. Please reconnect.",
                            )
                        }
                        _connectionState.value = ConnectionState.Error(
                            message = description,
                            title = title,
                        )
                    }
                    break
                }
            }
        }
    }

    private fun handleIncomingMessage(msg: Remotemessage.RemoteMessage, output: OutputStream) {
        if (msg.hasRemoteConfigure()) {
            val serverFeatures = msg.remoteConfigure.code1
            val activeFeatures = if (serverFeatures != 0) {
                SUPPORTED_FEATURES and serverFeatures
            } else {
                SUPPORTED_FEATURES
            }
            val reply = Remotemessage.RemoteMessage.newBuilder()
                .setRemoteConfigure(
                    Remotemessage.RemoteConfigure.newBuilder()
                        .setCode1(activeFeatures)
                        .setDeviceInfo(
                            Remotemessage.RemoteDeviceInfo.newBuilder()
                                .setModel("Nope Remote")
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
                    Remotemessage.RemoteSetActive.newBuilder().setActive(SUPPORTED_FEATURES),
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
        } else if (msg.hasRemotePingResponse()) {
            Log.d(TAG, "Received ping response: val1=${msg.remotePingResponse.val1}")
        } else if (msg.hasRemoteError()) {
            Log.w(TAG, "Received remote error from TV: value=${msg.remoteError.value}")
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
        isExplicitDisconnect = true
        connectionJob?.cancel()
        connectionJob = null
        disconnectInternal(keepState = false)
    }

    private fun disconnectInternal(keepState: Boolean = false) {
        try {
            socket?.session?.invalidate()
            socket?.close()
        } catch (_: Exception) {}
        socket = null
        outputStream = null
        if (!keepState &&
            _connectionState.value !is ConnectionState.Error &&
            _connectionState.value !is ConnectionState.NeedsPairing
        ) {
            _connectionState.value = ConnectionState.Disconnected
        }
    }
}
