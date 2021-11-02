package com.svga.plugin.svga_plugin.flutter

import android.view.Surface
import android.widget.ImageView
import com.svga.plugin.svga_plugin.proto.SvgaInfo.SVGALoadInfo
import com.svga.plugin.svga_plugin.sound_ext.SoundPool
import com.svga.plugin.svga_plugin.svga_android_lib.SVGAParser
import com.svga.plugin.svga_plugin.svga_android_lib.SVGAVideoEntity
import com.svga.plugin.svga_plugin.utils.ResultUtil
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.TextureRegistry
import java.lang.ref.WeakReference

class FlutterParseCompletion(
    private val loadInfo: SVGALoadInfo,
    private val result: MethodChannel.Result,
    dataSource: DataSource
) : SVGAParser.ParseCompletion {
    private val dataSource = WeakReference(dataSource)
    private val registry = WeakReference(dataSource.registry)

    interface DataSource {
        val widgetIdList: List<Int>

        val registry: TextureRegistry

        fun onModelGenerated(model: FlutterLoadModel)
    }

    override fun onComplete(videoItem: SVGAVideoEntity) {
        if (dataSource.get()?.widgetIdList?.contains(loadInfo.widgetId) != true) {
            result.success(ResultUtil.ok)
            return
        }

        val drawer = FlutterCanvasDrawer(videoItem)
        val textureEntry = registry.get()?.createSurfaceTexture()
        val texture = textureEntry?.surfaceTexture()

        if (textureEntry == null || texture == null) {
            result.success(ResultUtil.textureError)
            return
        }

        texture.setDefaultBufferSize(
            loadInfo.width.toInt(),
            loadInfo.height.toInt()
        )

        val model = FlutterLoadModel.Builder()
            .setDrawer(drawer)
            .setSurface(Surface(texture))
            .setTextureEntry(textureEntry)
            .setWidgetId(loadInfo.widgetId)
            .setMovie(videoItem.movie)
            .build()

        dataSource.get()?.onModelGenerated(model)

        result.success(textureEntry.id())
        drawer.drawOnSurface(model.surface, ImageView.ScaleType.FIT_CENTER)
    }

    override fun onError() {
    }
}
