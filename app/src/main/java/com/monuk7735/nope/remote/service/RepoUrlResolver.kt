package com.monuk7735.nope.remote.service

import okhttp3.OkHttpClient
import okhttp3.Request

object RepoUrlResolver {

    private const val GITHUB_API_IREXT_DB_URL =
        "https://api.github.com/repos/irext/database/contents/db"
    private const val GITHUB_TREE_IREXT_DB_URL =
        "https://github.com/irext/database/tree/master/db"
    private const val RAW_IREXT_DB_BASE_URL =
        "https://github.com/irext/database/raw/master/db/"

    private val SQLITE_DB_REGEX = Regex("""irext_db_[0-9A-Za-z_]+_sqlite3\.db""")

    fun resolveLatestIrextDbUrl(
        client: OkHttpClient,
        fallbackUrl: String = RepoDownloadManager.RepositoryInfo.IREXT.url,
    ): String {
        resolveFromGitHubApi(client)?.let { return it }
        resolveFromHtmlPage(client)?.let { return it }
        return fallbackUrl
    }

    internal fun parseDatabaseNames(text: String): List<String> = SQLITE_DB_REGEX.findAll(text)
        .map { it.value }
        .distinct()
        .toList()
        .sortedDescending()

    private fun resolveFromGitHubApi(client: OkHttpClient): String? {
        return try {
            val request = Request.Builder()
                .url(GITHUB_API_IREXT_DB_URL)
                .header("User-Agent", "Mozilla/5.0 (Android) NopeRemote/1.0")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val candidates = parseDatabaseNames(body)
                val best = candidates.firstOrNull() ?: return null
                RAW_IREXT_DB_BASE_URL + best
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveFromHtmlPage(client: OkHttpClient): String? {
        return try {
            val request = Request.Builder()
                .url(GITHUB_TREE_IREXT_DB_URL)
                .header("User-Agent", "Mozilla/5.0 (Android) NopeRemote/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val candidates = parseDatabaseNames(body)
                val best = candidates.firstOrNull() ?: return null
                RAW_IREXT_DB_BASE_URL + best
            }
        } catch (_: Exception) {
            null
        }
    }
}
