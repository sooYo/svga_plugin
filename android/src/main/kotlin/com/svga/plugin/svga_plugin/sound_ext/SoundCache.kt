package com.svga.plugin.svga_plugin.sound_ext

import android.content.Context
import android.util.Log
import com.svga.plugin.svga_plugin.svga_android_lib.proto.Svga
import java.io.File
import java.io.FileOutputStream

/**
 * A class that implements the sound file cache management
 *
 * The [SoundPool] class needs to load data from an existed file (both asset files and
 * disk files). Thus we need to store all audio data to a file before we can really load
 * them with [SoundPool]. To simplify this logic, SoundCache is the choice
 */
class SoundCache(context: Context) {
    private var cachePath: String = "${context.cacheDir.absolutePath}/svga"

    init {
        prepareCacheDirectory()
    }

    /**
     * Parse the movie entity and cache all audio data into disk
     */
    @Suppress("Unused")
    fun generateAudioFiles(movie: Svga.MovieEntity?): Map<String, File> {
        val result = mutableMapOf<String, File>()
        if (movie == null || movie.imagesCount == 0) {
            return result
        }

        movie.audiosList.forEach { audio ->
            if (audio.audioKey.isEmpty() || audio.totalTime == 0) {
                return@forEach
            }

            val audioData = movie.imagesMap[audio.audioKey]?.toByteArray()
            if (audioData == null || audioData.isEmpty()) {
                return@forEach
            }

            generateAudioFile(audio, audioData)?.let {
                result[audio.audioKey] = it
            }
        }

        return result
    }

    /**
     * Cache audio data if needed
     *
     * If the input buffer is a legal audio format data, then it'll stored
     * into the disk and the result file shall be returned. This method may
     * return null if [buffer] is empty、in a illegal format or file system
     * exception raised during the process
     */
    @Suppress("Unused")
    fun generateAudioFile(audio: Svga.AudioEntity?, buffer: ByteArray?): File? {
        if (audio?.audioKey?.isEmpty() == true || !isAudioBuffer(buffer)) {
            return null
        }

        val audioFile = File("$cachePath/${audio!!.audioKey}.audio")
        if (audioFile.exists()) {
            Log.d("TAG", "generateAudioFile: find cached file $audioFile")
            return audioFile
        }

        return audioFile.also {
            it.createNewFile()
            FileOutputStream(it).write(buffer)
        }
    }

    fun cleanAudioFiles(files: List<File?>?) {
        if (files == null || files.isEmpty()) {
            return
        }

        files.forEach {
            try {
                it?.delete()
            } catch (e: Exception) {
                Log.e("TAG", "cleanAudioFiles: $e")
            }
        }
    }

    private fun prepareCacheDirectory(): Boolean =
        File(cachePath).takeIf { !it.exists() }?.mkdir() ?: false

    /**
     * Check if the buffer represents an audio file
     */
    private fun isAudioBuffer(bytes: ByteArray?): Boolean {
        if (bytes == null || bytes.count() < 4) {
            return false
        }

        val meta = bytes.slice(IntRange(0, 3))
        val bit1 = meta[0].toInt()
        val bit2 = meta[1].toInt()
        val bit3 = meta[2].toInt()

        return (bit1 == 73 && bit2 == 68 && bit3 == 51) || (bit1 == -1 && bit2 == -5 && bit3 == -108)
    }
}