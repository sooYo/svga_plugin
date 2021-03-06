package com.svga.plugin.svga_plugin.sound_ext

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.util.LongSparseArray
import com.svga.plugin.svga_plugin.sound_ext.contants.LoadCompletion
import com.svga.plugin.svga_plugin.sound_ext.models.MovieSoundModel
import com.svga.plugin.svga_plugin.svga_android_lib.proto.MovieEntity
import java.io.FileInputStream

class SoundPool private constructor(maxStream: Int = 20) {
    companion object {
        val instance: com.svga.plugin.svga_plugin.sound_ext.SoundPool by lazy(
            mode = LazyThreadSafetyMode.SYNCHRONIZED
        ) { SoundPool() }
    }

    private var _pool: SoundPool? = null
    private val _models = LongSparseArray<MovieSoundModel>()
    private val _soundIDMap = HashMap<Int, Long>()

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
            val movieId = _soundIDMap[soundId] ?: return@setOnLoadCompleteListener
            val model = _models[movieId] ?: return@setOnLoadCompleteListener

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
    fun loadAudiosFromMovie(movie: MovieEntity, movieId: Long, completion: LoadCompletion) {
        val model = generateSoundModelForMovie(movie, movieId, completion)
        if (model == null || model.audiosList.isEmpty()) {
            completion()
            return
        }

        setModelsIntoSoundPool(model, movieId)
    }

    fun stopAudiosForMovie(movieId: Long) {
        _models[movieId]?.let {
            it.audiosList.forEach { e ->
                if (e.streamId != null) {
                    _pool?.stop(e.streamId!!)
                }
            }

            it.isPlaying = false
        }
    }

    fun pauseAudiosForMovie(movieId: Long) {
        _models[movieId]?.let {
            it.audiosList.forEach { e ->
                if (e.streamId != null) {
                    _pool?.pause(e.streamId!!)
                }
            }

            it.isPlaying = false
        }
    }

    fun resumeAudiosForMovie(movieId: Long) {
        _models[movieId]?.let {
            it.audiosList.forEach { e ->
                if (e.streamId != null) {
                    _pool?.resume(e.streamId!!)
                }
            }

            it.isPlaying = true
        }
    }

    fun unloadAudiosForMovie(movieId: Long) {
        _models[movieId]?.let {
            _cache.cleanAudioFiles(it.audioFiles)
            it.audiosList.forEach { e ->
                if (e.streamId != null) {
                    _pool?.stop(e.streamId!!)
                }
            }

            it.clear()
            it.isPlaying = false
        }

        _models.remove(movieId)
    }

    fun playMovie(movieId: Long, play: Boolean) {
        _models[movieId]?.let { it.isPlaying = play }
    }

    fun onFrameChangedForMovie(movieId: Long, frame: Int) {
        val model = _models[movieId] ?: return
        if (!model.finishLoad || !model.isPlaying) {
            // Didn't complete loading
            return
        }

        model.audiosList.forEach { audio ->
            if (audio.soundId == null) {
                return@forEach
            }

            val startFrame = audio.entity.startFrame ?: 0
            val endFrame = audio.entity.endFrame ?: 0

            if (startFrame >= frame && audio.streamId == null) {
                audio.streamId = _pool?.play(audio.soundId!!, 1f, 1f, 1, 0, 1f)
            } else if (endFrame <= frame && audio.streamId != null) {
                _pool?.stop(audio.streamId!!)
                audio.streamId = null
            }
        }
    }
// endregion Core

    // region SoundModel Generate
    private fun generateSoundModelForMovie(
        movie: MovieEntity,
        movieId: Long,
        completion: LoadCompletion
    ): MovieSoundModel? {
        if (movie.audios.count() == 0) {
            return null
        }

        val audios = movie.audios.filter {
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
                movie.images[audio.audioKey]?.toByteArray()
            )?.let {
                audioModel.addAudio(MovieSoundModel.SoundModel(audio, it))
            }
        }

        return audioModel.also { _models.put(movieId, it) }
    }

    private fun setModelsIntoSoundPool(model: MovieSoundModel, movieId: Long) {
        model.audiosList.forEach { audio ->
            val startTime = (audio.entity.startTime ?: 0).toDouble()
            val totalTime = (audio.entity.totalTime ?: 0).toDouble()

            if (totalTime.toInt() == 0) {
                // ??????????????? 0
                return@forEach
            }

            audio.soundId = FileInputStream(audio.cacheFile).let {
                val length = it.available().toDouble()
                val offset = startTime / totalTime * length
                _pool?.load(it.fd, offset.toLong(), length.toLong(), 1)
            }

            if (audio.soundId != null) {
                _soundIDMap[audio.soundId!!] = movieId
            }
        }
    }
// endregion SoundModel Generate
}