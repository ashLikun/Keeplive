//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package com.ashlikun.keeplive.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.*
import android.os.Build
import android.os.Build.VERSION
import android.util.Log
import androidx.annotation.RequiresApi
import com.ashlikun.keeplive.KeepLive
import com.ashlikun.keeplive.utils.ServiceUtils

/**
 * @author　　: 李坤
 * 创建时间: 2022/4/29 21:03
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：启动定时器，在定时器中启动本地服务和守护进程
 */
@RequiresApi(api = 21)
class JobHandlerService : JobService() {
    private var mJobScheduler: JobScheduler? = null
    val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e("aaa","JobHandlerService 我接收了关闭通知")
            stop()
        }
    }

    fun stop() {
        runCatching {
            mJobScheduler?.cancel(KeepLive.jobId)
            mJobScheduler = null
            if (VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(KeepLive.notificationId)
            }
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.startService(this)
        Log.e("JobHandlerService ", "onStartCommand")
        registerReceiver(stopReceiver, IntentFilter().apply {
            addAction(KeepLive.RECEIVER_KEEP_STOP)
        })
        if (VERSION.SDK_INT >= 21) {
            mJobScheduler = this.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
            mJobScheduler!!.cancel(KeepLive.jobId)
            val builder = JobInfo.Builder(KeepLive.jobId, ComponentName(this.packageName, JobHandlerService::class.java.name))
            if (VERSION.SDK_INT >= 24) {
                //  表示任务在至少多长时间后执行，不能和setPeriodic 同时使用，否则报错
                builder.setMinimumLatency(30000L)
                //表示任务在某段时间后必须执行 ，不能和setPeriodic 同时使用，否则报错
                builder.setOverrideDeadline(30000L)
                builder.setMinimumLatency(30000L)
                //  设置等待多长时间执行，默认是10s，如果小于10s，按照10s算
                builder.setBackoffCriteria(30000L, JobInfo.BACKOFF_POLICY_LINEAR)
            } else {
                //表示任务间隔多长时间执行
                builder.setPeriodic(30000L)
            }
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            builder.setPersisted(true)
            mJobScheduler!!.schedule(builder.build())
        }
        return START_STICKY
    }

    private fun startService(context: Context) {
        if (KeepLive.isStart) {
            Log.e("JobHandlerService ", "startService")
            //启用前台服务，提升优先级
            KeepLive.createNot(this)?.apply {
                startForeground(KeepLive.notificationId, build())
            }
            this.startService(Intent(context, LocalService::class.java))
            if (KeepLive.remoteEnable)
                this.startService(Intent(context, RemoteService::class.java))
        } else {
            if (KeepLive.isCheckStart) {
                stop()
            }
        }
    }

    override fun onStartJob(jobParameters: JobParameters?): Boolean {
        if (!ServiceUtils.isServiceRunning(this.applicationContext, LocalService::class.java.name) ||
            (KeepLive.remoteEnable && !ServiceUtils.isRunningTaskExist(this.applicationContext, this.packageName + ":remote"))
        ) {
            this.startService(this)
        }
        return false
    }

    override fun onStopJob(jobParameters: JobParameters?): Boolean {
        if (!ServiceUtils.isServiceRunning(this.applicationContext, LocalService::class.java.name) ||
            (KeepLive.remoteEnable && !ServiceUtils.isRunningTaskExist(this.applicationContext, this.packageName + ":remote"))
        ) {
            this.startService(this)
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching {
            unregisterReceiver(stopReceiver)
        }
    }
}