package com.monuk7735.nope.remote.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.monuk7735.nope.remote.R
import com.monuk7735.nope.remote.SettingsActivity
import com.monuk7735.nope.remote.utils.NotificationHelper
import androidx.core.content.edit
import androidx.core.app.ServiceCompat
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.File
import java.util.zip.ZipInputStream

class RepoDownloadService : Service() {

    companion object {
        const val ACTION_START_DOWNLOAD = "ACTION_START_DOWNLOAD"
        const val ACTION_STOP_DOWNLOAD = "ACTION_STOP_DOWNLOAD"
        const val EXTRA_URL = "EXTRA_URL"
        const val EXTRA_NAME = "EXTRA_NAME"
        const val EXTRA_DIRECTORY = "EXTRA_DIRECTORY"
    }


    private val activeThreads = java.util.concurrent.ConcurrentHashMap<String, Thread>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action) {
                ACTION_START_DOWNLOAD -> {
                    val url = intent.getStringExtra(EXTRA_URL)
                    val name = intent.getStringExtra(EXTRA_NAME)
                    val directory = intent.getStringExtra(EXTRA_DIRECTORY)
                    if (url != null && name != null && directory != null) {
                        startDownload(url, name, directory)
                    }
                }
                ACTION_STOP_DOWNLOAD -> {
                    // Start shutdown if needed, though individual threads handle their own lifecycle
                }
            }
        }
        return START_NOT_STICKY
    }

    private val client by lazy {
        okhttp3.OkHttpClient.Builder()
            .protocols(java.util.Collections.singletonList(okhttp3.Protocol.HTTP_1_1))
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    private val powerManager by lazy { getSystemService(Context.POWER_SERVICE) as android.os.PowerManager }
    private val wifiManager by lazy { applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager }

    private fun startDownload(url: String, name: String, directory: String) {
        if (activeThreads.containsKey(directory) && activeThreads[directory]?.isAlive == true) {
            RepoDownloadManager.log("Download already in progress for $directory")
            return
        }

        val notification = NotificationHelper.createDownloadNotification(this, "Starting $name...", 0, true)
        startForeground(NotificationHelper.NOTIFICATION_ID_DOWNLOAD, notification)
        
        // Initial state
        RepoDownloadManager.updateState(directory, RepoState(DownloadState.DOWNLOADING, 0f, "Preparing...", true))
        RepoDownloadManager.log("Starting download for $name ($directory)...")

        val thread = Thread {
            // Acquire locks
            val wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "NopeRemote:DownloadWakeLock")
            val wifiLock = wifiManager.createWifiLock(android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF, "NopeRemote:DownloadWifiLock")
            
            try {
                wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
                wifiLock.acquire()
                processDownload(url, name, directory)
            } catch (e: Exception) {
                e.printStackTrace()
                RepoDownloadManager.log("Error during download ($directory): ${e.message}")
                RepoDownloadManager.updateState(directory, RepoState(DownloadState.ERROR, 0f, "Error: ${e.message}"))
                NotificationHelper.updateDownloadNotification(this, "Error in $name", 0, false)
            } finally {
                if (wakeLock.isHeld) wakeLock.release()
                if (wifiLock.isHeld) wifiLock.release()
                
                activeThreads.remove(directory)
                // Stop service only if no active downloads remain
                if (activeThreads.isEmpty()) {
                    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
        activeThreads[directory] = thread
        thread.start()
    }

    private fun processDownload(originalUrl: String, name: String, directory: String) {
        val reposDir = File(filesDir, "repos")
        if (!reposDir.exists()) reposDir.mkdirs()
        val targetDir = File(reposDir, directory)

        // Ensure clean state
        if (targetDir.exists()) {
             RepoDownloadManager.updateState(directory, RepoState(DownloadState.DOWNLOADING, 0f, "Cleaning up...", true))
             setWritableRecursively(targetDir)
             targetDir.deleteRecursively()
        }
        targetDir.mkdirs()
        
        // Logic to determine download mode using Enum lookup
        val repoInfo = RepoDownloadManager.RepositoryInfo.fromDirectory(directory)
        
        if (repoInfo == null) {
            // Fallback (should not happen if launched via UI) or explicit error?
            RepoDownloadManager.log("[$name] Unknown repository directory: $directory")
            RepoDownloadManager.updateState(directory, RepoState(DownloadState.ERROR, 0f, "Unknown Repository"))
            return
        }

        if (repoInfo.mode == RepoDownloadManager.DownloadMode.DIRECT_FILE) {
             // SQLite Direct Download
             RepoDownloadManager.log("[$name] Mode: Direct File Download")
             RepoDownloadManager.updateState(directory, RepoState(DownloadState.DOWNLOADING, 0f, "Connecting...", true))

             val fileName = originalUrl.substringAfterLast("/")
             val downloadUrl = repoInfo.url // Use the trusted URL from Enum
             val targetFile = File(targetDir, fileName)

             downloadFile(downloadUrl, targetFile, name, directory)
             targetFile.setReadOnly()
             
             val finalLength = targetFile.length()
             
             if (finalLength < 1024 * 10) {
                   throw Exception("Downloaded file too small: $finalLength bytes")
             }

        } else {
             // ZIP Archive Download
             RepoDownloadManager.log("[$name] Mode: ZIP Download")
             RepoDownloadManager.updateState(directory, RepoState(DownloadState.DOWNLOADING, 0f, "Connecting...", true))
             
             val downloadUrl = repoInfo.url // Use the trusted URL from Enum (already is ZIP url)
             val zipFile = File(targetDir, "repo.zip")

             downloadFile(downloadUrl, zipFile, name, directory)
             
             RepoDownloadManager.updateState(directory, RepoState(DownloadState.EXTRACTING, 0f, "Extracting...", true))
             RepoDownloadManager.log("[$name] Extracting...")
             unzip(zipFile, targetDir)
             zipFile.delete()
             
             setReadOnlyRecursively(targetDir)
        }

        // Success
        // settingsPreferences.edit { putBoolean("repo_installed_$directory", true) }
        
        RepoDownloadManager.updateState(directory, RepoState(DownloadState.INSTALLED, 1f, "Installed"))
        RepoDownloadManager.log("[$name] Installation complete.")
        NotificationHelper.updateDownloadNotification(this, "$name Complete", 100, false)
    }

    private fun downloadFile(urlStr: String, outputFile: File, name: String, directory: String) {
        RepoDownloadManager.log("Starting download (stream) for $name")
        val request = okhttp3.Request.Builder()
            .url(urlStr)
            .header("User-Agent", "Mozilla/5.0 (Android) NopeRemote/1.0")
            .build()
            
        RepoDownloadManager.log("Executing request...")
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("HTTP ${response.code} ${response.message}")
        
        val body = response.body ?: throw Exception("Body null")
        val length = body.contentLength()
        RepoDownloadManager.log("Content-Length: $length, Content-Type: ${body.contentType()}")
        val input = body.byteStream()
        
        // Ensure file exists/writable
        if (outputFile.exists()) outputFile.delete()
        outputFile.createNewFile()
        
        // Use BufferedOutputStream for better disk IO performance
        val output = java.io.BufferedOutputStream(FileOutputStream(outputFile))
        
        val data = ByteArray(32 * 1024)
        var total = 0L
        var count: Int
        var lastUpdate = 0L
        
        try {
            RepoDownloadManager.log("Reading stream...")
            while (input.read(data).also { count = it } != -1) {
                total += count
                output.write(data, 0, count)
                
                val now = System.currentTimeMillis()
                if (now - lastUpdate > 500) {
                    lastUpdate = now
                    updateProgress(directory, name, total, length)
                }
            }
            output.flush()
            RepoDownloadManager.log("Download stream complete. Total: $total bytes")
        } finally {
            output.close()
            input.close()
            body.close()
        }
    }

    private fun updateProgress(directory: String, name: String, current: Long, total: Long) {
        if (total > 0) {
             val progress = current.toFloat() / total.toFloat()
             val percent = (progress * 100).toInt()
             RepoDownloadManager.updateState(directory, RepoState(DownloadState.DOWNLOADING, progress, "Downloading $percent%", false))
             NotificationHelper.updateDownloadNotification(this, "Downloading $name", percent, false)
        } else {
             RepoDownloadManager.updateState(directory, RepoState(DownloadState.DOWNLOADING, 0f, "Downloading...", true))
             NotificationHelper.updateDownloadNotification(this, "Downloading $name", 0, true)
        }
    }
    
    private fun unzip(zipFile: File, targetDir: File) {
        ZipInputStream(BufferedInputStream(java.io.FileInputStream(zipFile))).use { zis ->
            var zipEntry = zis.nextEntry
            while (zipEntry != null) {
                val fileName = zipEntry.name
                val newFile = File(targetDir, fileName)
                
                // Basic security check to prevent Zip Slip
                if (!newFile.canonicalPath.startsWith(targetDir.canonicalPath + File.separator)) {
                    throw Exception("Zip entry is outside of the target dir: $fileName")
                }

                if (zipEntry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    
                    // Ensure we can overwrite if it exists
                    if (newFile.exists()) {
                        if (!newFile.canWrite()) {
                            newFile.setWritable(true)
                        }
                        newFile.delete()
                    }

                    FileOutputStream(newFile).use { fos ->
                        val buffer = ByteArray(1024)
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            fos.write(buffer, 0, len)
                        }
                    }
                }
                zipEntry = zis.nextEntry
            }
            zis.closeEntry()
        }
    }

    private fun setReadOnlyRecursively(file: File) {
        if (file.isDirectory) {
             file.listFiles()?.forEach { setReadOnlyRecursively(it) }
        }
        file.setReadOnly()
    }

    private fun setWritableRecursively(file: File) {
        if (file.isDirectory) {
             file.listFiles()?.forEach { setWritableRecursively(it) }
        }
        file.setWritable(true)
    }
}
