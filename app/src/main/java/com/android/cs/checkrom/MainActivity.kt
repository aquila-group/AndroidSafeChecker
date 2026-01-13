package com.android.cs.checkrom

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.cs.checkrom.core.appCheck
import com.android.cs.checkrom.core.appCheck1
import com.android.cs.checkrom.core.cameraCheck
import com.android.cs.checkrom.core.checkAllDensityInfo
import com.android.cs.checkrom.core.checkBluetooth
import com.android.cs.checkrom.core.checkMemInfo
import com.android.cs.checkrom.core.checkProp
import com.android.cs.checkrom.core.checkScreenDensity
import com.android.cs.checkrom.core.checkScreenInfo
import com.android.cs.checkrom.core.checkSystemFeature
import com.android.cs.checkrom.core.checkVirtual
import com.android.cs.checkrom.databinding.ActivityMainBinding
import com.android.cs.checkrom.entry.CheckEntry
import com.android.cs.checkrom.ui.item.ItemAdapter
import com.android.cs.checkrom.utils.GlideUtils
import com.android.cs.checkrom.utils.NativeUtils.checkDockerenv
import com.android.cs.checkrom.utils.NativeUtils.checkInitProcess
import com.android.cs.checkrom.utils.NativeUtils.checkMountinfo
import com.android.cs.checkrom.utils.NativeUtils.checkSystemPropsModified
import com.android.cs.checkrom.utils.NativeUtils.isContainerByNamespace
import com.android.cs.checkrom.utils.NativeUtils.isInContainer
import com.android.cs.checkrom.utils.NativeUtils.isInContainerV2
import com.android.cs.checkrom.utils.NativeUtils.isInContainerV3
import com.android.cs.checkrom.utils.NativeUtils.isSuExist
import com.android.cs.checkrom.utils.NumAnimUtil
import com.android.cs.checkrom.widgets.RecyItemDecoration
import java.util.Timer
import java.util.TimerTask

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mTimer: Timer? = null

    companion object {
        const val STEP_Interval = 500L
    }

    private var mTimerMax = 5

    private var list = mutableListOf<CheckEntry>()
    private var mIncreaseAdapter: ItemAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        safeCheck()
        startTimer()
    }

    private fun initView() {
        binding.apply {
            headerLayout.tvTitle.setTextColor(-0x1)
            headerLayout.tvTitle.text = "安全检测"
            headerLayout.ivLeftIcon.visibility = View.GONE
            headerLayout.ivLeftIcon.setImageResource(R.mipmap.ic_arrow_back_white)
            headerLayout.ivLeftIcon.setOnClickListener {
                finish()
            }
            headerLayout.line.visibility = View.GONE
        }
    }


    private fun startTimer() {
        binding.apply {
            containerAnim.setBackgroundColor(Color.parseColor("#8278DC"))
            GlideUtils.loadImageViewGif(this@MainActivity, R.drawable.safe_check, ivGif)
            NumAnimUtil.startAnim(tvProgress, 100f, (mTimerMax - 1) * STEP_Interval)
        }

        var step = 0
        mTimer = Timer()
        mTimer?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    step++
                    if (step == mTimerMax) {
                        showUiStatus()
                        mTimer?.cancel()
                    }
                }
            }
        }, 200, STEP_Interval)
    }

    private fun safeCheck() {
        val activity = this@MainActivity
        list = mutableListOf(
            CheckEntry(
                "检测App完整性",
                appCheck(activity) && appCheck1(activity)
            ),
            CheckEntry("检测root", isSuExist()),
            CheckEntry("检测系统属性", checkProp() || checkSystemPropsModified() || checkVirtual()),
            CheckEntry("检测特定的硬件或软件特性", checkSystemFeature()),
            CheckEntry("检测内存信息", checkMemInfo()),
            CheckEntry("检测Mountinfo信息", checkMountinfo()),
            CheckEntry("检测容器状态", isInContainer()),
            CheckEntry("检测docker状态", checkDockerenv()),
            CheckEntry("检测系统进程", checkInitProcess()),
            CheckEntry("检测容器Namespace", isContainerByNamespace()),
            CheckEntry("检测系统cgroup", isInContainerV2()),
            CheckEntry("检测系统Ns", isInContainerV3()),
            CheckEntry("检测蓝牙", checkBluetooth()),
            CheckEntry(
                "检测屏幕数据",
                checkScreenInfo(activity) ||
                        checkScreenDensity(activity) ||
                        checkAllDensityInfo(activity)
            ),
            CheckEntry("检测摄像头", cameraCheck(activity)),
        )
        Log.e("TAG","checkVirtual() = ${checkVirtual()}")
    }

    private fun showUiStatus() {
        val activity = this@MainActivity
        val isAbnormal = list.any { it.state }

        binding.apply {
            containerAnim.visibility = View.GONE
            container.visibility = View.VISIBLE
            headerLayout.headerContainer.visibility = View.VISIBLE
            ivGif2.visibility = View.VISIBLE
            tvInfo2.visibility = View.VISIBLE
            tvInfo2.text = if (isAbnormal) "当前设备环境异常" else "当前设备环境正常"
            tvInfo2.setTextColor(
                ContextCompat.getColor(
                    activity,
                    if (isAbnormal) R.color.red else R.color.color_4BB93F
                )
            )

            if (!activity.isDestroyed) {
                GlideUtils.loadImageViewGif(activity, R.drawable.safe_check_result, ivGif2)
            }
            mIncreaseAdapter = ItemAdapter(activity, list)
            recycleView.run {
                layoutManager = LinearLayoutManager(activity)
                addItemDecoration(RecyItemDecoration(activity, DividerItemDecoration.VERTICAL))
                adapter = mIncreaseAdapter
            }
        }
    }
}
