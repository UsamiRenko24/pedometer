package com.demo.app.launcher.gamecenter

/**
 * 游戏中心会话级状态：首页 [url] 在打开游戏中心期间持久存在，
 * 首页刷新时覆盖，关闭 [GameCenterHostActivity] 时释放。
 */
object GameCenterSession {

    @Volatile
    var assetBaseUrl: String = ""
        private set

    fun updateAssetBaseUrl(url: String) {
        assetBaseUrl = url.trim()
    }

    fun clear() {
        assetBaseUrl = ""
    }
}
