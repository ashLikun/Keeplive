package com.ashlikun.keeplive.config

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.*

/**
 * @author　　: 李坤
 * 创建时间: 2022/4/29 23:56
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：通知的工具
 */

class NotificationUtils private constructor(private val context: Context) : ContextWrapper(context) {
    private val manager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    private val id: String = context.packageName
    private val name: String = context.packageName
    private var channel: NotificationChannel? = null

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        if (channel == null) {
            channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            channel!!.enableVibration(false)
            channel!!.enableLights(false)
            channel!!.enableVibration(false)
            channel!!.vibrationPattern = longArrayOf(0)
            channel!!.setSound(null, null)
            manager!!.createNotificationChannel(channel!!)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getChannelNotification(title: String?, content: String?, icon: Int, intent: Intent?): Notification.Builder {
        //PendingIntent.FLAG_UPDATE_CURRENT 这个类型才能传值
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return Notification.Builder(context, id)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(icon)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }

    fun getNotification_25(title: String?, content: String?, icon: Int, intent: Intent?): NotificationCompat.Builder {
        //PendingIntent.FLAG_UPDATE_CURRENT 这个类型才能传值
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(context, id)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(icon)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0))
            .setContentIntent(pendingIntent)
    }

    companion object {
        fun sendNotification(context: Context, title: String, content: String, icon: Int, intent: Intent) {
            val notificationUtils = NotificationUtils(context)
            var notification: Notification? = null
            notification = if (Build.VERSION.SDK_INT >= 26) {
                notificationUtils.createNotificationChannel()
                notificationUtils.getChannelNotification(title, content, icon, intent).build()
            } else {
                notificationUtils.getNotification_25(title, content, icon, intent).build()
            }
            notificationUtils.manager!!.notify(Random().nextInt(10000), notification)
        }

        @JvmStatic
        fun createNotification(context: Context, title: String, content: String, icon: Int, intent: Intent): Notification {
            val notificationUtils = NotificationUtils(context)
            var notification: Notification? = null
            notification = if (Build.VERSION.SDK_INT >= 26) {
                notificationUtils.createNotificationChannel()
                notificationUtils.getChannelNotification(title, content, icon, intent).build()
            } else {
                notificationUtils.getNotification_25(title, content, icon, intent).build()
            }
            return notification
        }
    }

}