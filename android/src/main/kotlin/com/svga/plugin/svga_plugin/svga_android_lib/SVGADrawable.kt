package com.svga.plugin.svga_plugin.svga_android_lib

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.svga.plugin.svga_plugin.sound_ext.SoundPool
import com.svga.plugin.svga_plugin.svga_android_lib.drawer.SVGACanvasDrawer

class SVGADrawable(val videoItem: SVGAVideoEntity, val dynamicItem: SVGADynamicEntity) : Drawable() {
    constructor(videoItem: SVGAVideoEntity) : this(videoItem, SVGADynamicEntity())

    var cleared = true
        internal set(value) {
            if (field == value) {
                return
            }
            field = value
            invalidateSelf()
        }

    var currentFrame = 0
        internal set(value) {
            if (field == value) {
                return
            }
            field = value
            invalidateSelf()
        }

    var attachViewId: Long = -1
    var scaleType: ImageView.ScaleType = ImageView.ScaleType.MATRIX

    private val drawer = SVGACanvasDrawer(videoItem, dynamicItem)

    override fun draw(canvas: Canvas) {
        if (cleared) {
            return
        }
        canvas.let {
            drawer.drawFrame(it, currentFrame, scaleType)
        }
    }

    override fun setAlpha(alpha: Int) {}

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    fun resume() {
        SoundPool.instance.resumeAudiosForMovie(attachViewId)
    }

    fun pause() {
        SoundPool.instance.pauseAudiosForMovie(attachViewId)
    }

    fun stop() {
        SoundPool.instance.stopAudiosForMovie(attachViewId)
    }

    fun clear() {
        SoundPool.instance.unloadAudiosForMovie(attachViewId)
        videoItem.clear()
    }
}