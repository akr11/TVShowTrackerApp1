package com.tvshowtracker.shared.di

import android.content.Context
import com.tvshowtracker.shared.data.db.RoomDatabaseBuilderFactory
import com.tvshowtracker.shared.data.db.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

class AndroidRoomDatabaseBuilderFactory(
    private val applicationContext: Context
) : RoomDatabaseBuilderFactory {
    override fun createRoomDatabaseBuilder() = getDatabaseBuilder(applicationContext.applicationContext)
}

fun androidInfrastructureModule(): Module = module {
    single<RoomDatabaseBuilderFactory> { AndroidRoomDatabaseBuilderFactory(androidContext()) }
}
