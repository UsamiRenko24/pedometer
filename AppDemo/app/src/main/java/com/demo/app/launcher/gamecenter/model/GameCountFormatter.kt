package com.demo.app.launcher.gamecenter.model

import java.text.NumberFormat
import java.util.Locale

/** 下载量等数值的展示格式化。 */
object GameCountFormatter {

    fun formatCompact(count: Long): String {
        return when {
            count >= 1_000_000_000 -> String.format(Locale.US, "%.1fB", count / 1_000_000_000.0)
            count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    fun formatLocalized(count: Long): String {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(count)
    }
}
