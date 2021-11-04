package com.svga.plugin.svga_plugin.svga_android_lib.drawer

import android.graphics.Canvas
import android.widget.ImageView
import com.svga.plugin.svga_plugin.svga_android_lib.SVGAVideoEntity
import com.svga.plugin.svga_plugin.svga_android_lib.entities.SVGAVideoSpriteFrameEntity
import com.svga.plugin.svga_plugin.svga_android_lib.utils.Pools
import com.svga.plugin.svga_plugin.svga_android_lib.utils.SVGAScaleInfo
import kotlin.math.max

/**
 * Created by cuiminghui on 2017/3/29.
 */

open class SGVADrawer(val videoItem: SVGAVideoEntity) {

    val scaleInfo = SVGAScaleInfo()

    private val spritePool = Pools.SimplePool<SVGADrawerSprite>(max(1, videoItem.spriteList.size))

    inner class SVGADrawerSprite(
        var _matteKey: String? = null,
        var _imageKey: String? = null,
        var _frameEntity: SVGAVideoSpriteFrameEntity? = null
    ) {
        val matteKey get() = _matteKey
        val imageKey get() = _imageKey
        val frameEntity get() = _frameEntity!!
    }

    internal fun requestFrameSprites(frameIndex: Int): List<SVGADrawerSprite> {
        return videoItem.spriteList.mapNotNull {
            if (frameIndex >= 0 && frameIndex < it.frames.size) {
                it.imageKey?.let { imageKey ->
                    if (!imageKey.endsWith(".matte") && it.frames[frameIndex].alpha <= 0.0) {
                        return@mapNotNull null
                    }
                    return@mapNotNull (spritePool.acquire() ?: SVGADrawerSprite()).apply {
                        _matteKey = it.matteKey
                        _imageKey = it.imageKey
                        _frameEntity = it.frames[frameIndex]
                    }
                }
            }
            return@mapNotNull null
        }
    }

    internal fun releaseFrameSprites(sprites: List<SVGADrawerSprite>) {
        sprites.forEach { spritePool.release(it) }
    }

    open fun drawFrame(canvas: Canvas, frameIndex: Int, scaleType: ImageView.ScaleType) {
        scaleInfo.performScaleType(
            canvas.width.toFloat(),
            canvas.height.toFloat(),
            videoItem.videoSize.width.toFloat(),
            videoItem.videoSize.height.toFloat(),
            scaleType
        )
    }
}
