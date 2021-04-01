package com.sovnem.bisheng

import android.app.Application
import android.content.Context

class SampleApplication:Application() {
    override fun onCreate() {
        super.onCreate()

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
}