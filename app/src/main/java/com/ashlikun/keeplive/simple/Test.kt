package com.ashlikun.keeplive.simple

import android.app.Application
import android.app.NotificationManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ashlikun.keeplive.KeepLive
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * 作者　　: 李坤
 * 创建时间: 2022/10/28　22:44
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：
 */
class Test {
    companion object {
        private val instance by lazy { Test() }
        fun get(): Test = instance
    }

    var count = 0
    var application: MyApp? = null
    private val manager by lazy {
        application!!.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun start(): Unit {
        GlobalScope.launch {
            while (true) {
                count++
                KeepLive.createNot(application!!)?.apply {
                    setContentText("运行了：${count} S   ${application!!.activityStart}    ${application!!.appStart}")
                    manager.notify(KeepLive.notificationId, this.build())
                }
//                binding.textView2.post {
//                    binding.textView2.text = "开始时间：${startTime}\n" + "当前时间：${getFormatTime(Calendar.getInstance())}" + "\n" + "运行了：${count} S"
//                }
                Log.e("aaaaa", "${count}")
                delay(1000)
            }
        }
    }

    fun init(application: MyApp) {
        this.application = application;
    }
}