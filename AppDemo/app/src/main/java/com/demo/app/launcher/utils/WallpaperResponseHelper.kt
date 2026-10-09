package com.demo.app.launcher.utils

import com.demo.app.launcher.wallpaper.model.WallpaperApiResponse

/**
 * 壁纸接口响应校验。
 * API 文档示例为 `{ "data": ... }`，无 `code` 时 Gson 默认 code=-1，仍视为成功。
 */
internal object WallpaperResponseHelper {

    fun <T> ensureSuccess(response: WallpaperApiResponse<T>?): WallpaperApiResponse<T> {
        requireNotNull(response) { "Invalid JSON" }
        when {
            response.code == 0 -> return response
            response.code == -1 && response.data != null -> return response
            else -> throw IllegalStateException(response.msg ?: "code=${response.code}")
        }
    }

    fun isSuccessCode(code: Int, hasData: Boolean): Boolean =
        code == 0 || (code == -1 && hasData)
}
