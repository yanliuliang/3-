package com.example.mymenudemo

/**
 * childId ： 一级父类的第一个子类在全部theme中的位置
 */
data class TableListBean(
    val label: String,
    var count: Int = 0,
    var childId: Int = 0,
    var select: Boolean = false,
    val list: MutableList<ThemeListBean>
)

data class ThemeListBean(
    val theme: String,
    val parentLabel: Int,
    var isSelect: Boolean = false,
    val list: MutableList<ListDataBean>
)

data class ListDataBean(
    var selectCount: Int = 0,
    val typeName: String,
    val parentTheme: String,
    val parentThemeId: Int,
    val parentLabel: String,
    var isSelect: Boolean = false
)
