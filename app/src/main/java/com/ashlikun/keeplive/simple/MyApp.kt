package com.ashlikun.keeplive.simple

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.ashlikun.keeplive.KeepLive
import com.ashlikun.keeplive.config.ForegroundNotification
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
    var activityStart = 0
    var appStart = 0
    var activitys = mutableListOf<Activity>()
    override fun onCreate() {
        super.onCreate()
        appStart++
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (!activitys.contains(activity))
                    activitys.add(activity)
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                activitys.remove(activity)
            }


        })
        Test.get().init(this)
        KeepLive.onWorkingCall = {
            if (activitys.getOrNull(0)?.isDestroyed != false) {
                activityStart++
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            }
        }
        KeepLive.startWork(this, ForegroundNotification("测试Keep", "描述", R.mipmap.ic_launcher, clickListener = { context, intent ->
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }))
        Test.get().start()
//        //定义前台服务的默认样式。即标题、描述和图标
//        //定义前台服务的通知点击事件
//        val foregroundNotification = ForegroundNotification("测试Keep", "描述", R.mipmap.ic_launcher) { context, intent -> }
//        //启动保活服务
//        //你需要保活的服务，如socket连接、定时任务等，建议不用匿名内部类的方式在这里写
//        KeepLive.startWork(this, foregroundNotification)
    }
}