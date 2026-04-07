package dev.sumanth.spd.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DownloadHistoryItem(
    val id: String = System.currentTimeMillis().toString(),
    val spotifyUrl: String,
    val title: String,
    val artist: String,
    val downloadedAt: Long = System.currentTimeMillis(),
    val totalTracks: Int = 0,
    val successfulTracks: Int = 0,
    val failedTracks: Int = 0,
    val filePath: String,
    val format: String = "m4a",
    val convertedToMp3: Boolean = false,
    val status: DownloadStatus = DownloadStatus.COMPLETED
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(downloadedAt))
    }

    fun getSuccessRate(): Int {
        return if (totalTracks > 0) {
            ((successfulTracks * 100) / totalTracks)
        } else {
            0
        }
    }

    fun getSummary(): String {
        return "$successfulTracks/${totalTracks} songs (${getSuccessRate()}%)"
    }
}

enum class DownloadStatus {
    DOWNLOADING,
    COMPLETED,
    FAILED,
    PARTIAL
}

data class DownloadTrack(
    val trackId: String = System.currentTimeMillis().toString(),
    val title: String,
    val artist: String,
    val status: TrackStatus = TrackStatus.PENDING,
    val downloadedAt: Long = System.currentTimeMillis(),
    val fileSize: Long = 0L,
    val speedMbps: Float = 0f
)

enum class TrackStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED
}
