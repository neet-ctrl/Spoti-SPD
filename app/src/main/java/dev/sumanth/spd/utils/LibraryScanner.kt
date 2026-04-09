package dev.sumanth.spd.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import dev.sumanth.spd.model.LocalSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LibraryScanner(private val context: Context) {

    suspend fun scanDirectory(
        directoryPath: String,
        onProgress: suspend (Int, Int) -> Unit = { _, _ -> }
    ): List<LocalSong> = withContext(Dispatchers.IO) {
        val dir = File(directoryPath)
        if (!dir.exists() || !dir.isDirectory) return@withContext emptyList()

        val audioFiles = mutableListOf<File>()
        dir.walkTopDown().forEach { file ->
            if (file.isFile && isAudioFile(file.extension)) {
                audioFiles.add(file)
            }
        }

        val total = audioFiles.size
        val result = mutableListOf<LocalSong>()
        for ((index, file) in audioFiles.withIndex()) {
            onProgress(index + 1, total)
            extractSong(file)?.let { result.add(it) }
        }
        result
    }

    private fun extractSong(file: File): LocalSong? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val song = LocalSong(
                filePath = file.absolutePath,
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: file.nameWithoutExtension,
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?: "Unknown Artist",
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                    ?: "Unknown Album",
                duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L,
                size = file.length(),
                dateModified = file.lastModified()
            )
            retriever.release()
            song
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isAudioFile(extension: String): Boolean {
        return extension.lowercase() in setOf("mp3", "m4a", "flac", "wav", "ogg", "aac", "opus", "wma")
    }
}
