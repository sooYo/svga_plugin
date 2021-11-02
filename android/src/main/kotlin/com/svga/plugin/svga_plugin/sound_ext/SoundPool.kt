package com.svga.plugin.svga_plugin.sound_ext

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.util.Log
import com.svga.plugin.svga_plugin.sound_ext.contants.LoadCompletion
import com.svga.plugin.svga_plugin.sound_ext.models.MovieSoundModel
import com.svga.plugin.svga_plugin.svga_android_lib.proto.Svga
import java.io.FileInputStream

class SoundPool private constructor(maxStream: Int = 20) {
    companion object {
        val instance: com.svga.plugin.svga_plugin.sound_ext.SoundPool by lazy(
            mode = LazyThreadSafetyMode.SYNCHRONIZED
        ) { SoundPool() }
    }

    private var _pool: SoundPool? = null
    private val _models: MutableMap<Int, MovieSoundModel> = mutableMapOf()
    private lateinit var _cache: SoundCache

    init {
        _pool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build()
                )
                .setMaxStreams(maxStream)
                .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(maxStream, AudioManager.STREAM_MUSIC, 0)
        }

        _pool?.setOnLoadCompleteListener { _, soundId, status ->
            val model = try {
                _models.values.first {
                    it.audiosList.find { e -> e.soundId == soundId } != null
                }
            } catch (e: Exception) {
                Log.e("TAG", "LoadComplete: $soundId not found, $e")
                null
            } ?: return@setOnLoadCompleteListener

            model.loadCount += 1
            model.errorCount += if (status != 0) 1 else 0

            if (model.loadCount == model.soundTracksCount) {
                model.completion()
            }
        }
    }

    fun updateContext(context: Context) {
        _cache = SoundCache(context)
    }

    // region Core
    fun loadAudiosFromMovie(movie: Svga.MovieEntity, completion: LoadCompletion) {
        val model = generateSoundModelForMovie(movie, completion)
        if (model == null || model.audiosList.isEmpty()) {
            completion()
            return
        }

        setModelsIntoSoundPool(model)
    }

    fun stopAudiosForMovie(movie: Svga.MovieEntity?) {
        if (movie == null) {
            return
        }

        _models[movie.hashCode()]?.let {
            it.audiosList.forEach { e ->
                if (e.streamId != null) {
                    _pool?.stop(e.streamId!!)
                }
            }

            it.isPlaying = false
        }
    }

    fun pauseAudiosForMovie(movie: Svga.MovieEntity?) {
        if (movie == null) {
            return
        }

        _models[movie.hashCode()]?.let {
            it.audiosList.forEach { e ->
                if (e.streamId != null) {
                    _pool?.pause(e.streamId!!)
                }
            }

            it.isPlaying = false
        }
    }

    fun resumeAudiosForMovie(movie: Svga.MovieEntity?) {
        if (movie == null) {
            return
        }

        _models[movie.hashCode()]?.let {
            it.audiosList.forEach { e ->
                if (e.streamId != null) {
                    _pool?.resume(e.streamId!!)
                }
            }

            it.isPlaying = true
        }
    }

    fun unloadAudiosForMovie(movie: Svga.MovieEntity?) {
        if (movie == null) {
            return
        }

        _models[movie.hashCode()]?.let {
            _cache.cleanAudioFiles(it.audioFiles)
            it.audiosList.forEach { e ->
                if (e.streamId != null) {
                    _pool?.stop(e.streamId!!)
                }
            }

            it.clear()
            it.isPlaying = false
        }
    }

    fun playMovie(movie: Svga.MovieEntity?, play: Boolean) {
        if (movie == null) {
            return
        }

        _models[movie.hashCode()]?.let { it.isPlaying = play }
    }

    fun onFrameChangedForMovie(movie: Svga.MovieEntity, frame: Int) {
        val model = _models[movie.hashCode()] ?: return
        if (!model.finishLoad || !model.isPlaying) {
            // Didn't complete loading
            return
        }

        model.audiosList.forEach { audio ->
            if (audio.soundId == null) {
                return@forEach
            }

            if (audio.entity.startFrame >= frame && audio.streamId == null) {
                audio.streamId = _pool?.play(audio.soundId!!, 1f, 1f, 1, 0, 1f)
            } else if (audio.entity.endFrame <= frame && audio.streamId != null) {
                _pool?.stop(audio.streamId!!)
                audio.streamId = null
            }
        }
    }
    // endregion Core

    // region SoundModel Generate
    private fun generateSoundModelForMovie(
        movie: Svga.MovieEntity,
        completion: LoadCompletion
    ): MovieSoundModel? {
        if (movie.audiosCount == 0) {
            return null
        }

        val audios = movie.audiosList.filter {
            it.audioKey.isNotEmpty() && it.totalTime != 0
        }

        if (audios.isEmpty()) {
            return null
        }

        val audioModel = MovieSoundModel(
            movie.hashCode(),
            audios.count(),
            completion
        )

        audios.forEach { audio ->
            _cache.generateAudioFile(
                audio,
                movie.imagesMap[audio.audioKey]?.toByteArray()
            )?.let {
                audioModel.addAudio(MovieSoundModel.SoundModel(audio, it))
            }
        }

        return audioModel.also { _models[it.movieId] = it }
    }

    private fun setModelsIntoSoundPool(model: MovieSoundModel) {
        model.audiosList.forEach { audio ->
            val startTime = audio.entity.startTime.toDouble()
            val totalTime = audio.entity.totalTime.toDouble()

            audio.soundId = FileInputStream(audio.cacheFile).let {
                val length = it.available().toDouble()
                val offset = startTime / totalTime * length
                _pool?.load(it.fd, offset.toLong(), length.toLong(), 1)
            }
        }
    }
// endregion SoundModel Generate
}