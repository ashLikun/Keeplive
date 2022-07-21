package com.ashlikun.keeplive.simple

import android.app.Application
import kotlinx.coroutines.Job


/**
 * 作者　　: 李坤
 * 创建时间: 2022/3/27　13:43
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：
 */
class MyApp : Application() {
    var job: Job? = null
    var count = 0
    override fun onCreate() {
        super.onCreate()
//        //定义前台服务的默认样式。即标题、描述和图标
//        //定义前台服务的通知点击事件
//        val foregroundNotification = ForegroundNotification("测试Keep", "描述", R.mipmap.ic_launcher) { context, intent -> }
//        //启动保活服务
//        //你需要保活的服务，如socket连接、定时任务等，建议不用匿名内部类的方式在这里写
//        KeepLive.startWork(this, foregroundNotification)
    }
}