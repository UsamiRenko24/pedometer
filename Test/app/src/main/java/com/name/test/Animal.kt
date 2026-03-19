package com.name.test

/**
 * 动物数据类
 * @param aName 动物名称
 * @param aSpeak 动物叫声/描述
 * @param aIcon 图标资源ID
 */
data class Animal(
    var aName: String = "",
    var aSpeak: String = "",
    var aIcon: Int = 0
)