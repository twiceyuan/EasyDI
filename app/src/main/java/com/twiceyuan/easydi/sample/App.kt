package com.twiceyuan.easydi.sample

import android.app.Application
import com.twiceyuan.easydi.EasyDI

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        initDI()
    }

    override fun onTerminate() {
        super.onTerminate()
        EasyDI.closeAll()
    }
}