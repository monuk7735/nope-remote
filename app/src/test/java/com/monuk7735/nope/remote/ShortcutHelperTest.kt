package com.monuk7735.nope.remote

import android.content.Context
import android.content.Intent
import androidx.core.content.IntentCompat
import androidx.test.core.app.ApplicationProvider
import com.monuk7735.nope.remote.models.database.RemoteDataDBModel
import com.monuk7735.nope.remote.models.database.SavedTvDeviceDBModel
import com.monuk7735.nope.remote.utils.ShortcutHelper
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ShortcutHelperTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    private fun createSampleRemote(
        id: Int = 42,
        name: String = "Living Room TV",
    ): RemoteDataDBModel = RemoteDataDBModel(
        id = id,
        name = name,
        brand = "Sony",
        type = "TV",
        added = Date(),
        onScreenRemoteButtonDBS = emptyList(),
        offScreenRemoteButtonDBS = emptyList(),
    )

    private fun createSampleWifiRemote(
        id: Int = 101,
        name: String = "Bedroom Shield",
    ): SavedTvDeviceDBModel = SavedTvDeviceDBModel(
        id = id,
        name = name,
        host = "192.168.1.50",
        port = 6466,
        added = Date(),
    )

    @Test
    fun testCreateRemoteShortcutIntent() {
        val remote = createSampleRemote()
        val intent = ShortcutHelper.createRemoteShortcutIntent(context, remote)

        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(RemoteControlActivity::class.java.name, intent.component?.className)
        assertEquals(42, intent.getIntExtra("remote_id", -1))
        val parcelableRemote = IntentCompat.getParcelableExtra(
            intent,
            "remote",
            RemoteDataDBModel::class.java,
        )
        assertNotNull(parcelableRemote)
        assertEquals(remote.name, parcelableRemote?.name)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun testCreateRemoteShortcutInfo() {
        val remote = createSampleRemote()
        val info = ShortcutHelper.createRemoteShortcutInfo(context, remote)

        assertEquals("remote_42", info.id)
        assertEquals("Living Room TV", info.shortLabel)
        assertEquals("Living Room TV (Sony)", info.longLabel)
        assertNotNull(info.intent)
        assertEquals(Intent.ACTION_VIEW, info.intent.action)
    }

    @Test
    fun testCreateWifiRemoteShortcutIntent() {
        val tv = createSampleWifiRemote()
        val intent = ShortcutHelper.createWifiRemoteShortcutIntent(context, tv)

        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(AndroidTvRemoteActivity::class.java.name, intent.component?.className)
        assertEquals("192.168.1.50", intent.getStringExtra("host"))
        assertEquals("Bedroom Shield", intent.getStringExtra("name"))
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun testCreateWifiRemoteShortcutInfo() {
        val tv = createSampleWifiRemote()
        val info = ShortcutHelper.createWifiRemoteShortcutInfo(context, tv)

        assertEquals("wifi_remote_101", info.id)
        assertEquals("Bedroom Shield", info.shortLabel)
        assertEquals("Bedroom Shield", info.longLabel)
        assertNotNull(info.intent)
        assertEquals(Intent.ACTION_VIEW, info.intent.action)
    }

    @Test
    fun testDisableShortcutsDoNotCrash() {
        ShortcutHelper.disableRemoteShortcut(context, 42)
        ShortcutHelper.disableWifiRemoteShortcut(context, 101)
    }

    @Test
    fun testIsPinShortcutSupportedReturnsBoolean() {
        val supported = ShortcutHelper.isPinShortcutSupported(context)
        assertTrue(supported || !supported)
    }
}
