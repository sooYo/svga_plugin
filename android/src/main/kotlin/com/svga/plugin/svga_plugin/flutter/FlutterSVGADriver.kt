package com.svga.plugin.svga_plugin.flutter

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import com.svga.plugin.svga_plugin.proto.SvgaInfo
import com.svga.plugin.svga_plugin.sound_ext.SoundPool
import com.svga.plugin.svga_plugin.svga_android_lib.SVGAVideoEntity

class FlutterSVGADriver(
    private val movie: SVGAVideoEntity,
    private val loadInfo: SvgaInfo.SVGALoadInfo
) {
    interface UpdateListener {
        fun onUpdate(frame: Int)
    }

    private var _animator: ValueAnimator? = null
    var delegate: UpdateListener? = null

    val isRunning: Boolean get() = _animator?.isRunning ?: false

    init {
        val duration = (movie.frames + 1) * (1000 / movie.FPS).toLong()

        _animator = ValueAnimator.ofInt(0, movie.frames)
        _animator?.duration = duration
        _animator?.repeatCount = when {
            loadInfo.loopCount <= 0 -> INFINITE
            else -> loadInfo.loopCount - 1
        }

        _animator?.addUpdateListener {
            val currentFrame = _animator!!.animatedValue as Int
            onFrameUpdate(currentFrame.coerceAtLeast(0).coerceAtMost(movie.frames))
        }

        _animator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) = stop()
            override fun onAnimationCancel(animation: Animator?) = stop()

            // When repeating this animation, always notify delegate animation ran to end
            // for some occasions the progress calculation gets wrong due to Float precision
            override fun onAnimationRepeat(animation: Animator?) {
                onFrameUpdate(movie.frames)
            }
        })
    }

    fun start() {
        _animator?.start()

        if (!loadInfo.mute && movie.movieItem != null) {
            SoundPool.instance.playMovie(loadInfo.widgetId, true)
        }
    }

    fun pause() {
        _animator?.pause()

        if (!loadInfo.mute && movie.movieItem != null) {
            SoundPool.instance.pauseAudiosForMovie(loadInfo.widgetId)
        }
    }

    fun resume() {
        _animator?.resume()

        if (!loadInfo.mute && movie.movieItem != null) {
            SoundPool.instance.resumeAudiosForMovie(loadInfo.widgetId)
        }
    }

    fun stop() {
        if (!isRunning) {
            return
        }

        _animator?.end()

        if (!loadInfo.mute && movie.movieItem != null) {
            SoundPool.instance.stopAudiosForMovie(loadInfo.widgetId)
        }
    }

    fun release() {
        if (!isRunning) {
            return
        }

        _animator?.removeAllUpdateListeners()
        _animator?.removeAllListeners()
        _animator?.end()
        _animator = null

        if (!loadInfo.mute && movie.movieItem != null) {
            SoundPool.instance.unloadAudiosForMovie(loadInfo.widgetId)
        }
    }

    private fun onFrameUpdate(frame: Int) {
        delegate?.onUpdate(frame)

        if (!loadInfo.mute && movie.movieItem != null) {
            SoundPool.instance.onFrameChangedForMovie(loadInfo.widgetId, frame)
        }
    }
}