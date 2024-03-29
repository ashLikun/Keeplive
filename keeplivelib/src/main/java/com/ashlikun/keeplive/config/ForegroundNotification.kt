package com.ashlikun.keeplive.config

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import java.io.Serializable

/**
 * @author　　: 李坤
 * 创建时间: 2022/4/29 23:52
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：默认前台服务样式
 */

data class ForegroundNotification(
    var title: String = "",
    var description: String = "",
    var iconRes: Int = 0,
    //优先级
    var importance: Int = NotificationManager.IMPORTANCE_MIN,
) :
    Serializable {
}