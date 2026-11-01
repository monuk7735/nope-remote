package com.monuk7735.nope.remote.utils

import android.content.Context
import android.content.SharedPreferences
import com.monuk7735.nope.remote.R

enum class HomeSection(val id: String, val title: String) {
    WIFI("wifi", "Android TVs"),
    IR("ir", "IR Remotes"),
    ;

    companion object {
        fun fromId(id: String): HomeSection? = entries.find { it.id == id }
    }
}

data class HomeSectionItem(val section: HomeSection, val isEnabled: Boolean = true)

object HomeSectionsManager {
    const val PREF_HOME_SECTIONS_ORDER = "pref_home_sections_order"
    const val PREF_HOME_SECTION_WIFI_ENABLED = "pref_home_section_wifi_enabled"
    const val PREF_HOME_SECTION_IR_ENABLED = "pref_home_section_ir_enabled"

    val DEFAULT_ORDER = listOf(HomeSection.WIFI, HomeSection.IR)

    fun getPreferences(context: Context): SharedPreferences {
        val prefName = try {
            context.getString(R.string.shared_pref_app_settings)
        } catch (_: Exception) {
            "SHARED_PREF_APP_SETTINGS"
        }
        return context.getSharedPreferences(
            prefName,
            Context.MODE_PRIVATE,
        )
    }

    fun getSections(context: Context): List<HomeSectionItem> {
        val prefs = getPreferences(context)
        val orderString = prefs.getString(PREF_HOME_SECTIONS_ORDER, null)
        val order = if (!orderString.isNullOrBlank()) {
            val parsed = orderString.split(",").mapNotNull { HomeSection.fromId(it.trim()) }
            val missing = DEFAULT_ORDER.filter { !parsed.contains(it) }
            parsed + missing
        } else {
            DEFAULT_ORDER
        }

        val wifiEnabled = prefs.getBoolean(PREF_HOME_SECTION_WIFI_ENABLED, true)
        val irEnabled = prefs.getBoolean(PREF_HOME_SECTION_IR_ENABLED, true)

        val (finalWifi, finalIr) = if (!wifiEnabled && !irEnabled) {
            true to true
        } else {
            wifiEnabled to irEnabled
        }

        return order.map { section ->
            when (section) {
                HomeSection.WIFI -> HomeSectionItem(section, finalWifi)
                HomeSection.IR -> HomeSectionItem(section, finalIr)
            }
        }
    }

    fun saveSections(context: Context, sections: List<HomeSectionItem>) {
        val prefs = getPreferences(context)
        val orderString = sections.joinToString(",") { it.section.id }
        val wifiEnabled = sections.find { it.section == HomeSection.WIFI }?.isEnabled ?: true
        val irEnabled = sections.find { it.section == HomeSection.IR }?.isEnabled ?: true

        prefs.edit()
            .putString(PREF_HOME_SECTIONS_ORDER, orderString)
            .putBoolean(PREF_HOME_SECTION_WIFI_ENABLED, wifiEnabled)
            .putBoolean(PREF_HOME_SECTION_IR_ENABLED, irEnabled)
            .apply()
    }

    fun canToggleSection(
        sections: List<HomeSectionItem>,
        sectionToToggle: HomeSection,
        newEnabledState: Boolean,
    ): Boolean {
        if (newEnabledState) return true
        val enabledCount = sections.count { it.isEnabled }
        val isCurrentlyEnabled = sections.find { it.section == sectionToToggle }?.isEnabled ?: false
        return !(isCurrentlyEnabled && enabledCount <= 1)
    }

    fun resetToDefault(context: Context): List<HomeSectionItem> {
        val defaults = DEFAULT_ORDER.map { HomeSectionItem(it, true) }
        saveSections(context, defaults)
        return defaults
    }

    fun getSummary(sections: List<HomeSectionItem>): String {
        val enabledSections = sections.filter { it.isEnabled }
        return when {
            enabledSections.size == 1 -> "${enabledSections.first().section.title} only"
            enabledSections.isNotEmpty() -> enabledSections.joinToString(" → ") { it.section.title }
            else -> "Default"
        }
    }
}
