package com.ashlikun.keeplive.simple

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import com.ashlikun.keeplive.KeepLive
import com.ashlikun.keeplive.config.ForegroundNotification
import com.ashlikun.keeplive.utils.ServiceUtils
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

        KeepLive.init { context, intent ->
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
        KeepLive.isCheckStart = false
        KeepLive.onWorkingCall = {
            Log.e("aaaa", "启动111")
            if (appRunStatus(this, false) == 2) {
                activityStart++
                Log.e("aaaa", "启动")
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }

//        //定义前台服务的默认样式。即标题、描述和图标
//        //定义前台服务的通知点击事件
//        val foregroundNotification = ForegroundNotification("测试Keep", "描述", R.mipmap.ic_launcher) { context, intent -> }
//        //启动保活服务
//        //你需要保活的服务，如socket连接、定时任务等，建议不用匿名内部类的方式在这里写
//        KeepLive.startWork(this, foregroundNotification)
    }


    fun appRunStatus(context: Context, ismoveTaskToFront: Boolean = true, ignoreTaskAffinitys: Array<String> = emptyArray()): Int {

        //处于后台
        var isHoutTai = false
        //获取ActivityManager
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val activities = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES).activities
        //获得当前运行的task,反转是为了对应app的任务栈顺序
        am.getRunningTasks(100)?.reversed()?.forEach {

            //找到当前应用的task，并启动task的栈顶activity，达到程序切换到前台
            it.describeContents()
            if (it.topActivity?.packageName == context.packageName) {
                //这里移动全部的任务栈
                if (ismoveTaskToFront) {
                    am.moveTaskToFront(it.id, 0)
                }
                //后台
                val actInfo = activities.find { itt -> itt.name == it.topActivity?.className }
                if (actInfo?.taskAffinity != "jpush.custom" && ignoreTaskAffinitys.find { actInfo?.taskAffinity == it } == null) {
                    isHoutTai = true
                }
            }
        }
        //若没有找到运行的task，用户结束了task或被系统释放，则重新启动mainactivity
        //未启动
        return if (isHoutTai) 1 else 2
    }
}