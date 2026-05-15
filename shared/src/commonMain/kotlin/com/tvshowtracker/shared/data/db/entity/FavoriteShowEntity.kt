package com.tvshowtracker.shared.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_shows")
data class FavoriteShowEntity(
    @PrimaryKey val showId: Long,
    val name: String,
    val posterUrl: String?,
    val summaryPlain: String?,
    /** Comma-separated genres */
    val genresCsv: String,
    val ratingAverage: Double?,
    val runtimeMinutes: Int?,
    val language: String?,
    val status: String?,
    val premiered: String?,
    val networkName: String?,
    val addedAtEpochMs: Long
)
