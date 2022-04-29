package com.ashlikun.keeplive.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.ashlikun.keeplive.KeepLive
import com.ashlikun.keeplive.config.NotificationUtils.Companion.createNotification
import com.ashlikun.keeplive.receiver.NotificationClickReceiver

/**
 * @author　　: 李坤
 * 创建时间: 2022/4/30 0:01
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍： 隐藏前台服务通知
 * Build.VERSION.SDK_INT < 25 才有效
 */
class HideForegroundService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground()
        handler.postDelayed({
            stopForeground(true)
            stopSelf()
        }, 2000)
        return START_NOT_STICKY
    }

    private fun startForeground() {
        if (KeepLive.foregroundNotification != null) {
            val intent = Intent(applicationContext, NotificationClickReceiver::class.java)
            intent.action = NotificationClickReceiver.CLICK_NOTIFICATION
            val notification = createNotification(this,
                KeepLive.foregroundNotification!!.title,
                KeepLive.foregroundNotification!!.description,
                KeepLive.foregroundNotification!!.iconRes,
                intent)
            startForeground(KeepLive.notificationId, notification)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}