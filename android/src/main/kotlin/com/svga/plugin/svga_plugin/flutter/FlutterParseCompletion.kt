package com.svga.plugin.svga_plugin.flutter

import android.content.Context
import android.view.Surface
import com.svga.plugin.svga_plugin.proto.SvgaInfo.SVGALoadInfo
import com.svga.plugin.svga_plugin.sound_ext.SoundPool
import com.svga.plugin.svga_plugin.svga_android_lib.SVGAParser
import com.svga.plugin.svga_plugin.svga_android_lib.SVGAVideoEntity
import com.svga.plugin.svga_plugin.utils.ResultUtil
import com.svga.plugin.svga_plugin.utils.imageViewScaleType
import com.svga.plugin.svga_plugin.utils.source
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.TextureRegistry
import java.lang.ref.WeakReference

class FlutterParseCompletion(
    private val loadInfo: SVGALoadInfo,
    private val result: MethodChannel.Result,
    private val context: Context,
    dataSource: DataSource
) : SVGAParser.ParseCompletion {
    private val dataSource = WeakReference(dataSource)
    private val registry = WeakReference(dataSource.registry)
    private val density = context.resources.displayMetrics.density

    interface DataSource {
        val widgetIdList: List<Long>

        val registry: TextureRegistry

        fun onModelGenerated(model: FlutterLoadModel)
    }

    private val downscaleFactor: Double
        get() {
            return with(context.resources.displayMetrics) {
                when {
                    densityDpi in 240..400 -> 0.7
                    densityDpi > 400 -> 0.6
                    else -> 0.8
                }
            }
        }

    override fun onComplete(videoItem: SVGAVideoEntity) {
        if (dataSource.get()?.widgetIdList?.contains(loadInfo.widgetId) != true) {
            result.success(ResultUtil.ok)
            return
        }

        val drawer = FlutterCanvasDrawer(videoItem, loadInfo)
        val textureEntry = registry.get()?.createSurfaceTexture()
        val texture = textureEntry?.surfaceTexture()

        if (textureEntry == null || texture == null) {
            result.success(ResultUtil.textureError)
            return
        }

        texture.setDefaultBufferSize(
            (loadInfo.width * density * downscaleFactor).toInt(),
            (loadInfo.height * density * downscaleFactor).toInt()
        )

        val model = FlutterLoadModel.Builder()
            .setDrawer(drawer)
            .setSurface(Surface(texture))
            .setTextureEntry(textureEntry)
            .setWidgetId(loadInfo.widgetId)
            .setSource(loadInfo.source)
            .setMovie(videoItem.movieItem)
            .build()

        dataSource.get()?.onModelGenerated(model)

        val completion: () -> Unit = {
            result.success(ResultUtil.successWithTexture(textureEntry.id()))
            drawer.drawOnSurface(model.surface, loadInfo.imageViewScaleType)
        }

        if (!loadInfo.mute && videoItem.movieItem != null) {
            SoundPool.instance.loadAudiosFromMovie(videoItem.movieItem!!, loadInfo.widgetId, completion)
        } else {
            completion()
        }
    }

    override fun onError() {
        result.success(ResultUtil.parseSVGAFailed(loadInfo.source))
    }
}
