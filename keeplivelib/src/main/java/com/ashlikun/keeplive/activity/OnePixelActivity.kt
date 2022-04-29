package com.ashlikun.keeplive.activity

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.Gravity
import android.view.WindowManager
import android.os.PowerManager
import android.content.Context
import java.lang.Exception
/**
 * @author　　: 李坤
 * 创建时间: 2022/4/29 23:52
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：一个像素Activity
 */

class OnePixelActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设定一像素的activity
        val window = window
        window.setGravity(Gravity.START or Gravity.TOP)
        val params = window.attributes
        params.x = 0
        params.y = 0
        params.height = 1
        params.width = 1
        window.attributes = params
    }

    override fun onResume() {
        super.onResume()
        checkScreenOn("onResume")
    }

    private fun checkScreenOn(methodName: String) {
        try {
            val pm = applicationContext.getSystemService(POWER_SERVICE) as PowerManager
            val isScreenOn = pm.isScreenOn
            if (isScreenOn) {
                finish()
            }
        } catch (e: Exception) {
        }
    }
}