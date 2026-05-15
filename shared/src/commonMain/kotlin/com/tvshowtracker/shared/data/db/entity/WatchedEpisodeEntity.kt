package com.tvshowtracker.shared.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "watched_episodes",
    primaryKeys = ["showId", "episodeId"],
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
data class WatchedEpisodeEntity(
    val showId: Long,
    val episodeId: Long
)
