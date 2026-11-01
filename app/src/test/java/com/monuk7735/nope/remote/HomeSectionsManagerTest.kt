package com.monuk7735.nope.remote

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.monuk7735.nope.remote.utils.HomeSection
import com.monuk7735.nope.remote.utils.HomeSectionItem
import com.monuk7735.nope.remote.utils.HomeSectionsManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class HomeSectionsManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        clearPreferences()
    }

    @After
    fun tearDown() {
        clearPreferences()
    }

    private fun clearPreferences() {
        HomeSectionsManager.getPreferences(context).edit().clear().commit()
    }

    @Test
    fun testDefaultSectionsOrderAndState() {
        val sections = HomeSectionsManager.getSections(context)
        assertEquals("Default count must be 2", 2, sections.size)
        assertEquals("Default first section must be WIFI", HomeSection.WIFI, sections[0].section)
        assertEquals("Default second section must be IR", HomeSection.IR, sections[1].section)
        assertTrue("WIFI must be enabled by default", sections[0].isEnabled)
        assertTrue("IR must be enabled by default", sections[1].isEnabled)
    }

    @Test
    fun testSaveAndRetrieveReorderedSections() {
        val reordered = listOf(
            HomeSectionItem(HomeSection.IR, true),
            HomeSectionItem(HomeSection.WIFI, true),
        )
        HomeSectionsManager.saveSections(context, reordered)

        val retrieved = HomeSectionsManager.getSections(context)
        assertEquals("First section must be IR", HomeSection.IR, retrieved[0].section)
        assertEquals("Second section must be WIFI", HomeSection.WIFI, retrieved[1].section)
        assertTrue(retrieved[0].isEnabled)
        assertTrue(retrieved[1].isEnabled)
    }

    @Test
    fun testDisableOneSection() {
        // Disable WIFI, keep IR enabled
        val irOnly = listOf(
            HomeSectionItem(HomeSection.IR, true),
            HomeSectionItem(HomeSection.WIFI, false),
        )
        HomeSectionsManager.saveSections(context, irOnly)

        val retrieved = HomeSectionsManager.getSections(context)
        assertEquals(HomeSection.IR, retrieved[0].section)
        assertTrue(retrieved[0].isEnabled)
        assertEquals(HomeSection.WIFI, retrieved[1].section)
        assertFalse(retrieved[1].isEnabled)

        // Disable IR, keep WIFI enabled
        val wifiOnly = listOf(
            HomeSectionItem(HomeSection.WIFI, true),
            HomeSectionItem(HomeSection.IR, false),
        )
        HomeSectionsManager.saveSections(context, wifiOnly)

        val retrievedWifi = HomeSectionsManager.getSections(context)
        assertEquals(HomeSection.WIFI, retrievedWifi[0].section)
        assertTrue(retrievedWifi[0].isEnabled)
        assertEquals(HomeSection.IR, retrievedWifi[1].section)
        assertFalse(retrievedWifi[1].isEnabled)
    }

    @Test
    fun testCannotToggleLastEnabledSection() {
        val wifiOnly = listOf(
            HomeSectionItem(HomeSection.WIFI, true),
            HomeSectionItem(HomeSection.IR, false),
        )

        // Should NOT be allowed to disable WIFI when it is the only enabled section
        val canDisableWifi =
            HomeSectionsManager.canToggleSection(wifiOnly, HomeSection.WIFI, false)
        assertFalse("Cannot disable the last remaining section", canDisableWifi)

        // Should be allowed to re-enable IR
        val canEnableIr = HomeSectionsManager.canToggleSection(wifiOnly, HomeSection.IR, true)
        assertTrue("Can enable disabled section", canEnableIr)
    }

    @Test
    fun testFallbackWhenBothSectionsDisabledInPreferences() {
        // Force both to false in SharedPreferences directly
        HomeSectionsManager.getPreferences(context).edit()
            .putBoolean(HomeSectionsManager.PREF_HOME_SECTION_WIFI_ENABLED, false)
            .putBoolean(HomeSectionsManager.PREF_HOME_SECTION_IR_ENABLED, false)
            .commit()

        val sections = HomeSectionsManager.getSections(context)
        // Must fallback to both enabled
        assertTrue(
            "WIFI must be recovered to enabled",
            sections.find { it.section == HomeSection.WIFI }?.isEnabled == true,
        )
        assertTrue(
            "IR must be recovered to enabled",
            sections.find { it.section == HomeSection.IR }?.isEnabled == true,
        )
    }

    @Test
    fun testResetToDefault() {
        val custom = listOf(
            HomeSectionItem(HomeSection.IR, true),
            HomeSectionItem(HomeSection.WIFI, false),
        )
        HomeSectionsManager.saveSections(context, custom)

        val resetResult = HomeSectionsManager.resetToDefault(context)
        assertEquals(HomeSection.WIFI, resetResult[0].section)
        assertEquals(HomeSection.IR, resetResult[1].section)
        assertTrue(resetResult[0].isEnabled)
        assertTrue(resetResult[1].isEnabled)

        val retrieved = HomeSectionsManager.getSections(context)
        assertEquals(HomeSection.WIFI, retrieved[0].section)
        assertEquals(HomeSection.IR, retrieved[1].section)
        assertTrue(retrieved[0].isEnabled)
        assertTrue(retrieved[1].isEnabled)
    }

    @Test
    fun testSummaryFormatting() {
        val defaultSections = listOf(
            HomeSectionItem(HomeSection.WIFI, true),
            HomeSectionItem(HomeSection.IR, true),
        )
        assertEquals("Android TVs → IR Remotes", HomeSectionsManager.getSummary(defaultSections))

        val reorderedSections = listOf(
            HomeSectionItem(HomeSection.IR, true),
            HomeSectionItem(HomeSection.WIFI, true),
        )
        assertEquals("IR Remotes → Android TVs", HomeSectionsManager.getSummary(reorderedSections))

        val wifiOnly = listOf(
            HomeSectionItem(HomeSection.WIFI, true),
            HomeSectionItem(HomeSection.IR, false),
        )
        assertEquals("Android TVs only", HomeSectionsManager.getSummary(wifiOnly))

        val irOnly = listOf(
            HomeSectionItem(HomeSection.IR, true),
            HomeSectionItem(HomeSection.WIFI, false),
        )
        assertEquals("IR Remotes only", HomeSectionsManager.getSummary(irOnly))
    }
}
