package com.ashlikun.keeplive.config

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.ashlikun.keeplive.KeepLive

/**
 * @author　　: 李坤
 * 创建时间: 2022/4/29 23:56
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：通知的工具
 */

class KeepNotificationUtils private constructor(private val context: Context) : ContextWrapper(context) {
    private val manager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    private val id: String = notId ?: "Keep:" + context.packageName
    private val name: String = notName ?: "Keep:" + context.packageName
    private var channel: NotificationChannel? = null

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        if (channel == null) {
            //运行后台优先级不需要高
            channel = NotificationChannel(id, name, KeepLive.foregroundNotification?.importance ?: NotificationManager.IMPORTANCE_MIN)
                .apply {
                    setShowBadge(false)
                    enableVibration(false)
                    enableLights(false)
                    enableVibration(false)
                    vibrationPattern = longArrayOf(0)
                    setSound(null, null)
                    manager!!.createNotificationChannel(this)
                }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getChannelNotification(title: String, content: String, icon: Int, intent: Intent): NotificationCompat.Builder {
        //PendingIntent.FLAG_UPDATE_CURRENT 这个类型才能传值
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        else PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(context, id)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(icon)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }

    fun getNotification_25(title: String, content: String, icon: Int, intent: Intent): NotificationCompat.Builder {
        //PendingIntent.FLAG_UPDATE_CURRENT 这个类型才能传值
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
        else PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(context, id)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(icon)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVibrate(longArrayOf(0))
            .setContentIntent(pendingIntent)
    }

    companion object {
        var notId: String? = null
        var notName: String? = null

        @JvmStatic
        fun createNotification(context: Context, title: String, content: String, icon: Int, intent: Intent): NotificationCompat.Builder {
            val notificationUtils = KeepNotificationUtils(context)
            var notification: NotificationCompat.Builder? = null
            notification = if (Build.VERSION.SDK_INT >= 26) {
                notificationUtils.createNotificationChannel()
                notificationUtils.getChannelNotification(title, content, icon, intent)
            } else {
                notificationUtils.getNotification_25(title, content, icon, intent)
            }
            KeepLive.createNotificationCall?.invoke(notification)
            return notification
        }
    }

}