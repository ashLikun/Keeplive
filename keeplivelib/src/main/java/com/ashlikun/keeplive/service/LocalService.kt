package com.ashlikun.keeplive.service

import android.app.Service
import android.content.*
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import com.ashlikun.keeplive.KeepLive
import com.ashlikun.keeplive.R
import com.ashlikun.keeplive.receiver.OnepxReceiver
import com.ashlikun.keeplive.utils.ServiceUtils

/**
 * @author　　: 李坤
 * 创建时间: 2022/4/29 20:58
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：本地进程
 */
class LocalService : Service() {

    //像素保活
    private val mOnepxReceiver = OnepxReceiver()

    //屏幕点亮状态监听，用于单独控制音乐播放
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action == "_ACTION_SCREEN_OFF") {
                isPause = false
                play()
            } else if (intent?.action == "_ACTION_SCREEN_ON") {
                isPause = true
                pause()
            }
        }
    }
    private var isPause = true //控制暂停
    private var mediaPlayer: MediaPlayer? = null
    private var mBilder = MyBilder()
    private var handler = Handler(Looper.getMainLooper())
    private var mIsBoundRemoteService = false
    override fun onCreate() {
        super.onCreate()
        val pm = applicationContext.getSystemService(POWER_SERVICE) as PowerManager
        isPause = pm.isScreenOn
        Log.e("LocalService", "onCreate")
    }

    val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e("aaa", "LocalService 我接收了关闭通知")
            stop()
        }
    }

    fun stop() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(KeepLive.notificationId)
            }
        }
        runCatching { stopSelf() }
        runCatching {
            pause()
            handler.removeCallbacksAndMessages(null)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.e("LocalService", "onBind")
        return mBilder
    }

    private fun playMusice() {
        if (KeepLive.useSilenceMusice) {
            //播放无声音乐
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.silent)
                if (mediaPlayer != null) {
                    mediaPlayer!!.setVolume(0f, 0f)
                    mediaPlayer!!.setOnCompletionListener { mediaPlayer: MediaPlayer? ->
                        if (!isPause) {
                            if (KeepLive.musiceReplay) {
                                play()
                            } else {
                                handler.postDelayed({ play() }, KeepLive.musiceDelety)
                            }
                        }
                    }
                    mediaPlayer!!.setOnErrorListener { mp: MediaPlayer?, what: Int, extra: Int ->
                        playMusice()
                        false
                    }
                    play()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("LocalService", "onStartCommand")
        registerReceiver(stopReceiver, IntentFilter().apply {
            addAction(KeepLive.RECEIVER_KEEP_STOP)
        })
        //播放无声音乐
        playMusice()
        //像素保活
        registerReceiver(mOnepxReceiver, IntentFilter().apply {
            addAction("android.intent.action.SCREEN_OFF")
            addAction("android.intent.action.SCREEN_ON")
        })
        //屏幕点亮状态监听，用于单独控制音乐播放
        registerReceiver(screenStateReceiver, IntentFilter().apply {
            addAction("_ACTION_SCREEN_OFF")
            addAction("_ACTION_SCREEN_ON")
        })
        //启用前台服务，提升优先级
        KeepLive.createNot(this)?.apply {
            startForeground(KeepLive.notificationId, build())
        }
        //绑定守护进程
        if (KeepLive.remoteEnable) {
            try {
                mIsBoundRemoteService = this.bindService(Intent(this, RemoteService::class.java), connection, BIND_ABOVE_CLIENT)
            } catch (e: Exception) {
            }
        }
        if (KeepLive.isCheckStart) {
            if (KeepLive.isStart) {
                KeepLive.onWorkingCall?.invoke(this)
            } else {
                stop()
            }
        } else {
            KeepLive.onWorkingCall?.invoke(this)
        }
        return START_STICKY
    }

    private fun play() {
        if (KeepLive.useSilenceMusice) {
            if (mediaPlayer?.isPlaying == false) {
                Log.e("LocalService", "播放音乐")
                mediaPlayer?.start()
            }
        }
    }

    private fun pause() {
        if (KeepLive.useSilenceMusice) {
            if (mediaPlayer?.isPlaying == true) {
                Log.e("LocalService", "暂停播放音乐")
                mediaPlayer?.pause()
            }
        }
    }


    private inner class MyBilder : GuardAidl.Stub() {
        @Throws(RemoteException::class)
        override fun wakeUp(title: String, discription: String, iconRes: Int) {
        }
    }

    private val connection = object : ServiceConnection {
        //方法onServiceDisconnected() 在连接正常关闭的情况下是不会被调用的, 该方法只在Service 被破坏了或者被杀死的时候调用. 例如, 系统资源不足, 要关闭一些Services, 刚好连接绑定的 Service 是被关闭者之一,  这个时候onServiceDisconnected() 就会被调用。
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e("LocalService", "onServiceDisconnected")
            if (KeepLive.remoteEnable && ServiceUtils.isServiceRunning(applicationContext, LocalService::class.java.name)) {
                startService(Intent(this@LocalService, RemoteService::class.java))
                mIsBoundRemoteService = bindService(Intent(this@LocalService, RemoteService::class.java), this, BIND_ABOVE_CLIENT)
            }
            val pm = applicationContext.getSystemService(POWER_SERVICE) as PowerManager
            sendBroadcast(Intent(if (pm.isScreenOn) "_ACTION_SCREEN_ON" else "_ACTION_SCREEN_OFF"))
        }

        //服务开启
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e("LocalService", "onServiceConnected")
            try {
                KeepLive.foregroundNotification?.apply {
                    val guardAidl = GuardAidl.Stub.asInterface(service)
                    guardAidl.wakeUp(title, description, iconRes)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching {
            pause()
        }
        Log.e("LocalService", "onDestroy")
        try {
            if (mIsBoundRemoteService) {
                unbindService(connection)
            }
        } catch (e: Exception) {
        }
        try {
            unregisterReceiver(stopReceiver)
            unregisterReceiver(mOnepxReceiver)
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
        }
        KeepLive.onStopCall?.invoke(this)
    }
}