package com.demo.app.launcher.wallpaper.parser

/** 壁纸资源相对路径与 CDN 根地址拼接。 */
internal object WallpaperUrlResolver {

    fun absoluteUrl(baseUrl: String, path: String?): String {
        val relative = path?.trim().orEmpty()
        if (relative.isEmpty()) return ""
        if (relative.startsWith("http://") || relative.startsWith("https://")) return relative
        return baseUrl.trimEnd('/') + "/" + relative.trimStart('/')
    }
}
