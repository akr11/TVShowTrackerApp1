package com.tvshowtracker.android

import android.app.Application
import com.tvshowtracker.shared.di.androidInfrastructureModule
import com.tvshowtracker.shared.di.initKoin
import org.koin.android.ext.koin.androidContext

class TvShowTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(listOf(androidInfrastructureModule())) {
            androidContext(this@TvShowTrackerApplication)
        }
    }
}
