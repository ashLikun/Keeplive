package com.ashlikun.keeplive

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import com.ashlikun.keeplive.config.ForegroundNotification
import com.ashlikun.keeplive.config.KeepLiveService
import com.ashlikun.keeplive.service.JobHandlerService
import com.ashlikun.keeplive.service.LocalService
import com.ashlikun.keeplive.service.RemoteService

/**
 * 保活工具
 */
typealias KeepLiveCall = () -> Unit

object KeepLive {
    const val RECEIVER_KEEP_STOP = "_KEEP_STOP"

    //保活是否运行
    var isStart = false

    var foregroundNotification: ForegroundNotification? = null
    var notificationId = 13691

    /**
     * 运行中
     * 由于服务可能会多次自动启动，该方法可能重复调用
     */
    var onWorkingCall: KeepLiveCall? = null

    /**
     * 服务终止
     * 由于服务可能会被多次终止，该方法可能重复调用，需同onWorking配套使用，如注册和注销
     */
    var onStopCall: KeepLiveCall? = null

    //是否启用无声音乐  * 如不设置，则默认启用
    var useSilenceMusice = false

    //音乐是否重复播放流氓模式  相对耗电，但可造就不死之身
    var musiceReplay = false

    //是否开启像素保活
    var onepx = false

    //是否开启守护进程
    var remoteEnable = true

    /**
     * 启动保活
     *
     * @param application            your application
     * @param foregroundNotification 前台服务 必须要，安卓8.0后必须有前台通知才能正常启动Service
     * @param keepLiveService        保活业务
     */
    fun startWork(application: Application,
        foregroundNotification: ForegroundNotification,
        onWorkingCall: KeepLiveCall? = null,
        onStopCall: KeepLiveCall? = null) {
        if (isMain(application)) {
            isStart = true
            KeepLive.foregroundNotification = foregroundNotification
            KeepLive.onWorkingCall = onWorkingCall
            KeepLive.onStopCall = onStopCall
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //启动定时器，在定时器中启动本地服务和守护进程
                val intent = Intent(application, JobHandlerService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    application.startForegroundService(intent)
                } else {
                    application.startService(intent)
                }
            } else {
                //启动本地服务
                application.startService(Intent(application, LocalService::class.java))
                if (remoteEnable) {
                    //启动守护进程
                    application.startService(Intent(application, RemoteService::class.java))
                }
            }
        }
    }

    /**
     * 停止保活
     *
     * @param application            your application
     * @param foregroundNotification 前台服务 必须要，安卓8.0后必须有前台通知才能正常启动Service
     * @param keepLiveService        保活业务
     */
    fun stopWork(application: Application) {
        if (isMain(application)) {
            isStart = false
            //发送退出广播
            application.sendBroadcast(Intent(RECEIVER_KEEP_STOP))
        }
    }

    private fun isMain(application: Application): Boolean {
        val pid = Process.myPid()
        var processName = ""
        val mActivityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcessInfos = mActivityManager.runningAppProcesses
        if (runningAppProcessInfos != null) {
            for (appProcess in mActivityManager.runningAppProcesses) {
                if (appProcess.pid == pid) {
                    processName = appProcess.processName
                    break
                }
            }
            val packageName = application.packageName
            if (processName == packageName) {
                return true
            }
        }
        return false
    }
}