package com.svga.plugin.svga_plugin.sound_ext.models

import com.svga.plugin.svga_plugin.sound_ext.contants.LoadCompletion
import com.svga.plugin.svga_plugin.svga_android_lib.proto.AudioEntity
import java.io.File

/**
 * Abstraction of audios in a SVGA
 *
 * [movieId] is the unique id of the attached SVGA file. [soundTracksCount] is the
 * total number of audios in this file, because we don't want to hold a [MovieEntity]
 * in this class, thus we store the key properties to judge whether loading finish or not
 */
class MovieSoundModel(
    val movieId: Int,
    val soundTracksCount: Int,
    val completion: LoadCompletion
) {
    /**
     * Represents a single audio
     */
    class SoundModel(val entity: AudioEntity, val cacheFile: File?) {
        var soundId: Int? = null
        var streamId: Int? = null
    }

    private val models: MutableMap<String, SoundModel> = mutableMapOf()

    var loadCount: Int = 0
    var errorCount: Int = 0 // Load complete with non-zero status code
    var isPlaying: Boolean = false

    val audiosList: List<SoundModel> get() = models.values.toList()
    val audioFiles: List<File?> get() = models.map { it.value.cacheFile }.toList()
    val finishLoad: Boolean get() = loadCount == soundTracksCount

    fun addAudio(audio: SoundModel) = models.put(audio.entity.audioKey, audio)

    fun clear() = models.clear()
}