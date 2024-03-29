package com.ashlikun.keeplive

import android.app.ActivityManager
import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import com.ashlikun.keeplive.config.ForegroundNotification
import com.ashlikun.keeplive.config.KeepNotificationUtils
import com.ashlikun.keeplive.service.JobHandlerService
import com.ashlikun.keeplive.service.LocalService
import com.ashlikun.keeplive.service.RemoteService

/**
 * 保活工具
 */
typealias KeepLiveCall = (service: Service) -> Unit

object KeepLive {
    const val RECEIVER_KEEP_STOP = "_KEEP_STOP"
    const val NOTIFY_TYPE = "KEEP_NOTIFY_TYPE"

    //保活是否运行
    var isStart = false

    var foregroundNotification: ForegroundNotification? = null

    /**
     * 通知栏点击的回调
     * 这个放在Application里面，才能被回调
     */
    var notifyClickIntent: Intent? = null
        private set(value) {
            value?.action = NOTIFY_TYPE
            field = value
        }

    //创建前提通知时候的回调，方便外部进行特殊设置
    var createNotificationCall: ((NotificationCompat.Builder) -> Unit)? = null

    //通知id
    var notificationId = 13691

    //JobScheduler 的id
    var jobId = 121

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
    var useSilenceMusice = true

    //音乐是否重复播放流氓模式  相对耗电，但可造就不死之身
    var musiceReplay = true

    //间隔多久播放下一次
    var musiceDelety = 5000L

    //一个像素
    var onepx = true


    //是否开启守护进程 双进程保活，6.0之前
    var remoteEnable = false

    /**
     * 每次服务创建是否检测start状态
     * 如果不是start，就停止服务
     */
    var isCheckStart = true

    /**
     * 是否启动定时器服务
     * 这里如果用户开启APP后台运行，那么APP会永远关不了(没有页面，但是有服务在运行)
     */
    var isStartJobHandler = false

    /**
     * 在Application 初始化
     */
    fun init(notifyClickIntent: Intent) {
        this.notifyClickIntent = notifyClickIntent
    }

    /**
     * 创建foregroundNotification 对应的通知栏Builder
     * 前提foregroundNotification 不为null
     */
    fun createNot(context: Context) =
        if (foregroundNotification == null) null else KeepNotificationUtils.createNotification(
            context,
            foregroundNotification!!.title,
            foregroundNotification!!.description,
            foregroundNotification!!.iconRes,
            notifyClickIntent!!
        )

    /**
     * 启动保活
     * 1:先检查是否可以关闭电池优化
     * 2：未关闭就使用备选的方案，一像素，守护进程，无声音乐
     * 3：启动后要在结束的地方调用 stopWork 方法
     */
    fun startIgnoreBattery(activity: ComponentActivity, foregroundNotification: ForegroundNotification) {
        activity.ignoreBattery {
            startWork(activity.application, foregroundNotification)
        }
    }

    /**
     * 启动保活
     * 未打开电池优化或者自启动的使用这个方法
     *
     * @param application            your application
     * @param foregroundNotification 前台服务 必须要，安卓8.0后必须有前台通知才能正常启动Service
     * @param keepLiveService        保活业务
     */
    fun startWork(
        application: Application,
        foregroundNotification: ForegroundNotification
    ) {
        Log.e("KeepLive", "startWork")
        if (isMain(application)) {
            isStart = true
            KeepLive.foregroundNotification = foregroundNotification
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isStartJobHandler) {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isStartJobHandler) {
                //启动定时器，在定时器中启动本地服务和守护进程
                val intent = Intent(application, JobHandlerService::class.java)
                application.stopService(intent)
            } else {
                //启动本地服务
                application.stopService(Intent(application, LocalService::class.java))
                if (remoteEnable) {
                    //启动守护进程
                    application.stopService(Intent(application, RemoteService::class.java))
                }
            }
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