package com.ashlikun.keeplive.simple

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventCallback
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ashlikun.keeplive.KeepLive
import com.ashlikun.keeplive.utils.ServiceUtils
import kotlinx.coroutines.*
import java.util.*

/**
 * 作者　　: 李坤
 * 创建时间: 2022/10/28　22:44
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：
 */
class Test {
    companion object {
        private val instance by lazy { Test() }
        fun get(): Test = instance
    }

    val sensorManager by lazy {
        application!!.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
    }
    val stepDetector by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    }
    var count = 0
    var steps = 0
    var application: MyApp? = null

    val handle by lazy {
        Handler(Looper.getMainLooper())
    }
    private val manager by lazy {
        application!!.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
    }
    val sensorCall by lazy {
        @RequiresApi(Build.VERSION_CODES.N)
        object : SensorEventCallback() {
            override fun onSensorChanged(event: SensorEvent) {
                super.onSensorChanged(event)
                if (event.values[0] == 1.0f) {
                    steps++
                }
                Log.i("Test", "Detected step changes:" + event.values[0]);

            }
        }
    }
    var call: ((String) -> Unit)? = null
    val run by lazy {
        Runnable {
            count++
            val showText = "运行了：${count} S   ${application!!.activityStart}    ${application!!.appStart}  步数：${steps}"
            //                ServiceUtils.isRunningTaskExist2(application!!, application!!.packageName)
            KeepLive.createNot(application!!)?.apply {
                setContentText(showText)
                manager.notify(KeepLive.notificationId, this.build())
            }
            //            withContext(Dispatchers.Main) {
            call?.invoke(showText)
            //            }
            //                binding.textView2.post {
            //                    binding.textView2.text = "开始时间：${startTime}\n" + "当前时间：${getFormatTime(Calendar.getInstance())}" + "\n" + "运行了：${count} S"
            //                }
            Log.e("aaaaa", "${count}")
            reRun()
        }
    }

    private fun reRun() {
        handle.postDelayed(run,1000)
    }

    fun start(call: (String) -> Unit): Unit {
        this.call = call
        if (Build.VERSION.SDK_INT >= 29) {
//            sensorManager.registerListener(sensorCall, stepDetector, 1000000)
        }
//        handle.post(run)
        GlobalScope.launch {
            while (true) {
                count++
                val showText = "运行了：${count} S   ${application!!.activityStart}    ${application!!.appStart}  步数：${steps}"
//                ServiceUtils.isRunningTaskExist2(application!!, application!!.packageName)
//                KeepLive.createNot(application!!)?.apply {
//                    setContentText(showText)
//                    manager.notify(KeepLive.notificationId, this.build())
//                }
                withContext(Dispatchers.Main) {
                    call(showText)
                }
//                binding.textView2.post {
//                    binding.textView2.text = "开始时间：${startTime}\n" + "当前时间：${getFormatTime(Calendar.getInstance())}" + "\n" + "运行了：${count} S"
//                }
                Log.e("aaaaa", "${count}")
                delay(1000)
            }
        }
    }

    fun init(application: MyApp) {
        this.application = application;
    }
}