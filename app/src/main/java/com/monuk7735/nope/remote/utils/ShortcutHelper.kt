package com.monuk7735.nope.remote.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.monuk7735.nope.remote.AndroidTvRemoteActivity
import com.monuk7735.nope.remote.RemoteControlActivity
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
)
object ShortcutHelper {

    private const val ICON_SIZE = 216
    private const val DEFAULT_BACKGROUND_COLOR = 0xFF1C1B1F.toInt()
    private const val DEFAULT_ICON_COLOR = 0xFFFFFFFF.toInt()

    fun isPinShortcutSupported(context: Context): Boolean =
        ShortcutManagerCompat.isRequestPinShortcutSupported(context)

    fun createShortcutIconBitmap(
        imageVector: ImageVector,
        size: Int = ICON_SIZE,
        backgroundColor: Int = DEFAULT_BACKGROUND_COLOR,
        iconColor: Int = DEFAULT_ICON_COLOR,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        val targetIconSize = size * 0.52f
        val maxViewport = maxOf(imageVector.viewportWidth, imageVector.viewportHeight).let {
            if (it <= 0f) 24f else it
        }
        val scale = targetIconSize / maxViewport
        val translateX = (size - imageVector.viewportWidth * scale) / 2f
        val translateY = (size - imageVector.viewportHeight * scale) / 2f

        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate(translateX, translateY)
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = iconColor
            style = Paint.Style.FILL
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = iconColor
            style = Paint.Style.STROKE
        }

        fun drawNode(node: VectorNode) {
            when (node) {
                is VectorPath -> {
                    val androidPath = node.pathData.toPath().asAndroidPath()
                    val transformedPath = Path(androidPath).apply {
                        transform(matrix)
                    }
                    if (node.stroke != null && node.fill == null) {
                        strokePaint.strokeWidth = node.strokeLineWidth * scale
                        canvas.drawPath(transformedPath, strokePaint)
                    } else {
                        canvas.drawPath(transformedPath, fillPaint)
                    }
                }
                is VectorGroup -> {
                    for (child in node) {
                        drawNode(child)
                    }
                }
            }
        }

        drawNode(imageVector.root)
        return bitmap
    }

    fun createRemoteShortcutIntent(context: Context, remote: RemoteDataDBModel): Intent =
        Intent(context, RemoteControlActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("remote_id", remote.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

    fun createRemoteShortcutInfo(context: Context, remote: RemoteDataDBModel): ShortcutInfoCompat {
        val iconBitmap = createShortcutIconBitmap(remote.getIcon())
        return ShortcutInfoCompat.Builder(context, "remote_${remote.id}")
            .setShortLabel(remote.name)
            .setLongLabel("${remote.name} (${remote.brand})")
            .setIcon(IconCompat.createWithAdaptiveBitmap(iconBitmap))
            .setIntent(createRemoteShortcutIntent(context, remote))
            .build()
    }

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
    ): ShortcutInfoCompat {
        val iconBitmap = createShortcutIconBitmap(Icons.Outlined.Tv)
        return ShortcutInfoCompat.Builder(context, "wifi_remote_${tv.id}")
            .setShortLabel(tv.name)
            .setLongLabel(tv.name)
            .setIcon(IconCompat.createWithAdaptiveBitmap(iconBitmap))
            .setIntent(createWifiRemoteShortcutIntent(context, tv))
            .build()
    }

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
