package com.ashlikun.keeplive.simple

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ashlikun.keeplive.KeepLive
import com.ashlikun.keeplive.config.ForegroundNotification
import com.ashlikun.keeplive.simple.databinding.MainViewgroupActivityBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    val binding by lazy {
        MainViewgroupActivityBinding.inflate(layoutInflater)
    }

    fun getFormatTime(calendar: Calendar?, format: String = "yyyy-MM-dd HH:mm:ss") =
        try {
            if (calendar == null) ""
            else SimpleDateFormat(format, Locale.getDefault()).format(calendar.time)
        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }


    var count = 0
    var startTime = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(binding.root)
        startTime = getFormatTime(Calendar.getInstance())
        binding.textView.text = "开始时间：${startTime}"

        binding.textView.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 29) {
                // Android10.0之后使用计步器需要健身运动权限
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                    KeepLive.startWork(application, ForegroundNotification("测试Keep", "描述", R.mipmap.ic_launcher))
                    Test.get().start {
                        binding.textView.text = it
                    }
                } else {
                    requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 111)
                }
            }

        }
        binding.textView2.setOnClickListener {
            KeepLive.stopWork(application)
        }
//        KeepLive.startWork(application, ForegroundNotification("测试Keep", "描述", R.mipmap.ic_launcher))
//        Test.get().start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111 && grantResults.find { it != PackageManager.PERMISSION_GRANTED } == null) {
            KeepLive.startWork(application, ForegroundNotification("测试Keep", "描述", R.mipmap.ic_launcher))
            Test.get().start {
                binding.textView.text = it
            }
        }
    }

}
