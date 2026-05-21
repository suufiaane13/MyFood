package com.si.myfoodordering

import android.app.Application
import com.si.myfoodordering.push.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FoodApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensure(this)
    }
}
