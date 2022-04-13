package com.bj.micar

import android.app.Application
import com.abner.share.ShareUtils

/**
 *AUTHOR:AbnerMing
 *DATE:2022/4/12
 *INTRODUCE:
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ShareUtils.get().initShare(
            this,
            BuildConfig.WX_ID,
            BuildConfig.QQ_ID,
            BuildConfig.WB_KEY
        )
    }
}