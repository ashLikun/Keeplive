package com.ashlikun.keeplive

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import java.util.concurrent.atomic.AtomicInteger

/**
 * 作者　　: 李坤
 * 创建时间: 2022/4/30　10:00
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：
 */
/**
 * 新的启动api
 * @param contract [ActivityResultContract.StartActivityForResult]  or  [ActivityResultContracts.RequestMultiplePermissions()]
 * @param isUnregister 是否返回的时候 释放
 * @param key key的前缀
 */
private fun <I, O> ComponentActivity.registerForActivityResultX(
    contract: ActivityResultContract<I, O>,
    isUnregister: Boolean = true,
    key: String = "KeepLiveForActivityResult",
    callback: (O) -> Unit
): ActivityResultLauncher<I> {
    var launcher: ActivityResultLauncher<I>? = null
    //这种注册需要自己unregister
    launcher = activityResultRegistry.register(key + AtomicInteger().getAndIncrement(), contract) {
        callback.invoke(it)
        //这里主动释放
        if (isUnregister)
            launcher!!.unregister()
    }
    return launcher
}

/**
 * 是否忽略电池优化白名单
 */
private fun Context.isIgnoringBatteryOptimizations() = runCatching {
    val pm = applicationContext.getSystemService(Activity.POWER_SERVICE) as PowerManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        pm.isIgnoringBatteryOptimizations(packageName)
    } else true
}.getOrNull() ?: true

/**
 * 忽略电池优化白名单
 * @param isToList true:跳转到设置里面的电池优化管理列表,false:弹窗形式申请电池优化白名单
 * @param hook 在启动弹窗或者跳转页面前，允许插入你的代码
 * @param result 结果
 */
internal fun ComponentActivity.ignoreBattery(isToList: Boolean = false, hook: ((run: () -> Unit) -> Unit)? = null, result: ((Boolean) -> Unit)? = null) {
    runCatching {
        if (isIgnoringBatteryOptimizations()) result?.invoke(true)
        else {
            fun start() = registerForActivityResultX(ActivityResultContracts.StartActivityForResult()) {
                //请求结果
                result?.invoke(isIgnoringBatteryOptimizations())
            }.launch(if (isToList) Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS) else Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            })
            hook?.invoke(::start).apply {
                if (hook == null) start()
            }
        }
    }.getOrElse {
        it.printStackTrace()
        result?.invoke(false)
    }
}

/**
 * 启动自启动白名单
 * 可能有的手机没有
 * @return 是否启动成功
 */
fun Context.startAutostartSetting(): Boolean {
    runCatching {
        startActivity(getAutostartSettingIntent())
        return true
    }.getOrElse {
        it.printStackTrace()
        return false
    }
}


/**
 * 获取自启动管理页面的Intent
 * @return 返回自启动管理页面的Intent
 */
fun Context.getAutostartSettingIntent(): Intent {
    var componentName: ComponentName? = null
    val brand = Build.MANUFACTURER
    val intent = Intent()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    when {
        brand.toLowerCase() == "samsung" -> componentName = ComponentName(
            "com.samsung.android.sm",
            "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity"
        )

        brand.toLowerCase() == "yulong" || brand.toLowerCase() == "360" -> componentName =
            ComponentName(
                "com.yulong.android.coolsafe",
                "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity"
            )
        brand.toLowerCase() == "oneplus" -> componentName = ComponentName(
            "com.oneplus.security",
            "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
        )
        brand.toLowerCase() == "letv" -> {
            intent.action = "com.letv.android.permissionautoboot"
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package", packageName, null)
        }
        IfRom.isHuawei ->             //荣耀V8，EMUI 8.0.0，Android 8.0上，以下两者效果一样
            componentName = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )
        IfRom.isXiaomi -> componentName = ComponentName(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
        IfRom.isVivo -> //            componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.safaguard.PurviewTabActivity");
            componentName = ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
            )
        IfRom.isOppo -> //            componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
            componentName = ComponentName(
                "com.coloros.oppoguardelf",
                "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"
            )
        IfRom.isMeizu -> componentName =
            ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity")
        else -> {
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package", packageName, null)
        }
    }
    intent.component = componentName
    return intent
}