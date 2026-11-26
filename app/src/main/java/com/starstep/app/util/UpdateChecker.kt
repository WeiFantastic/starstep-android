package com.starstep.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** 线上版本信息（对应站点 version.json） */
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val downloadPage: String,
    val changelog: String,
    val forceUpdate: Boolean
)

/**
 * 版本检查：请求线上 version.json，对比当前版本。
 * 设计原则——全程静默容错：任何网络/解析失败都返回 null，绝不打扰用户。
 * 只有明确发现 versionCode 更高时才提示。
 */
object UpdateChecker {
    // 线上版本源（花生壳已认领的稳定域名；如域名变更只需改这一行）
    const val VERSION_URL = "https://weifantastic.kicp.fun/version.json"
    const val FEEDBACK_QQ = "1183295524"
    const val FEEDBACK_URL = "mqq://im/chat?chat_type=wpa&uin=1183295524"

    suspend fun check(currentCode: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val conn = URL(VERSION_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.requestMethod = "GET"
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val json = JSONObject(body)
            val remoteCode = json.optInt("versionCode", 0)
            if (remoteCode <= currentCode) return@withContext null  // 已是最新，静默
            UpdateInfo(
                versionCode = remoteCode,
                versionName = json.optString("versionName", ""),
                downloadUrl = json.optString("downloadUrl", ""),
                downloadPage = json.optString("downloadPage", ""),
                changelog = json.optString("changelog", ""),
                forceUpdate = json.optBoolean("forceUpdate", false)
            )
        } catch (t: Throwable) {
            null  // 离线 / 无网络 / 解析失败：一律当作没有更新
        }
    }
}
