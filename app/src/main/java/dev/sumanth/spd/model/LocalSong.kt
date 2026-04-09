package dev.sumanth.spd.model

data class LocalSong(
    val filePath: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val size: Long,
    val dateModified: Long,
    val isFavorite: Boolean = false
)

data class LocalPlaybackItem(
    val filePath: String,
    val title: String,
    val artist: String
)
