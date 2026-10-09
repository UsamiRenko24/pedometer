package com.demo.app.launcher.gamecenter.parser

import com.demo.app.integration.http.EncodeUtils
import java.nio.charset.StandardCharsets

/** 游戏中心 Base64 文本解码与资源 URL 拼接。 */
internal object GameCenterTextDecoder {

    private val BASE64_TEXT_PATTERN = Regex("^[A-Za-z0-9+/]+={0,2}$")

    /**
     * 仅当字符串形如 Base64 时才解码；已是中文/英文明文时原样返回，避免标题乱码。
     */
    fun decodeBase64Text(value: String?): String {
        val raw = value?.trim().orEmpty()
        if (raw.isEmpty()) return ""
        if (!isLikelyBase64(raw)) return raw
        return runCatching {
            val decoded = String(EncodeUtils.decode(raw), StandardCharsets.UTF_8).trim()
            decoded.ifBlank { raw }
        }.getOrDefault(raw)
    }

    private fun isLikelyBase64(value: String): Boolean {
        if (value.length < 8 || value.length % 4 != 0) return false
        if (!BASE64_TEXT_PATTERN.matches(value)) return false
        // 已含中文等非 ASCII 字符，视为服务端明文标题
        if (value.any { it.code > 0x7F }) return false
        return true
    }

    fun absoluteUrl(baseUrl: String, path: String?): String {
        val relative = path?.trim().orEmpty()
        if (relative.isEmpty()) return ""
        if (relative.startsWith("http://") || relative.startsWith("https://")) return relative
        return baseUrl.trimEnd('/') + "/" + relative.trimStart('/')
    }
}
