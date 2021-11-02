package com.svga.plugin.svga_plugin.flutter

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.Surface
import android.widget.ImageView
import com.svga.plugin.svga_plugin.sound_ext.SoundPool
import com.svga.plugin.svga_plugin.svga_android_lib.SVGADynamicEntity
import com.svga.plugin.svga_plugin.svga_android_lib.SVGAVideoEntity
import com.svga.plugin.svga_plugin.svga_android_lib.drawer.SVGACanvasDrawer

/**
 * Drawer for Flutter plugin
 *
 * `repeatCount` is the count to play the movie, eg. with repeatCount = 1 , the
 * movie shall be played for one time. And when it's 0, the movie will be repeatedly
 * played. Values less than 0 will be handled as 0.
 */
class FlutterCanvasDrawer(
    videoItem: SVGAVideoEntity,
    repeatCount: Int = 1,
    mute: Boolean = false
) : SVGACanvasDrawer(videoItem, SVGADynamicEntity()), FlutterSVGADriver.UpdateListener {

    private val driver = FlutterSVGADriver(videoItem, mute, repeatCount)
    private lateinit var surface: Surface
    private lateinit var scaleType: ImageView.ScaleType

    init {
        driver.delegate = this
    }

    fun drawOnSurface(surface: Surface, scaleType: ImageView.ScaleType) {
        this.surface = surface
        this.scaleType = scaleType

        driver.start()
    }

    fun release() {
        surface.release()
        driver.release()
    }

    fun pause() = driver.pause()

    fun resume() = driver.resume()

    fun stop() = driver.stop()

    override fun onUpdate(frame: Int) {
        surface.lockCanvas(null).run {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            drawFrame(this, frame, scaleType)
            surface.unlockCanvasAndPost(this)
        }

        if (videoItem.movie != null) {
            SoundPool.instance.onFrameChangedForMovie(videoItem.movie!!, frame)
        }
    }
}