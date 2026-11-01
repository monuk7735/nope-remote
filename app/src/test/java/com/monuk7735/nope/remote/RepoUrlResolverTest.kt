package com.monuk7735.nope.remote

import com.monuk7735.nope.remote.service.RepoDownloadManager
import com.monuk7735.nope.remote.service.RepoUrlResolver
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepoUrlResolverTest {

    private val sampleApiJson = """
        [
          {
            "name": "irext_db_20260519_sqlite3.db",
            "path": "db/irext_db_20260519_sqlite3.db",
            "type": "file"
          },
          {
            "name": "irext_db_20260929_mysql.sql",
            "path": "db/irext_db_20260929_mysql.sql",
            "type": "file"
          },
          {
            "name": "irext_db_20260929_sqlite3.db",
            "path": "db/irext_db_20260929_sqlite3.db",
            "type": "file"
          }
        ]
    """.trimIndent()

    private val sampleHtml = """
        <div class="Box-row">
            <a href="/irext/database/blob/master/db/irext_db_20260519_sqlite3.db">irext_db_20260519_sqlite3.db</a>
            <a href="/irext/database/blob/master/db/irext_db_20260929_mysql.sql">irext_db_20260929_mysql.sql</a>
            <a href="/irext/database/blob/master/db/irext_db_20260929_sqlite3.db">irext_db_20260929_sqlite3.db</a>
        </div>
    """.trimIndent()

    @Test
    fun testParseDatabaseNamesFromJsonFiltersAndSortsDescending() {
        val names = RepoUrlResolver.parseDatabaseNames(sampleApiJson)
        assertEquals(2, names.size)
        assertEquals("irext_db_20260929_sqlite3.db", names[0])
        assertEquals("irext_db_20260519_sqlite3.db", names[1])
    }

    @Test
    fun testParseDatabaseNamesWithNoMatchesReturnsEmpty() {
        val names = RepoUrlResolver.parseDatabaseNames("no matching files here")
        assertTrue(names.isEmpty())
    }

    @Test
    fun testParseDatabaseNamesFromHtmlFiltersAndSortsDescending() {
        val names = RepoUrlResolver.parseDatabaseNames(sampleHtml)
        assertEquals(2, names.size)
        assertEquals("irext_db_20260929_sqlite3.db", names[0])
        assertEquals("irext_db_20260519_sqlite3.db", names[1])
    }

    @Test
    fun testResolveFromGitHubApiSuccess() {
        val mockClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                if (request.url.encodedPath.contains("contents/db")) {
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(sampleApiJson.toResponseBody("application/json".toMediaTypeOrNull()))
                        .build()
                } else {
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(404)
                        .message("Not Found")
                        .body("".toResponseBody(null))
                        .build()
                }
            }
            .build()

        val resolved = RepoUrlResolver.resolveLatestIrextDbUrl(mockClient)
        assertEquals(
            "https://github.com/irext/database/raw/master/db/irext_db_20260929_sqlite3.db",
            resolved,
        )
    }

    @Test
    fun testResolveFromHtmlFallbackWhenApiFails() {
        val mockClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                if (request.url.host == "api.github.com") {
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(403)
                        .message("Rate Limit Exceeded")
                        .body("{}".toResponseBody("application/json".toMediaTypeOrNull()))
                        .build()
                } else if (request.url.encodedPath.contains("tree/master/db")) {
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(sampleHtml.toResponseBody("text/html".toMediaTypeOrNull()))
                        .build()
                } else {
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(404)
                        .message("Not Found")
                        .body("".toResponseBody(null))
                        .build()
                }
            }
            .build()

        val resolved = RepoUrlResolver.resolveLatestIrextDbUrl(mockClient)
        assertEquals(
            "https://github.com/irext/database/raw/master/db/irext_db_20260929_sqlite3.db",
            resolved,
        )
    }

    @Test
    fun testResolveFallsBackToDefaultWhenAllNetworkFails() {
        val mockClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                throw IOException("Network unreachable")
            }
            .build()

        val fallback = RepoDownloadManager.RepositoryInfo.IREXT.url
        val resolved = RepoUrlResolver.resolveLatestIrextDbUrl(mockClient, fallback)
        assertEquals(fallback, resolved)
    }
}
