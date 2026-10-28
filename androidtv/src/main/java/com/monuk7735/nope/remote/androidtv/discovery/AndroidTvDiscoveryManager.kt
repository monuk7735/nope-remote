package com.monuk7735.nope.remote.androidtv.discovery

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import java.util.Collections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiscoveredTvDevice(val name: String, val host: String, val port: Int)

class AndroidTvDiscoveryManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private val wifiManager = context.applicationContext.getSystemService(
        Context.WIFI_SERVICE,
    ) as? WifiManager
    private val connectivityManager =
        context.applicationContext.getSystemService(
            Context.CONNECTIVITY_SERVICE,
        ) as? ConnectivityManager

    private var multicastLock: WifiManager.MulticastLock? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var networkChangeDebounceJob: Job? = null
    private var lastNetwork: Network? = null

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredTvDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredTvDevice>> = _discoveredDevices.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val activeListeners =
        Collections.synchronizedList(mutableListOf<NsdManager.DiscoveryListener>())
    private val resolveQueue = ArrayDeque<NsdServiceInfo>()
    private var isResolving = false
    private var timeoutJob: Job? = null
    private var isRescanning = false
    private var rescanJob: Job? = null

    companion object {
        private const val TAG = "TvDiscovery"
        const val SERVICE_TYPE_V2 = "_androidtvremote2._tcp"
        const val SERVICE_TYPE_V1 = "_androidtvremote._tcp"
        private const val SCAN_TIMEOUT_MS = 15000L
    }

    @Synchronized
    fun startDiscovery() {
        registerNetworkCallback()
        if (_isDiscovering.value) return
        startDiscoveryInternal()
    }

    private fun registerNetworkCallback() {
        if (networkCallback != null) return
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (lastNetwork == null) {
                    lastNetwork = network
                    return
                }
                if (lastNetwork != network) {
                    lastNetwork = network
                    scheduleNetworkRescan()
                }
            }

            override fun onLost(network: Network) {
                if (lastNetwork == network) {
                    lastNetwork = null
                    scheduleNetworkRescan()
                }
            }

            override fun onLinkPropertiesChanged(
                network: Network,
                linkProperties: LinkProperties,
            ) {
                if (lastNetwork == network) {
                    scheduleNetworkRescan()
                }
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager?.registerDefaultNetworkCallback(callback)
            } else {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                connectivityManager?.registerNetworkCallback(request, callback)
            }
            networkCallback = callback
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register network callback", e)
        }
    }

    private fun scheduleNetworkRescan() {
        networkChangeDebounceJob?.cancel()
        networkChangeDebounceJob = scope.launch {
            delay(600)
            Log.d(TAG, "Network change detected, refreshing discovery")
            rescan()
        }
    }

    private fun startDiscoveryInternal() {
        _isDiscovering.value = true

        try {
            if (multicastLock == null) {
                multicastLock = wifiManager?.createMulticastLock("NopeRemote_mDNS")?.apply {
                    setReferenceCounted(false)
                    acquire()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire multicast lock", e)
        }

        startDiscoveryForServiceType(SERVICE_TYPE_V2)
        scope.launch {
            delay(200)
            if (_isDiscovering.value) {
                startDiscoveryForServiceType(SERVICE_TYPE_V1)
            }
        }

        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(SCAN_TIMEOUT_MS)
            if (_isDiscovering.value) {
                stopDiscoveryServices()
            }
        }
    }

    @Synchronized
    fun rescan() {
        rescanJob?.cancel()
        timeoutJob?.cancel()
        timeoutJob = null
        isRescanning = true
        _isDiscovering.value = true
        _discoveredDevices.value = emptyList()

        synchronized(activeListeners) {
            for (listener in activeListeners) {
                try {
                    nsdManager?.stopServiceDiscovery(listener)
                } catch (_: Exception) {}
            }
            activeListeners.clear()
        }

        synchronized(resolveQueue) {
            resolveQueue.clear()
            isResolving = false
        }

        rescanJob = scope.launch {
            delay(300)
            isRescanning = false
            startDiscoveryInternal()
        }
    }

    private fun startDiscoveryForServiceType(serviceType: String) {
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Discovery started for $regType")
                _isDiscovering.value = true
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(
                    TAG,
                    "Service found: ${serviceInfo.serviceName} type=${serviceInfo.serviceType}",
                )
                queueResolve(serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                val name = cleanServiceName(serviceInfo.serviceName ?: "")
                _discoveredDevices.update { list ->
                    list.filterNot { it.name == name }
                }
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "Discovery stopped for $serviceType")
                synchronized(activeListeners) {
                    activeListeners.remove(this)
                    if (!isRescanning && activeListeners.isEmpty()) {
                        _isDiscovering.value = false
                    }
                }
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Start discovery failed for $serviceType with error $errorCode")
                synchronized(activeListeners) {
                    activeListeners.remove(this)
                    if (!isRescanning && activeListeners.isEmpty()) {
                        _isDiscovering.value = false
                    }
                }
                try {
                    nsdManager?.stopServiceDiscovery(this)
                } catch (_: Exception) {}
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Stop discovery failed for $serviceType with error $errorCode")
                synchronized(activeListeners) {
                    activeListeners.remove(this)
                    if (!isRescanning && activeListeners.isEmpty()) {
                        _isDiscovering.value = false
                    }
                }
            }
        }

        try {
            nsdManager?.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
            activeListeners.add(listener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start discovery for $serviceType", e)
            if (!isRescanning && activeListeners.isEmpty()) {
                _isDiscovering.value = false
            }
        }
    }

    private fun queueResolve(serviceInfo: NsdServiceInfo) {
        synchronized(resolveQueue) {
            resolveQueue.add(serviceInfo)
            if (!isResolving) {
                processNextResolve()
            }
        }
    }

    private fun processNextResolve() {
        val next = synchronized(resolveQueue) {
            if (resolveQueue.isEmpty()) {
                isResolving = false
                return
            }
            isResolving = true
            resolveQueue.removeFirst()
        }

        try {
            nsdManager?.resolveService(
                next,
                object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.w(TAG, "Resolve failed for ${serviceInfo.serviceName}: $errorCode")
                        synchronized(resolveQueue) {
                            isResolving = false
                            processNextResolve()
                        }
                    }

                    override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                        val host = resolvedInfo.host?.hostAddress
                        val name = cleanServiceName(
                            resolvedInfo.serviceName ?: host ?: "Android TV",
                        )
                        val port = resolvedInfo.port
                        Log.d(TAG, "Resolved service: $name at $host:$port")

                        if (host != null) {
                            val device = DiscoveredTvDevice(
                                name = name,
                                host = host,
                                port = port,
                            )
                            _discoveredDevices.update { list ->
                                if (list.none { it.host == host }) {
                                    list + device
                                } else {
                                    list
                                }
                            }
                        }

                        synchronized(resolveQueue) {
                            isResolving = false
                            processNextResolve()
                        }
                    }
                },
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking resolveService", e)
            synchronized(resolveQueue) {
                isResolving = false
                processNextResolve()
            }
        }
    }

    private fun cleanServiceName(rawName: String): String =
        rawName.replace("\\032", " ").replace("\\", "").trim()

    @Synchronized
    private fun stopDiscoveryServices() {
        rescanJob?.cancel()
        rescanJob = null
        isRescanning = false
        timeoutJob?.cancel()
        timeoutJob = null
        _isDiscovering.value = false

        synchronized(activeListeners) {
            for (listener in activeListeners) {
                try {
                    nsdManager?.stopServiceDiscovery(listener)
                } catch (_: Exception) {}
            }
            activeListeners.clear()
        }

        synchronized(resolveQueue) {
            resolveQueue.clear()
            isResolving = false
        }

        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
            }
        } catch (_: Exception) {}
        multicastLock = null
    }

    @Synchronized
    fun stopDiscovery() {
        stopDiscoveryServices()
        networkChangeDebounceJob?.cancel()
        networkChangeDebounceJob = null
        networkCallback?.let { callback ->
            try {
                connectivityManager?.unregisterNetworkCallback(callback)
            } catch (_: Exception) {}
            networkCallback = null
        }
        lastNetwork = null
    }
}
