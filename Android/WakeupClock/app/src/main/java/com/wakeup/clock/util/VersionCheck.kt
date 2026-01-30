package com.wakeup.clock.util

import android.util.Log
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

    private const val TAG = "VersionCheck"
    private const val PATH_VERSION = "/api/version"
    private const val CONNECT_TIMEOUT_MS = 5000
    private const val READ_TIMEOUT_MS = 5000

    /**
     * 获取服务端最新版本信息，失败返回 null。
     */
    suspend fun fetchLatest(): RemoteVersion? = withContext(Dispatchers.IO) {
        try {
            val base = BuildConfig.VERSION_CHECK_BASE_URL.trimEnd('/')
            val fullUrl = "$base$PATH_VERSION"
            Log.d(TAG, "开始检查版本，URL: $fullUrl")
            
            val url = URL(fullUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = CONNECT_TIMEOUT_MS
            conn.readTimeout = READ_TIMEOUT_MS
            conn.useCaches = false
            
            val responseCode = conn.responseCode
            Log.d(TAG, "服务器响应码: $responseCode")
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "服务器返回非200状态码: $responseCode")
                return@withContext null
            }
            
            val body = conn.inputStream.bufferedReader().readText()
            Log.d(TAG, "服务器返回内容: $body")
            
            if (body.isNullOrEmpty()) {
                Log.w(TAG, "服务器返回空内容")
                return@withContext null
            }
            
            val json = JSONObject(body)
            val remote = RemoteVersion(
                versionName = json.optString("versionName", ""),
                versionCode = json.optInt("versionCode", 0),
                apkUrl = json.optString("apkUrl", "")
            )
            Log.d(TAG, "解析成功: versionName=${remote.versionName}, versionCode=${remote.versionCode}")
            remote
        } catch (e: Exception) {
            Log.e(TAG, "版本检查失败: ${e.message}", e)
            null
        }
    }

    /**
     * 是否有新版本（服务端 versionCode > 当前 versionCode）
     */
    fun isNewer(remote: RemoteVersion): Boolean {
        val localCode = BuildConfig.VERSION_CODE
        val isNewer = remote.versionCode > localCode
        Log.d(TAG, "版本比较: 服务器=${remote.versionCode}, 本地=$localCode, 有新版本=$isNewer")
        return isNewer
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
