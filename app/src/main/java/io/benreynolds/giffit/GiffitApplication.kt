package io.benreynolds.giffit

import android.app.Application
import timber.log.Timber

class GiffitApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}