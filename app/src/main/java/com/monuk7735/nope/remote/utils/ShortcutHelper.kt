package com.monuk7735.nope.remote.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.monuk7735.nope.remote.AndroidTvRemoteActivity
import com.monuk7735.nope.remote.R
import com.monuk7735.nope.remote.RemoteControlActivity
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
)
object ShortcutHelper {

    fun isPinShortcutSupported(context: Context): Boolean =
        ShortcutManagerCompat.isRequestPinShortcutSupported(context)

    fun createRemoteShortcutIntent(context: Context, remote: RemoteDataDBModel): Intent =
        Intent(context, RemoteControlActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("remote_id", remote.id)
            putExtra("remote", remote)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

    fun createRemoteShortcutInfo(context: Context, remote: RemoteDataDBModel): ShortcutInfoCompat =
        ShortcutInfoCompat.Builder(context, "remote_${remote.id}")
            .setShortLabel(remote.name)
            .setLongLabel("${remote.name} (${remote.brand})")
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(createRemoteShortcutIntent(context, remote))
            .build()

    fun pinRemoteShortcut(context: Context, remote: RemoteDataDBModel): Boolean {
        if (!isPinShortcutSupported(context)) return false
        val shortcutInfo = createRemoteShortcutInfo(context, remote)
        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }

    fun createWifiRemoteShortcutIntent(context: Context, tv: SavedTvDeviceDBModel): Intent =
        Intent(context, AndroidTvRemoteActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("host", tv.host)
            putExtra("name", tv.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

    fun createWifiRemoteShortcutInfo(
        context: Context,
        tv: SavedTvDeviceDBModel,
    ): ShortcutInfoCompat = ShortcutInfoCompat.Builder(context, "wifi_remote_${tv.id}")
        .setShortLabel(tv.name)
        .setLongLabel(tv.name)
        .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
        .setIntent(createWifiRemoteShortcutIntent(context, tv))
        .build()

    fun pinWifiRemoteShortcut(context: Context, tv: SavedTvDeviceDBModel): Boolean {
        if (!isPinShortcutSupported(context)) return false
        val shortcutInfo = createWifiRemoteShortcutInfo(context, tv)
        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }

    fun disableRemoteShortcut(context: Context, remoteId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                ShortcutManagerCompat.disableShortcuts(
                    context,
                    listOf("remote_$remoteId"),
                    "Remote has been deleted",
                )
            } catch (_: Exception) {
            }
        }
    }

    fun disableWifiRemoteShortcut(context: Context, tvId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                ShortcutManagerCompat.disableShortcuts(
                    context,
                    listOf("wifi_remote_$tvId"),
                    "TV remote has been deleted",
                )
            } catch (_: Exception) {
            }
        }
    }
}
