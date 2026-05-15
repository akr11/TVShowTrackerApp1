package com.tvshowtracker.shared.di

import com.tvshowtracker.shared.data.db.RoomDatabaseBuilderFactory
import com.tvshowtracker.shared.data.db.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

class IosRoomDatabaseBuilderFactory : RoomDatabaseBuilderFactory {
    override fun createRoomDatabaseBuilder() = getDatabaseBuilder()
}

fun iosInfrastructureModule(): Module = module {
    single<RoomDatabaseBuilderFactory> { IosRoomDatabaseBuilderFactory() }
}
