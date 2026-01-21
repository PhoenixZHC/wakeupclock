package com.wakeup.clock.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * 节假日检查器
 * 用于检查指定日期是否为节假日或调休工作日
 */
object HolidayChecker {
    
    private const val TAG = "HolidayChecker"
    
    // 节假日 API（使用免费的节假日 API）
    private const val HOLIDAY_API_URL = "https://timor.tech/api/holiday/info/"
    
    // 缓存节假日数据
    private val holidayCache = mutableMapOf<String, HolidayInfo>()
    
    data class HolidayInfo(
        val isHoliday: Boolean,      // 是否为节假日
        val isWorkday: Boolean,       // 是否为调休工作日
        val name: String?             // 节假日名称
    )
    
    /**
     * 检查是否应该跳过闹钟
     * - 如果是节假日，跳过
     * - 如果是调休工作日，不跳过
     */
    fun shouldSkipAlarm(date: Date): Boolean {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        
        // 先检查缓存
        val cached = holidayCache[dateStr]
        if (cached != null) {
            // 节假日跳过，调休工作日不跳过
            return cached.isHoliday && !cached.isWorkday
        }
        
        // 没有缓存数据，默认不跳过
        return false
    }
    
    /**
     * 预加载节假日数据
     */
    suspend fun preloadHolidays(forceRefresh: Boolean = false) {
        if (!forceRefresh && holidayCache.isNotEmpty()) {
            return
        }
        
        withContext(Dispatchers.IO) {
            try {
                // 获取未来30天的节假日信息
                val calendar = Calendar.getInstance()
                for (i in 0 until 30) {
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(calendar.time)
                    
                    if (!holidayCache.containsKey(dateStr) || forceRefresh) {
                        fetchHolidayInfo(dateStr)?.let { info ->
                            holidayCache[dateStr] = info
                        }
                    }
                    
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                
                Log.d(TAG, "Preloaded ${holidayCache.size} holiday entries")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to preload holidays: ${e.message}")
            }
        }
    }
    
    /**
     * 从 API 获取节假日信息
     */
    private fun fetchHolidayInfo(dateStr: String): HolidayInfo? {
        return try {
            val url = URL("$HOLIDAY_API_URL$dateStr")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                parseHolidayResponse(response)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch holiday info for $dateStr: ${e.message}")
            null
        }
    }
    
    /**
     * 解析节假日 API 响应
     */
    private fun parseHolidayResponse(response: String): HolidayInfo? {
        return try {
            val json = JSONObject(response)
            if (json.getInt("code") != 0) {
                return null
            }
            
            val type = json.getJSONObject("type")
            val typeCode = type.getInt("type")
            val name: String? = if (type.has("name") && !type.isNull("name")) type.getString("name") else null
            
            // type: 0=工作日, 1=周末, 2=节假日, 3=调休工作日
            HolidayInfo(
                isHoliday = typeCode == 1 || typeCode == 2,
                isWorkday = typeCode == 0 || typeCode == 3,
                name = name
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse holiday response: ${e.message}")
            null
        }
    }
    
    /**
     * 清除缓存
     */
    fun clearCache() {
        holidayCache.clear()
    }
}
