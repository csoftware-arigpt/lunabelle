package com.cycletracker.app

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.cycletracker.app.core.lock.AppLockManager
import com.cycletracker.app.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CycleTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        // Lock the app whenever it goes to the background (process-level, so the biometric
        // dialog itself does not trigger a re-lock).
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) { AppLockManager.lock() }
        })
    }
}
