package ui.navigation

sealed class Screen(val route: String, val label: String) {
    object Record : Screen("record", "记录")
    object Diet : Screen("diet", "饮食")
    object Step : Screen("step", "计步器")
    object Weight : Screen("weight", "体重")
    object Profile : Screen("profile", "我的")
}