package com.svga.plugin.svga_plugin.flutter

import android.view.Surface
import com.svga.plugin.svga_plugin.svga_android_lib.proto.MovieEntity
import io.flutter.view.TextureRegistry.SurfaceTextureEntry

class FlutterLoadModel(
    val surface: Surface,
    val drawer: FlutterCanvasDrawer,
    val textureEntry: SurfaceTextureEntry,
    val widgetId: Long,
    val source: String,
    val continual: Boolean,
    val movie: MovieEntity?
) {
    class Builder {
        private lateinit var surface: Surface
        private lateinit var drawer: FlutterCanvasDrawer
        private lateinit var textureEntry: SurfaceTextureEntry
        private lateinit var source: String

        private var continual: Boolean = true
        private var movie: MovieEntity? = null
        private var widgetId: Long = -1

        fun setDrawer(drawer: FlutterCanvasDrawer) = apply { this.drawer = drawer }

        fun setSurface(surface: Surface) = apply { this.surface = surface }

        fun setTextureEntry(entry: SurfaceTextureEntry) = apply { textureEntry = entry }

        fun setMovie(movie: MovieEntity?) = apply { this.movie = movie }

        fun setWidgetId(id: Long) = apply { widgetId = id }

        fun setSource(source: String) = apply { this.source = source }

        fun setContinual(continual: Boolean) = apply { this.continual = continual }

        fun build(): FlutterLoadModel {
            return FlutterLoadModel(
                surface,
                drawer,
                textureEntry,
                widgetId,
                source,
                continual,
                movie
            )
        }
    }

    fun release() {
        drawer.release()
        textureEntry.release()
        surface.release()
    }
}

