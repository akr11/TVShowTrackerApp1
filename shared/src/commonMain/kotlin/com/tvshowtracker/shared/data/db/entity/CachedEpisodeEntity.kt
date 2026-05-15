package com.tvshowtracker.shared.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_episodes",
    foreignKeys = [
        ForeignKey(
            entity = FavoriteShowEntity::class,
            parentColumns = ["showId"],
            childColumns = ["showId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["showId"])]
)
data class CachedEpisodeEntity(
    @PrimaryKey val episodeId: Long,
    val showId: Long,
    val season: Int,
    val number: Int,
    val name: String?,
    val airdate: String?,
    val runtimeMinutes: Int?,
    val mediumImageUrl: String?,
    val summaryPlain: String?
)
