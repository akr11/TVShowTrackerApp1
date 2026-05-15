package com.tvshowtracker.shared.data.db

import androidx.room.RoomDatabase

/**
 * Platform supplies [Room.databaseBuilder] wiring (Android Context vs iOS path).
 * Interface-based wiring avoids expect/actual for dependency injection boundaries.
 */
interface RoomDatabaseBuilderFactory {
    fun createRoomDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
}
