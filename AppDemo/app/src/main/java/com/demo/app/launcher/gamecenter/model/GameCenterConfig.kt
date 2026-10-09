package com.demo.app.launcher.gamecenter.model

import java.util.Locale

/** 游戏中心接口请求参数。 */
data class GameCenterConfig(
    val bid: String = "com.rwapp.fm.tvremote",
    val categoryId: Int = 2
) {
    /** 按系统 Locale 返回 jp / us。 */
    fun resolveCountry(): String {
        return if (Locale.getDefault().country.equals("JP", ignoreCase = true)) "jp" else "us"
    }
}
