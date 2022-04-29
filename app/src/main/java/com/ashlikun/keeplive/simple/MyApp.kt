package com.ashlikun.keeplive.simple

import android.app.Application
import android.util.Log
import com.ashlikun.keeplive.KeepLive
import com.ashlikun.keeplive.config.ForegroundNotification
import com.ashlikun.keeplive.config.KeepLiveService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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
        //定义前台服务的默认样式。即标题、描述和图标
        //定义前台服务的通知点击事件
        val foregroundNotification = ForegroundNotification("测试Keep", "描述", R.mipmap.ic_launcher) { context, intent -> }
        //启动保活服务
        //你需要保活的服务，如socket连接、定时任务等，建议不用匿名内部类的方式在这里写
        KeepLive.startWork(this, foregroundNotification, onWorkingCall = {
            Log.e("aaaddd", "onWorking")
            job?.cancel()
            job = GlobalScope.launch {
                while (true) {
                    count++
                    Log.e("bbbbb", "${count}")
                    delay(1000)
                }
            }

        }, onStopCall = {
            Log.e("aaaddd", "onStop")
            job?.cancel()
        }
        )
    }
}