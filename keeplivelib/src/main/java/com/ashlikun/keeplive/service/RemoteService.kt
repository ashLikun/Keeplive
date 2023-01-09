package com.ashlikun.keeplive.service

import android.app.Service
import android.content.*
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.RemoteException
import android.util.Log
import com.ashlikun.keeplive.KeepLive
import com.ashlikun.keeplive.config.KeepNotificationUtils.Companion.createNotification
import com.ashlikun.keeplive.utils.ServiceUtils

/**
 * @author　　: 李坤
 * 创建时间: 2022/4/30 0:03
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：守护进程 双进程保活，6.0之前
 */
class RemoteService : Service() {
    private val mBilder: MyBilder = MyBilder()
    private var mIsBoundLocalService = false
    val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            stop()
        }
    }

    fun stop() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(KeepLive.notificationId)
            }
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBilder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("RemoteService ", "onStartCommand")
        registerReceiver(stopReceiver, IntentFilter().apply {
            addAction(KeepLive.RECEIVER_KEEP_STOP)
        })
        try {
            mIsBoundLocalService = this.bindService(Intent(this@RemoteService, LocalService::class.java), connection, BIND_ABOVE_CLIENT)
        } catch (e: Exception) {
        }
        if (!KeepLive.isStart) {
            if (KeepLive.isCheckStart) {
                stop()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(stopReceiver)
            if (mIsBoundLocalService) {
                unbindService(connection)
            }
        } catch (e: Exception) {
        }
    }

    private inner class MyBilder : GuardAidl.Stub() {
        @Throws(RemoteException::class)
        override fun wakeUp(title: String, discription: String, iconRes: Int) {
            if (Build.VERSION.SDK_INT < 25) {
                val notification = createNotification(this@RemoteService, title, discription, iconRes,
                    KeepLive.notifyClickIntent!!)
                this@RemoteService.startForeground(KeepLive.notificationId, notification.build())
            }
        }
    }

    private val connection = object : ServiceConnection {
        //方法onServiceDisconnected() 在连接正常关闭的情况下是不会被调用的, 该方法只在Service 被破坏了或者被杀死的时候调用. 例如, 系统资源不足, 要关闭一些Services, 刚好连接绑定的 Service 是被关闭者之一,  这个时候onServiceDisconnected() 就会被调用。
        override fun onServiceDisconnected(name: ComponentName?) {
            if (ServiceUtils.isRunningTaskExist(applicationContext, "$packageName:remote")) {
                startService(Intent(this@RemoteService, LocalService::class.java))
                mIsBoundLocalService = bindService(
                    Intent(
                        this@RemoteService,
                        LocalService::class.java
                    ), this, BIND_ABOVE_CLIENT
                )
            }
            val pm = applicationContext.getSystemService(POWER_SERVICE) as PowerManager
            sendBroadcast(Intent(if (pm.isScreenOn) "_ACTION_SCREEN_ON" else "_ACTION_SCREEN_OFF"))
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {}
    }
}