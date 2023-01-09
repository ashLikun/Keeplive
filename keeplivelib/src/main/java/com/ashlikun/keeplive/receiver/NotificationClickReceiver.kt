package com.ashlikun.keeplive.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ashlikun.keeplive.KeepLive

/**
 * @author　　: 李坤
 * 创建时间: 2022/4/29 23:57
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：通知点击的广播
 */

class NotificationClickReceiver : BroadcastReceiver() {
    companion object {
        const val CLICK_NOTIFICATION = "CLICK_NOTIFICATION"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == CLICK_NOTIFICATION) {
            KeepLive.notifyClickCall?.invoke(context, intent)
        }
        Log.e("aaaa","1111111111")
    }
}