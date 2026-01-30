package com.wakeup.clock.util

import com.wakeup.clock.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 服务端版本信息（与 GET /api/version 返回一致）
 */
data class RemoteVersion(
    val versionName: String,
    val versionCode: Int,
    val apkUrl: String
)

/**
 * 检查是否有新版本：请求服务端 /api/version，与当前 versionCode 比较。
 * 仅当次生效，「稍后」不持久化，下次启动再检查。
 */
object VersionCheck {

    private const val PATH_VERSION = "/api/version"
    private const val CONNECT_TIMEOUT_MS = 5000
    private const val READ_TIMEOUT_MS = 5000

    /**
     * 获取服务端最新版本信息，失败返回 null。
     */
    suspend fun fetchLatest(): RemoteVersion? = withContext(Dispatchers.IO) {
        try {
            val base = BuildConfig.VERSION_CHECK_BASE_URL.trimEnd('/')
            val url = URL("$base$PATH_VERSION")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = CONNECT_TIMEOUT_MS
            conn.readTimeout = READ_TIMEOUT_MS
            conn.useCaches = false
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
            val body = conn.inputStream.bufferedReader().readText() ?: return@withContext null
            val json = JSONObject(body)
            RemoteVersion(
                versionName = json.optString("versionName", ""),
                versionCode = json.optInt("versionCode", 0),
                apkUrl = json.optString("apkUrl", "")
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 是否有新版本（服务端 versionCode > 当前 versionCode）
     */
    fun isNewer(remote: RemoteVersion): Boolean {
        return remote.versionCode > BuildConfig.VERSION_CODE
    }

    /**
     * 拼接完整下载地址（用于打开浏览器）
     */
    fun fullDownloadUrl(remote: RemoteVersion): String {
        val base = BuildConfig.VERSION_CHECK_BASE_URL.trimEnd('/')
        val path = remote.apkUrl.trimStart('/')
        return if (path.isEmpty()) base else "$base/$path"
    }
}
