package com.monuk7735.nope.remote

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.monuk7735.nope.remote.database.Converters
import com.monuk7735.nope.remote.database.RemoteDatabase
import com.monuk7735.nope.remote.models.database.AndroidTvButtonDBModel
import java.io.File
import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DatabaseMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RemoteDatabase::class.java,
    )

    private lateinit var context: Context
    private val dbName = "test-migration.db"
    private val converters = Converters()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        cleanupDb()
    }

    @After
    fun tearDown() {
        cleanupDb()
    }

    private fun cleanupDb() {
        context.deleteDatabase(dbName)
        File(context.filesDir, dbName).delete()
    }

    @Test
    fun testMigration_2_to_3() {
        val dateJson = converters.fromDate(Date())
        helper.createDatabase(dbName, 2).apply {
            execSQL(
                """
                INSERT INTO `remotes` (
                    `name`, `brand`, `type`, `added`,
                    `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`
                ) VALUES ('TV v2', 'Sony', 'TV', ?, '[]', '[]')
                """.trimIndent(),
                arrayOf<Any>(dateJson),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            dbName,
            3,
            true,
            RemoteDatabase.MIGRATION_2_3,
        )

        // Verify existing rows received default value 1 for preferCustomUi
        val cursor = db.query("SELECT preferCustomUi FROM remotes WHERE name = 'TV v2'")
        assertTrue(cursor.moveToFirst())
        assertEquals(1, cursor.getInt(0))
        cursor.close()
    }

    @Test
    fun testMigration_3_to_4() {
        val dateJson = converters.fromDate(Date())
        helper.createDatabase(dbName, 3).apply {
            execSQL(
                """
                INSERT INTO `remotes` (
                    `name`, `brand`, `type`, `added`,
                    `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`, `preferCustomUi`
                ) VALUES ('TV v3', 'LG', 'TV', ?, '[]', '[]', 0)
                """.trimIndent(),
                arrayOf<Any>(dateJson),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            dbName,
            4,
            true,
            RemoteDatabase.MIGRATION_3_4,
        )

        val cursor = db.query("SELECT model, preferCustomUi FROM remotes WHERE name = 'TV v3'")
        assertTrue(cursor.moveToFirst())
        assertTrue(cursor.isNull(0))
        assertEquals(0, cursor.getInt(1))
        cursor.close()
    }

    @Test
    fun testMigration_4_to_5() {
        val dateJson = converters.fromDate(Date())
        helper.createDatabase(dbName, 4).apply {
            execSQL(
                """
                INSERT INTO `remotes` (
                    `name`, `brand`, `type`, `added`,
                    `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`,
                    `preferCustomUi`, `model`
                ) VALUES ('Custom Remote', 'DIY', 'Custom', ?, '[]', '[]', 1, 'v1')
                """.trimIndent(),
                arrayOf<Any>(dateJson),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            dbName,
            5,
            true,
            RemoteDatabase.MIGRATION_4_5,
        )

        val cursor = db.query("SELECT isCustom, model FROM remotes WHERE name = 'Custom Remote'")
        assertTrue(cursor.moveToFirst())
        assertEquals(0, cursor.getInt(0))
        assertEquals("v1", cursor.getString(1))
        cursor.close()
    }

    @Test
    fun testMigration_5_to_6() {
        val dateJson = converters.fromDate(Date())
        helper.createDatabase(dbName, 5).apply {
            execSQL(
                """
                INSERT INTO `remotes` (
                    `name`, `brand`, `type`, `added`,
                    `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`,
                    `preferCustomUi`, `model`, `isCustom`
                ) VALUES ('TV v5', 'Samsung', 'TV', ?, '[]', '[]', 1, 'QLED', 0)
                """.trimIndent(),
                arrayOf<Any>(dateJson),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            dbName,
            6,
            true,
            RemoteDatabase.MIGRATION_5_6,
        )

        // Verify remotes preserved
        val remoteCursor = db.query("SELECT brand FROM remotes WHERE name = 'TV v5'")
        assertTrue(remoteCursor.moveToFirst())
        assertEquals("Samsung", remoteCursor.getString(0))
        remoteCursor.close()

        // Verify wifi_remotes table and unique constraint
        db.execSQL(
            "INSERT INTO `wifi_remotes` (`name`, `host`, `port`, `added`) VALUES (?, ?, ?, ?)",
            arrayOf<Any>("Bedroom TV", "192.168.1.100", 6466, dateJson),
        )

        try {
            db.execSQL(
                "INSERT INTO `wifi_remotes` (`name`, `host`, `port`, `added`) VALUES (?, ?, ?, ?)",
                arrayOf<Any>("Duplicate TV", "192.168.1.100", 6466, dateJson),
            )
            fail("Expected SQLiteConstraintException on duplicate host")
        } catch (_: SQLiteConstraintException) {
            // Expected unique index constraint violation
        }
    }

    @Test
    fun testMigration_6_to_7() {
        val dateJson = converters.fromDate(Date())
        helper.createDatabase(dbName, 6).apply {
            execSQL(
                """
                INSERT INTO `remotes` (
                    `name`, `brand`, `type`, `added`,
                    `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`,
                    `preferCustomUi`, `model`, `isCustom`
                ) VALUES ('Pre-grid TV', 'Sony', 'TV', ?, '[]', '[]', 1, 'Bravia', 0)
                """.trimIndent(),
                arrayOf<Any>(dateJson),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            dbName,
            7,
            true,
            RemoteDatabase.MIGRATION_6_7,
        )

        val cursor = db.query(
            "SELECT gridHCount, gridVCount FROM remotes WHERE name = 'Pre-grid TV'",
        )
        assertTrue(cursor.moveToFirst())
        assertEquals(5, cursor.getInt(0))
        assertEquals(10, cursor.getInt(1))
        cursor.close()
    }

    @Test
    fun testMigration_7_to_8() {
        val dateJson = converters.fromDate(Date())
        helper.createDatabase(dbName, 7).apply {
            execSQL(
                "INSERT INTO `wifi_remotes` (`name`, `host`, `port`, `added`) VALUES (?, ?, ?, ?)",
                arrayOf<Any>("Living Room TV", "192.168.1.100", 6466, dateJson),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            dbName,
            8,
            true,
            RemoteDatabase.MIGRATION_7_8,
        )

        val cursor = db.query(
            "SELECT gridHCount, gridVCount, onScreenButtons, offScreenButtons " +
                "FROM wifi_remotes WHERE host = '192.168.1.100'",
        )
        assertTrue(cursor.moveToFirst())
        assertEquals(5, cursor.getInt(0))
        assertEquals(10, cursor.getInt(1))
        assertTrue(cursor.isNull(2))
        assertTrue(cursor.isNull(3))
        cursor.close()
    }

    @Test
    fun testAllMigrations_2_to_8_sequentially() {
        val dateJson = converters.fromDate(Date())
        helper.createDatabase(dbName, 2).apply {
            execSQL(
                """
                INSERT INTO `remotes` (
                    `name`, `brand`, `type`, `added`,
                    `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`
                ) VALUES ('Old Remote', 'Sony', 'TV', ?, '[]', '[]')
                """.trimIndent(),
                arrayOf<Any>(dateJson),
            )
            execSQL(
                "INSERT INTO `macros` (`name`, `macroUnits`) VALUES ('Power All', '[]')",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            dbName,
            8,
            true,
            RemoteDatabase.MIGRATION_2_3,
            RemoteDatabase.MIGRATION_3_4,
            RemoteDatabase.MIGRATION_4_5,
            RemoteDatabase.MIGRATION_5_6,
            RemoteDatabase.MIGRATION_6_7,
            RemoteDatabase.MIGRATION_7_8,
        )

        // Verify remotes evolved completely
        val remoteCursor = db.query(
            "SELECT preferCustomUi, model, isCustom, gridHCount, gridVCount " +
                "FROM remotes WHERE name = 'Old Remote'",
        )
        assertTrue(remoteCursor.moveToFirst())
        assertEquals(1, remoteCursor.getInt(0))
        assertTrue(remoteCursor.isNull(1))
        assertEquals(0, remoteCursor.getInt(2))
        assertEquals(5, remoteCursor.getInt(3))
        assertEquals(10, remoteCursor.getInt(4))
        remoteCursor.close()

        // Verify macros preserved
        val macroCursor = db.query("SELECT macroUnits FROM macros WHERE name = 'Power All'")
        assertTrue(macroCursor.moveToFirst())
        assertEquals("[]", macroCursor.getString(0))
        macroCursor.close()
    }

    @Test
    fun testMigration_6_to_8_productionUpgradeWithDaoVerification() {
        val dateJson = converters.fromDate(Date())
        helper.createDatabase(dbName, 6).apply {
            execSQL(
                """
                INSERT INTO `remotes` (
                    `name`, `brand`, `type`, `added`,
                    `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`,
                    `preferCustomUi`, `model`, `isCustom`
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any>("IR TV", "Sony", "TV", dateJson, "[]", "[]", 1, "Bravia", 0),
            )
            execSQL(
                "INSERT INTO `macros` (`name`, `macroUnits`) VALUES ('Power All', '[]')",
            )
            execSQL(
                "INSERT INTO `wifi_remotes` (`name`, `host`, `port`, `added`) VALUES (?, ?, ?, ?)",
                arrayOf<Any>("Living Room Android TV", "192.168.1.50", 6466, dateJson),
            )
            close()
        }

        // 1. Run migrations and strictly validate schema against 8.json
        val migratedDb = helper.runMigrationsAndValidate(
            dbName,
            8,
            true,
            RemoteDatabase.MIGRATION_6_7,
            RemoteDatabase.MIGRATION_7_8,
        )

        // 2. Verify schema defaults and preserved data in SQLite
        val remoteCursor = migratedDb.query(
            "SELECT brand, gridHCount, gridVCount FROM remotes WHERE name = 'IR TV'",
        )
        assertTrue(remoteCursor.moveToFirst())
        assertEquals("Sony", remoteCursor.getString(0))
        assertEquals(5, remoteCursor.getInt(1))
        assertEquals(10, remoteCursor.getInt(2))
        remoteCursor.close()

        val wifiCursor = migratedDb.query(
            "SELECT name, port, gridHCount, gridVCount, onScreenButtons " +
                "FROM wifi_remotes WHERE host = '192.168.1.50'",
        )
        assertTrue(wifiCursor.moveToFirst())
        assertEquals("Living Room Android TV", wifiCursor.getString(0))
        assertEquals(6466, wifiCursor.getInt(1))
        assertEquals(5, wifiCursor.getInt(2))
        assertEquals(10, wifiCursor.getInt(3))
        assertTrue(wifiCursor.isNull(4))
        wifiCursor.close()

        // 3. Open via Room and verify DAO read/write operations
        val roomDb = Room.databaseBuilder(
            context,
            RemoteDatabase::class.java,
            dbName,
        )
            .addMigrations(
                RemoteDatabase.MIGRATION_6_7,
                RemoteDatabase.MIGRATION_7_8,
            )
            .fallbackToDestructiveMigration(false)
            .build()

        val wifiDao = roomDb.wifiRemoteDao()
        val device = checkNotNull(runBlocking { wifiDao.getWifiRemoteByHost("192.168.1.50") })
        assertEquals("Living Room Android TV", device.name)
        assertEquals(5, device.gridHCount)
        assertEquals(10, device.gridVCount)
        assertNull(device.onScreenButtons)

        // Update layout via DAO
        val testButton = AndroidTvButtonDBModel(
            id = "test_power",
            name = "Power",
            keyCodeName = "KEYCODE_POWER",
            iconName = "power",
            gridCol = 0,
            gridRow = 0,
        )
        val updatedDevice = device.copy(
            gridHCount = 6,
            gridVCount = 12,
            onScreenButtons = listOf(testButton),
        )
        runBlocking { wifiDao.updateWifiRemote(updatedDevice) }

        val reloadedDevice = checkNotNull(
            runBlocking { wifiDao.getWifiRemoteByHost("192.168.1.50") },
        )
        assertEquals(6, reloadedDevice.gridHCount)
        assertEquals(12, reloadedDevice.gridVCount)
        val reloadedButtons = checkNotNull(reloadedDevice.onScreenButtons)
        assertEquals(1, reloadedButtons.size)
        assertEquals("test_power", reloadedButtons[0].id)

        roomDb.close()
    }
}
