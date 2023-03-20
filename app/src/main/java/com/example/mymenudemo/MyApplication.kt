package com.example.mymenudemo

import android.app.Application
import com.drake.brv.utils.BRV

/**
 * @Description:application
 * @Author: dick
 * @CreateDate: 2022/10/13
 * @Version:
 */
class MyApplication : Application() {
    companion object {
        lateinit var instance: Application
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance =this
        // 初始化BindingAdapter的默认绑定ID, 如果不使用DataBinding并不需要初始化
        BRV.modelId = BR.m
    }
}