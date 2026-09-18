package com.pierre.tunescout

import android.app.Application
import com.pierre.tunescout.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TuneScoutApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TuneScoutApplication)
            modules(appModules)
        }
    }
}
