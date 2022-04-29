package com.ashlikun.keeplive.utils

import android.app.ActivityManager
import android.content.Context

object ServiceUtils {
    fun isServiceRunning(ctx: Context, className: String): Boolean {
        var isRunning = false
        val activityManager = ctx
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val servicesList = activityManager.getRunningServices(Int.MAX_VALUE)
        if (servicesList != null) {
            val l: Iterator<ActivityManager.RunningServiceInfo> = servicesList.iterator()
            while (l.hasNext()) {
                val si = l.next()
                if (className == si.service.className) {
                    isRunning = true
                }
            }
        }
        return isRunning
    }

    fun isRunningTaskExist(context: Context, processName: String): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processList = am.runningAppProcesses
        if (processList != null) {
            for (info in processList) {
                if (info.processName == processName) {
                    return true
                }
            }
        }
        return false
    }
}