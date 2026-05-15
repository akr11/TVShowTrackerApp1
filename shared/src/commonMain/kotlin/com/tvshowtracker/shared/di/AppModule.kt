package com.tvshowtracker.shared.di

import com.tvshowtracker.shared.data.db.AppDatabase
import com.tvshowtracker.shared.data.db.RoomDatabaseBuilderFactory
import com.tvshowtracker.shared.data.db.getRoomDatabase
import com.tvshowtracker.shared.data.network.TvShowsRemoteApi
import com.tvshowtracker.shared.data.network.TvmazeClient
import com.tvshowtracker.shared.data.network.createTvmazeHttpClient
import com.tvshowtracker.shared.data.repository.SessionCache
import com.tvshowtracker.shared.data.repository.TvShowRepository
import com.tvshowtracker.shared.presentation.DetailViewModel
import com.tvshowtracker.shared.presentation.EpisodesViewModel
import com.tvshowtracker.shared.presentation.LibraryViewModel
import com.tvshowtracker.shared.presentation.SearchViewModel
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/**
 * Pure shared graph. Requires at least one [RoomDatabaseBuilderFactory] binding —
 * supplied from `androidInfrastructureModule()` / `iosInfrastructureModule()`
 * rather than expect/actual.
 */
val sharedModule = module {
    single { createTvmazeHttpClient() }
    single<TvShowsRemoteApi> { TvmazeClient(get()) }
    single<AppDatabase> {
        getRoomDatabase(get<RoomDatabaseBuilderFactory>().createRoomDatabaseBuilder())
    }
    single { get<AppDatabase>().tvShowDao() }
    single { SessionCache() }
    single { TvShowRepository(get(), get(), get()) }
    factory { SearchViewModel(get()) }
    factory { DetailViewModel(get()) }
    factory { EpisodesViewModel(get()) }
    factory { LibraryViewModel(get()) }
}

fun initKoin(
    infrastructureModules: List<Module>,
    appDeclaration: KoinAppDeclaration = {}
) {
    Napier.base(DebugAntilog())
    startKoin {
        appDeclaration()
        modules(infrastructureModules + sharedModule)
    }
}
