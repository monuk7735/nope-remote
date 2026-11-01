package com.monuk7735.nope.remote

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.monuk7735.nope.remote.database.RemoteDatabase
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DatabaseMigrationTest {

    private lateinit var context: Context
    private val dbName = "test-migration.db"

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

    private fun createDatabaseAtVersion(
        version: Int,
        setupBlock: (SupportSQLiteDatabase) -> Unit,
    ): SupportSQLiteDatabase {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    setupBlock(db)
                }

                override fun onUpgrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int,
                ) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        return helper.writableDatabase
    }

    data class ColumnInfo(
        val cid: Int,
        val name: String,
        val type: String,
        val notNull: Boolean,
        val defaultValue: String?,
        val pk: Boolean,
    )

    private fun getTableColumns(db: SupportSQLiteDatabase, table: String): Map<String, ColumnInfo> {
        val cursor = db.query("PRAGMA table_info(`$table`)")
        val columns = mutableMapOf<String, ColumnInfo>()
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            columns[name] = ColumnInfo(
                cid = cursor.getInt(cursor.getColumnIndexOrThrow("cid")),
                name = name,
                type = cursor.getString(cursor.getColumnIndexOrThrow("type")).uppercase(),
                notNull = cursor.getInt(cursor.getColumnIndexOrThrow("notnull")) == 1,
                defaultValue = cursor.getString(cursor.getColumnIndexOrThrow("dflt_value")),
                pk = cursor.getInt(cursor.getColumnIndexOrThrow("pk")) > 0,
            )
        }
        cursor.close()
        return columns
    }

    private fun hasIndex(db: SupportSQLiteDatabase, table: String, indexName: String): Boolean {
        val cursor = db.query("PRAGMA index_list(`$table`)")
        var found = false
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == indexName) {
                found = true
                break
            }
        }
        cursor.close()
        return found
    }

    @Test
    fun testMigration_2_to_3_separately() {
        val db = createDatabaseAtVersion(2) { database ->
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `remotes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `brand` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `added` TEXT NOT NULL,
                    `onScreenRemoteButtonDBS` TEXT NOT NULL,
                    `offScreenRemoteButtonDBS` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `macros` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `macroUnits` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO `remotes` (`name`, `brand`, `type`, `added`, `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`)
                VALUES ('TV v2', 'Sony', 'TV', '2025-01-01', '[]', '[]')
                """.trimIndent(),
            )
        }

        // Before migration, preferCustomUi does not exist
        assertFalse(getTableColumns(db, "remotes").containsKey("preferCustomUi"))

        // Run MIGRATION_2_3
        RemoteDatabase.MIGRATION_2_3.migrate(db)

        // Verify column added with expected attributes
        val columns = getTableColumns(db, "remotes")
        assertTrue("preferCustomUi column must exist", columns.containsKey("preferCustomUi"))
        val col = columns["preferCustomUi"]!!
        assertEquals("INTEGER", col.type)
        assertTrue("preferCustomUi must be NOT NULL", col.notNull)
        assertEquals("1", col.defaultValue)

        // Verify existing rows received default value 1
        val cursor = db.query("SELECT preferCustomUi FROM remotes WHERE name = 'TV v2'")
        assertTrue(cursor.moveToFirst())
        assertEquals(1, cursor.getInt(0))
        cursor.close()

        db.close()
    }

    @Test
    fun testMigration_3_to_4_separately() {
        val db = createDatabaseAtVersion(3) { database ->
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `remotes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `brand` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `added` TEXT NOT NULL,
                    `onScreenRemoteButtonDBS` TEXT NOT NULL,
                    `offScreenRemoteButtonDBS` TEXT NOT NULL,
                    `preferCustomUi` INTEGER NOT NULL DEFAULT 1
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `macros` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `macroUnits` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO `remotes` (`name`, `brand`, `type`, `added`, `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`, `preferCustomUi`)
                VALUES ('TV v3', 'LG', 'TV', '2025-02-01', '[]', '[]', 1)
                """.trimIndent(),
            )
        }

        // Before migration, model does not exist
        assertFalse(getTableColumns(db, "remotes").containsKey("model"))

        // Run MIGRATION_3_4
        RemoteDatabase.MIGRATION_3_4.migrate(db)

        // Verify column added with expected attributes
        val columns = getTableColumns(db, "remotes")
        assertTrue("model column must exist", columns.containsKey("model"))
        val col = columns["model"]!!
        assertEquals("TEXT", col.type)
        assertFalse("model must be nullable", col.notNull)

        // Verify existing rows have NULL model
        val cursor = db.query("SELECT model FROM remotes WHERE name = 'TV v3'")
        assertTrue(cursor.moveToFirst())
        assertTrue("model should be null for existing rows", cursor.isNull(0))
        cursor.close()

        // Verify inserting new row with model works
        db.execSQL(
            """
            INSERT INTO `remotes` (`name`, `brand`, `type`, `added`, `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`, `preferCustomUi`, `model`)
            VALUES ('TV v4', 'LG', 'TV', '2025-02-02', '[]', '[]', 1, 'OLED55C1')
            """.trimIndent(),
        )
        val newCursor = db.query("SELECT model FROM remotes WHERE name = 'TV v4'")
        assertTrue(newCursor.moveToFirst())
        assertEquals("OLED55C1", newCursor.getString(0))
        newCursor.close()

        db.close()
    }

    @Test
    fun testMigration_4_to_5_separately() {
        val db = createDatabaseAtVersion(4) { database ->
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `remotes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `brand` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `added` TEXT NOT NULL,
                    `onScreenRemoteButtonDBS` TEXT NOT NULL,
                    `offScreenRemoteButtonDBS` TEXT NOT NULL,
                    `preferCustomUi` INTEGER NOT NULL DEFAULT 1,
                    `model` TEXT DEFAULT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `macros` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `macroUnits` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO `remotes` (`name`, `brand`, `type`, `added`, `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`, `preferCustomUi`, `model`)
                VALUES ('TV v4', 'Panasonic', 'TV', '2025-03-01', '[]', '[]', 1, NULL)
                """.trimIndent(),
            )
        }

        // Before migration, isCustom does not exist
        assertFalse(getTableColumns(db, "remotes").containsKey("isCustom"))

        // Run MIGRATION_4_5
        RemoteDatabase.MIGRATION_4_5.migrate(db)

        // Verify column added with expected attributes
        val columns = getTableColumns(db, "remotes")
        assertTrue("isCustom column must exist", columns.containsKey("isCustom"))
        val col = columns["isCustom"]!!
        assertEquals("INTEGER", col.type)
        assertTrue("isCustom must be NOT NULL", col.notNull)
        assertEquals("0", col.defaultValue)

        // Verify existing rows have isCustom = 0
        val cursor = db.query("SELECT isCustom FROM remotes WHERE name = 'TV v4'")
        assertTrue(cursor.moveToFirst())
        assertEquals(0, cursor.getInt(0))
        cursor.close()

        db.close()
    }

    @Test
    fun testMigration_5_to_6_separately() {
        val db = createDatabaseAtVersion(5) { database ->
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `remotes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `brand` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `added` TEXT NOT NULL,
                    `onScreenRemoteButtonDBS` TEXT NOT NULL,
                    `offScreenRemoteButtonDBS` TEXT NOT NULL,
                    `preferCustomUi` INTEGER NOT NULL,
                    `model` TEXT,
                    `isCustom` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `macros` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `macroUnits` TEXT NOT NULL
                )
                """.trimIndent(),
            )
        }

        // Before migration, wifi_remotes does not exist
        val tablesBefore = mutableListOf<String>()
        val checkCursor = db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='wifi_remotes'",
        )
        while (checkCursor.moveToNext()) {
            tablesBefore.add(checkCursor.getString(0))
        }
        checkCursor.close()
        assertTrue("wifi_remotes must not exist before migration", tablesBefore.isEmpty())

        // Run MIGRATION_5_6
        RemoteDatabase.MIGRATION_5_6.migrate(db)

        // Verify wifi_remotes table columns match Room expected schema
        val columns = getTableColumns(db, "wifi_remotes")
        assertEquals(5, columns.size)

        // Column: id
        val idCol = columns["id"]
        assertNotNull(idCol)
        assertEquals("INTEGER", idCol!!.type)
        assertTrue(idCol.notNull)
        assertTrue(idCol.pk)

        // Column: name
        val nameCol = columns["name"]
        assertNotNull(nameCol)
        assertEquals("TEXT", nameCol!!.type)
        assertTrue(nameCol.notNull)

        // Column: host
        val hostCol = columns["host"]
        assertNotNull(hostCol)
        assertEquals("TEXT", hostCol!!.type)
        assertTrue(hostCol.notNull)

        // Column: port
        val portCol = columns["port"]
        assertNotNull(portCol)
        assertEquals("INTEGER", portCol!!.type)
        assertTrue(portCol.notNull)

        // Column: added (CRITICAL: must be TEXT to match Room's Date TypeConverter)
        val addedCol = columns["added"]
        assertNotNull(addedCol)
        assertEquals("TEXT", addedCol!!.type)
        assertTrue(addedCol.notNull)

        // Verify unique index on host exists
        assertTrue(
            "index_wifi_remotes_host must exist",
            hasIndex(db, "wifi_remotes", "index_wifi_remotes_host"),
        )

        // Verify insert and unique constraint enforcement
        db.execSQL(
            """
            INSERT INTO `wifi_remotes` (`name`, `host`, `port`, `added`)
            VALUES ('Bedroom TV', '192.168.1.100', 6466, '2026-11-01T12:00:00Z')
            """.trimIndent(),
        )

        try {
            db.execSQL(
                """
                INSERT INTO `wifi_remotes` (`name`, `host`, `port`, `added`)
                VALUES ('Duplicate TV', '192.168.1.100', 6466, '2026-11-01T12:00:00Z')
                """.trimIndent(),
            )
            fail("Expected SQLiteConstraintException on duplicate host")
        } catch (_: SQLiteConstraintException) {
            // Expected
        }

        db.close()

        // Finally, verify Room's own schema validator accepts the migrated database
        val roomDb = Room.databaseBuilder(
            context,
            RemoteDatabase::class.java,
            dbName,
        )
            .addMigrations(
                RemoteDatabase.MIGRATION_2_3,
                RemoteDatabase.MIGRATION_3_4,
                RemoteDatabase.MIGRATION_4_5,
                RemoteDatabase.MIGRATION_5_6,
                RemoteDatabase.MIGRATION_6_7,
            )
            .fallbackToDestructiveMigration(false)
            .build()

        val writableDb = roomDb.openHelper.writableDatabase
        assertNotNull(writableDb)
        roomDb.close()
    }

    @Test
    fun testMigration_6_to_7_separately() {
        val db = createDatabaseAtVersion(6) { database ->
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `remotes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `brand` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `added` TEXT NOT NULL,
                    `onScreenRemoteButtonDBS` TEXT NOT NULL,
                    `offScreenRemoteButtonDBS` TEXT NOT NULL,
                    `preferCustomUi` INTEGER NOT NULL,
                    `model` TEXT,
                    `isCustom` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `macros` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `macroUnits` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `wifi_remotes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `host` TEXT NOT NULL,
                    `port` INTEGER NOT NULL DEFAULT 6466,
                    `added` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO `remotes` (`name`, `brand`, `type`, `added`, `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`, `preferCustomUi`, `model`, `isCustom`)
                VALUES ('Living Room TV', 'Sony', 'TV', '2026-01-01', '[]', '[]', 1, NULL, 0)
                """.trimIndent(),
            )
        }

        RemoteDatabase.MIGRATION_6_7.migrate(db)

        val columns = getTableColumns(db, "remotes")
        assertTrue(columns.containsKey("gridHCount"))
        assertTrue(columns.containsKey("gridVCount"))
        assertEquals("INTEGER", columns["gridHCount"]!!.type)
        assertEquals("INTEGER", columns["gridVCount"]!!.type)

        val cursor = db.query(
            "SELECT gridHCount, gridVCount FROM remotes WHERE name = 'Living Room TV'",
        )
        assertTrue(cursor.moveToFirst())
        assertEquals(5, cursor.getInt(0))
        assertEquals(10, cursor.getInt(1))
        cursor.close()
        db.close()
    }

    @Test
    fun testMigration7to8_addsWifiRemoteLayoutColumns() {
        val db = createDatabaseAtVersion(7) { database ->
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `remotes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `brand` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `added` TEXT NOT NULL,
                    `onScreenRemoteButtonDBS` TEXT NOT NULL,
                    `offScreenRemoteButtonDBS` TEXT NOT NULL,
                    `preferCustomUi` INTEGER NOT NULL,
                    `model` TEXT,
                    `isCustom` INTEGER NOT NULL,
                    `gridHCount` INTEGER NOT NULL DEFAULT 5,
                    `gridVCount` INTEGER NOT NULL DEFAULT 10
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `macros` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `macroUnits` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `wifi_remotes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `host` TEXT NOT NULL,
                    `port` INTEGER NOT NULL DEFAULT 6466,
                    `added` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO `wifi_remotes` (`name`, `host`, `port`, `added`)
                VALUES ('Living Room TV', '192.168.1.100', 6466, '2026-01-01')
                """.trimIndent(),
            )
        }

        RemoteDatabase.MIGRATION_7_8.migrate(db)

        val wifiColumns = getTableColumns(db, "wifi_remotes")
        assertTrue(wifiColumns.containsKey("onScreenButtons"))
        assertTrue(wifiColumns.containsKey("offScreenButtons"))
        assertTrue(wifiColumns.containsKey("gridHCount"))
        assertTrue(wifiColumns.containsKey("gridVCount"))
        assertEquals("TEXT", wifiColumns["onScreenButtons"]!!.type)
        assertEquals("TEXT", wifiColumns["offScreenButtons"]!!.type)
        assertEquals("INTEGER", wifiColumns["gridHCount"]!!.type)
        assertEquals("INTEGER", wifiColumns["gridVCount"]!!.type)

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
        db.close()
    }

    @Test
    fun testAllMigrationsSequentially_from2to8() {
        val db = createDatabaseAtVersion(2) { database ->
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `remotes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `brand` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `added` TEXT NOT NULL,
                    `onScreenRemoteButtonDBS` TEXT NOT NULL,
                    `offScreenRemoteButtonDBS` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `macros` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `macroUnits` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            database.execSQL(
                """
                INSERT INTO `remotes` (`name`, `brand`, `type`, `added`, `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`)
                VALUES ('Legacy Remote', 'Samsung', 'TV', '2024-01-01', '[]', '[]')
                """.trimIndent(),
            )
        }

        // Close initial DB at version 2. Room will execute all migrations (2 -> 3 -> 4 -> 5 -> 6 -> 7 -> 8)
        db.close()

        // Open via Room to trigger full onValidateSchema() check for version 8
        val roomDb = Room.databaseBuilder(
            context,
            RemoteDatabase::class.java,
            dbName,
        )
            .addMigrations(
                RemoteDatabase.MIGRATION_2_3,
                RemoteDatabase.MIGRATION_3_4,
                RemoteDatabase.MIGRATION_4_5,
                RemoteDatabase.MIGRATION_5_6,
                RemoteDatabase.MIGRATION_6_7,
                RemoteDatabase.MIGRATION_7_8,
            )
            .fallbackToDestructiveMigration(false)
            .build()

        val writableDb = roomDb.openHelper.writableDatabase
        assertNotNull(writableDb)

        // Verify the legacy remote retained its data and acquired defaults
        val cursor = writableDb.query("SELECT * FROM remotes WHERE name = 'Legacy Remote'")
        assertTrue(cursor.moveToFirst())
        val preferCustomUiIdx = cursor.getColumnIndex("preferCustomUi")
        val modelIdx = cursor.getColumnIndex("model")
        val isCustomIdx = cursor.getColumnIndex("isCustom")
        val gridHCountIdx = cursor.getColumnIndex("gridHCount")
        val gridVCountIdx = cursor.getColumnIndex("gridVCount")
        assertEquals(1, cursor.getInt(preferCustomUiIdx))
        assertTrue(cursor.isNull(modelIdx))
        assertEquals(0, cursor.getInt(isCustomIdx))
        assertEquals(5, cursor.getInt(gridHCountIdx))
        assertEquals(10, cursor.getInt(gridVCountIdx))
        cursor.close()

        roomDb.close()
    }

    @Test
    fun testRecoveryFromFailedMigration_succeedsCleanly() {
        val initialDb = createDatabaseAtVersion(5) { db ->
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `remotes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `brand` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `added` TEXT NOT NULL,
                    `onScreenRemoteButtonDBS` TEXT NOT NULL,
                    `offScreenRemoteButtonDBS` TEXT NOT NULL,
                    `preferCustomUi` INTEGER NOT NULL,
                    `model` TEXT,
                    `isCustom` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `macros` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `macroUnits` TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO `remotes` (`name`, `brand`, `type`, `added`, `onScreenRemoteButtonDBS`, `offScreenRemoteButtonDBS`, `preferCustomUi`, `model`, `isCustom`)
                VALUES ('Living Room TV', 'Samsung', 'TV', '2026-01-01', '[]', '[]', 1, NULL, 0)
                """.trimIndent(),
            )
        }
        initialDb.close()

        // Simulate previous broken migration attempt that threw IllegalStateException
        val brokenMigration = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `wifi_remotes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `host` TEXT NOT NULL,
                        `port` INTEGER NOT NULL DEFAULT 6466,
                        `added` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }

        try {
            val brokenRoomDb = Room.databaseBuilder(context, RemoteDatabase::class.java, dbName)
                .addMigrations(
                    brokenMigration,
                    RemoteDatabase.MIGRATION_6_7,
                    RemoteDatabase.MIGRATION_7_8,
                )
                .fallbackToDestructiveMigration(false)
                .build()
            brokenRoomDb.openHelper.writableDatabase
            fail("Expected schema validation failure on broken migration")
        } catch (_: IllegalStateException) {
            // Expected failure
        }

        // Verify the database version was rolled back to 5 by SQLite transaction management
        val verifyDb = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context).name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                    override fun onCreate(db: SupportSQLiteDatabase) {}
                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) {}
                }).build(),
        ).writableDatabase

        val cursor = verifyDb.query("PRAGMA user_version")
        assertTrue(cursor.moveToFirst())
        assertEquals("Database version must remain at 5 after rollback", 5, cursor.getInt(0))
        cursor.close()
        verifyDb.close()

        // Now run the app with the fixed MIGRATION_5_6 - it must recover seamlessly
        val recoveredRoomDb = Room.databaseBuilder(context, RemoteDatabase::class.java, dbName)
            .addMigrations(
                RemoteDatabase.MIGRATION_2_3,
                RemoteDatabase.MIGRATION_3_4,
                RemoteDatabase.MIGRATION_4_5,
                RemoteDatabase.MIGRATION_5_6,
                RemoteDatabase.MIGRATION_6_7,
                RemoteDatabase.MIGRATION_7_8,
            )
            .fallbackToDestructiveMigration(false)
            .build()

        val writableDb = recoveredRoomDb.openHelper.writableDatabase
        assertNotNull(writableDb)

        // Verify user data is preserved
        val remoteCursor = writableDb.query(
            "SELECT name FROM remotes WHERE name = 'Living Room TV'",
        )
        assertTrue("User remotes must be intact after recovery", remoteCursor.moveToFirst())
        remoteCursor.close()

        // Verify wifi_remotes table is ready and functional
        val wifiCursor = writableDb.query("SELECT * FROM wifi_remotes")
        assertNotNull(wifiCursor)
        assertEquals(0, wifiCursor.count)
        wifiCursor.close()

        recoveredRoomDb.close()
    }
}
