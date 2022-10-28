package com.ashlikun.keeplive.simple

import android.app.NotificationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ashlikun.keeplive.KeepLive
import com.ashlikun.keeplive.config.ForegroundNotification
import com.ashlikun.keeplive.simple.databinding.MainViewgroupActivityBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
            KeepLive.startWork(application, ForegroundNotification("测试Keep", "描述", R.mipmap.ic_launcher))
        }
        binding.textView2.setOnClickListener {
            KeepLive.stopWork(application)
        }

    }

}
