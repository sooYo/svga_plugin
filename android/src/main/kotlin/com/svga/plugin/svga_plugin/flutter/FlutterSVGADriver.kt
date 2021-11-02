package com.svga.plugin.svga_plugin.flutter

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import com.svga.plugin.svga_plugin.sound_ext.SoundPool
import com.svga.plugin.svga_plugin.svga_android_lib.SVGAVideoEntity
import kotlin.math.roundToLong

class FlutterSVGADriver(
    private val movie: SVGAVideoEntity,
    private val mute: Boolean = false,
    repeatCount: Int = 1
) {
    interface UpdateListener {
        fun onUpdate(frame: Int)
    }

    private var _animator: ValueAnimator? = null
    var delegate: UpdateListener? = null

    val isRunning: Boolean get() = _animator?.isRunning ?: false

    init {
        val duration = movie.frames / movie.fps.toFloat() * 1000f

        _animator = ValueAnimator.ofFloat(0f, duration)
        _animator?.duration = duration.roundToLong()
        _animator?.repeatCount = when {
            repeatCount <= 0 -> INFINITE
            else -> repeatCount - 1
        }

        _animator?.addUpdateListener {
            val time = (it.animatedValue as Float).coerceAtLeast(0f) / 1000f
            delegate?.onUpdate((time * movie.fps).toInt())
        }

        _animator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) = stop()
            override fun onAnimationCancel(animation: Animator?) = stop()

            // When repeating this animation, always notify delegate animation ran to end
            // for some occasions the progress calculation gets wrong due to Float precision
            override fun onAnimationRepeat(animation: Animator?) {
                delegate?.onUpdate(movie.frames)
            }
        })
    }

    fun start() {
        _animator?.start()

        if (!mute && movie.movie != null) {
            SoundPool.instance.playMovie(movie.movie, true)
        }
    }

    fun pause() {
        _animator?.pause()

        if (!mute && movie.movie != null) {
            SoundPool.instance.pauseAudiosForMovie(movie.movie)
        }
    }

    fun resume() {
        _animator?.resume()

        if (!mute && movie.movie != null) {
            SoundPool.instance.resumeAudiosForMovie(movie.movie)
        }
    }

    fun stop() {
        if (!isRunning) {
            return
        }

        _animator?.end()

        if (!mute && movie.movie != null) {
            SoundPool.instance.stopAudiosForMovie(movie.movie)
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

        if (!mute && movie.movie != null) {
            SoundPool.instance.unloadAudiosForMovie(movie.movie)
        }
    }
}