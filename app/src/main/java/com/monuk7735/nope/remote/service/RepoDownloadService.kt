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
import org.eclipse.jgit.api.Git
import androidx.core.content.edit
import androidx.core.app.ServiceCompat
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.File
import org.eclipse.jgit.lib.ProgressMonitor

class RepoDownloadService : Service() {

    companion object {
        const val ACTION_START_DOWNLOAD = "ACTION_START_DOWNLOAD"
        const val ACTION_STOP_DOWNLOAD = "ACTION_STOP_DOWNLOAD"
        const val EXTRA_URL = "EXTRA_URL"
        const val EXTRA_NAME = "EXTRA_NAME"
        const val EXTRA_DIRECTORY = "EXTRA_DIRECTORY"
    }

    private var currentThread: Thread? = null

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
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startDownload(url: String, name: String, directory: String) {
        val notification = NotificationHelper.createDownloadNotification(this, "Preparing download...", 0, true)
        startForeground(NotificationHelper.NOTIFICATION_ID_DOWNLOAD, notification)
        
        RepoDownloadManager.clear()
        RepoDownloadManager.activeRepoId.postValue(directory)
        RepoDownloadManager.downloadStatus.postValue("Starting service...")

        currentThread = Thread {
            try {
                RepoDownloadManager.log("Initializing...")
                val reposDir = File(filesDir, "repos")
                if (!reposDir.exists()) reposDir.mkdirs()

                val targetDir = File(reposDir, directory)
                
                NotificationHelper.updateDownloadNotification(this, "Downloading $name", 0, true)

                if (name.contains("SQLite") || url.endsWith(".db")) {
                     RepoDownloadManager.log("Detected SQLite repository request.")
                     RepoDownloadManager.downloadStatus.postValue("Starting direct download...")

                     val dbFileName = "irext_db_20251031_sqlite3.db"
                     val dbUrl = "https://opensource.irext.net/irext/database/-/raw/master/db/irext_db_20251031_sqlite3.db"
                     val dbFile = File(targetDir, dbFileName)
                     
                     if (!targetDir.exists()) targetDir.mkdirs()

                     try {
                         RepoDownloadManager.log("Downloading from $dbUrl")
                         downloadFile(dbUrl, dbFile)
                         RepoDownloadManager.log("Download complete: ${dbFile.length()} bytes")
                         
                         if (dbFile.length() < 1024 * 10) {
                              throw Exception("Downloaded file is too small: ${dbFile.length()} bytes")
                         }
                         
                     } catch (e: Exception) {
                         RepoDownloadManager.log("Direct download failed: ${e.message}")
                         throw e
                     }

                } else {
                    if (targetDir.exists() && File(targetDir, ".git").exists()) {
                        RepoDownloadManager.log("Repository exists. Updating...")
                        RepoDownloadManager.downloadStatus.postValue("Updating...")
    
                        val git = Git.open(targetDir)
                        git.pull().setProgressMonitor(createProgressMonitor()).call()
                        git.close()
    
                        RepoDownloadManager.log("Update complete.")
                        RepoDownloadManager.downloadStatus.postValue("Update Complete")
                    } else {
                        if (targetDir.exists()) {
                            RepoDownloadManager.log("Target directory exists but invalid. Cleaning up...")
                            targetDir.deleteRecursively()
                        }
    
                        RepoDownloadManager.log("Cloning repository from $url...")
                        RepoDownloadManager.downloadStatus.postValue("Cloning...")
    
                        val git = Git.cloneRepository()
                            .setURI(url)
                            .setDepth(1)
                            .setDirectory(targetDir)
                            .setProgressMonitor(createProgressMonitor())
                            .call()
                        
                        git.close()
    
                        RepoDownloadManager.log("Clone complete.")
                        RepoDownloadManager.downloadStatus.postValue("Clone Complete")
                    }
                }
                
                var success = true
                val settingsPreferences = getSharedPreferences(getString(R.string.shared_pref_app_settings), Context.MODE_PRIVATE)
                
                if (success) {
                    settingsPreferences.edit { putBoolean("repo_installed_$directory", true) }
                    RepoDownloadManager.downloadProgress.postValue(1f)
                    RepoDownloadManager.downloadStatus.postValue("Download Complete")
                    RepoDownloadManager.log("Operation finished successfully.")
                    NotificationHelper.updateDownloadNotification(this, "Download Complete", 100, false)
                } else {
                    settingsPreferences.edit { putBoolean("repo_installed_$directory", false) }
                    RepoDownloadManager.log("Operation failed validation.")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = "Error: ${e.message}"
                RepoDownloadManager.log(errorMsg)
                RepoDownloadManager.downloadStatus.postValue(errorMsg)
                NotificationHelper.updateDownloadNotification(this, "Error: ${e.message}", 0, false)
            } finally {
               try {
                   Thread.sleep(2000)
               } catch (e: InterruptedException) {
                   e.printStackTrace()
               }
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
               stopSelf()
               RepoDownloadManager.activeRepoId.postValue("")
            }
        }
        currentThread?.start()
    }


    private fun createProgressMonitor(): ProgressMonitor {
        return object : ProgressMonitor {
            private var totalWork = 0
            private var completed = 0

            override fun start(totalTasks: Int) {
                RepoDownloadManager.log("Starting Git operation...")
            }

            override fun beginTask(title: String, totalWork: Int) {
                this.totalWork = totalWork
                this.completed = 0
                RepoDownloadManager.log("Task: $title")
            }

            override fun update(completed: Int) {
                this.completed += completed
                if (totalWork > 0) {
                    val progress = this.completed.toFloat() / this.totalWork.toFloat()
                    RepoDownloadManager.downloadProgress.postValue(progress)
                    

                }
            }

            override fun showDuration(enabled: Boolean) {}

            override fun endTask() {}

            override fun isCancelled(): Boolean = false
        }
    }

    private fun downloadFile(urlStr: String, outputFile: File) {
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
             throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
        }

        val fileLength = connection.contentLength
        val input = BufferedInputStream(url.openStream(), 8192)
        val output = FileOutputStream(outputFile)

        val data = ByteArray(1024)
        var total: Long = 0
        var count: Int
        
        RepoDownloadManager.downloadStatus.postValue("Downloading Database...")

        while (input.read(data).also { count = it } != -1) {
            total += count.toLong()
            if (fileLength > 0) {
                 val progress = (total * 100 / fileLength).toInt()
                 if (total % 4096 == 0L) {
                    RepoDownloadManager.downloadProgress.postValue(total.toFloat() / fileLength.toFloat())
                 }
            }
            output.write(data, 0, count)
        }

        output.flush()
        output.close()
        input.close()
    }
}
