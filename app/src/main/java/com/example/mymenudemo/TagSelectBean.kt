package com.example.mymenudemo

/**
 * @Description:tag编辑输入框
 * @Author: dick
 * @CreateDate: 2023/3/20
 * @Version:
 */
data class TagSelectBean(
    val tagId: Int,
    var select: Boolean = false,
    val text: String,
    val isClose: Boolean = false


)