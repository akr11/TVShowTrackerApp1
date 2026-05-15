package com.tvshowtracker.shared.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.tvshowtracker.shared.data.db.entity.CachedEpisodeEntity
import com.tvshowtracker.shared.data.db.entity.FavoriteShowEntity
import com.tvshowtracker.shared.data.db.entity.WatchedEpisodeEntity

@Database(
    entities = [
        FavoriteShowEntity::class,
        CachedEpisodeEntity::class,
        WatchedEpisodeEntity::class
    ],
    version = 2,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tvShowDao(): TvShowDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
