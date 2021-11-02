package com.svga.plugin.svga_plugin.flutter

import android.view.Surface
import com.svga.plugin.svga_plugin.svga_android_lib.proto.Svga
import io.flutter.view.TextureRegistry.SurfaceTextureEntry

class FlutterLoadModel(
    val surface: Surface,
    val drawer: FlutterCanvasDrawer,
    private val textureEntry: SurfaceTextureEntry,
    val widgetId: Long,
    val movie: Svga.MovieEntity?
) {
    class Builder {
        private lateinit var surface: Surface
        private lateinit var drawer: FlutterCanvasDrawer
        private lateinit var textureEntry: SurfaceTextureEntry

        private var movie: Svga.MovieEntity? = null
        private var widgetId: Long = -1

        fun setDrawer(drawer: FlutterCanvasDrawer) = apply { this.drawer = drawer }

        fun setSurface(surface: Surface) = apply { this.surface = surface }

        fun setTextureEntry(entry: SurfaceTextureEntry) = apply { textureEntry = entry }

        fun setMovie(movie: Svga.MovieEntity?) = apply { this.movie = movie }

        fun setWidgetId(id: Long) = apply { widgetId = id }

        fun build(): FlutterLoadModel {
            return FlutterLoadModel(surface, drawer, textureEntry, widgetId, movie)
        }
    }

    fun release() {
        drawer.release()
        textureEntry.release()
        surface.release()
    }
}

