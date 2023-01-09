package com.ashlikun.keeplive.simple

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager

/**
 * 作者　　: 李坤
 * 创建时间: 2023/1/9　13:41
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：透明的 Activity 。 所有与通知相关的工作都在透明 Activity 中处理
 * 参考JNotifyActivity
 */
class NotifyActivity : Activity() {
    companion object {
        var onHandler: ((context: Context, intent: Intent) -> Unit)? = null
    }

    override fun onCreate(bundle: Bundle?) {
        runCatching {
            super.onCreate(bundle)
            //防止截屏录屏
            this.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            handleIntent(this.intent)
        }.onFailure {
            finish()
        }
    }

    override fun onNewIntent(var1: Intent?) {
        runCatching {
            super.onNewIntent(var1)
            handleIntent(var1)
        }.onFailure {
            finish()
        }
    }

    private fun handleIntent(intent: Intent?) {
        runCatching {
            if (intent != null) {
                onHandler?.invoke(this.applicationContext, intent)
            }
        }.onFailure {
            //handle intent failed:
            it.printStackTrace()
        }
        finish()
    }
}