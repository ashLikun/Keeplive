package com.ashlikun.keeplive.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.lang.Runnable
import com.ashlikun.keeplive.activity.OnePixelActivity
import android.app.PendingIntent
import android.os.Handler
import java.lang.Exception
import android.os.Looper
import com.ashlikun.keeplive.KeepLive

/**
 * @author　　: 李坤
 * 创建时间: 2022/4/29 23:58
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：一个像素的广播,主要监听屏幕关闭和开启
 * 屏幕开启：关闭一像素和关闭音乐
 * 屏幕关闭：开启一像素和音乐
 */

class OnepxReceiver : BroadcastReceiver() {
    var mHander: Handler = Handler(Looper.getMainLooper())
    var screenOn = true
    override fun onReceive(context: Context, intent: Intent) {
        //屏幕关闭的时候接受到广播
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            screenOn = false
//            if (KeepLive.onepx) {
//                mHander.postDelayed({
//                    if (!screenOn) {
//                        try {
//                            PendingIntent.getActivity(context, 0,
//                                Intent(context, OnePixelActivity::class.java).apply {
//                                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                                }, 0).send()
//                            /*} catch (PendingIntent.CanceledException e) {*/
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                }, 1000)
//            }
            //通知屏幕已关闭，开始播放无声音乐
            context.sendBroadcast(Intent("_ACTION_SCREEN_OFF"))
        } else if (intent.action == Intent.ACTION_SCREEN_ON) {
            //屏幕打开的时候发送广播  结束一像素
            screenOn = true
            //通知屏幕已点亮，停止播放无声音乐
            context.sendBroadcast(Intent("_ACTION_SCREEN_ON"))
        }
    }

}