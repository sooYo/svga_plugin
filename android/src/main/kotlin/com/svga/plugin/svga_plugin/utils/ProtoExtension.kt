package com.svga.plugin.svga_plugin.utils

import android.widget.ImageView
import com.svga.plugin.svga_plugin.proto.SvgaInfo.SVGALoadInfo
import java.net.URL

val SVGALoadInfo.imageViewScaleType: ImageView.ScaleType
    get() {
        return when (scaleType) {
            ImageView.ScaleType.FIT_XY.ordinal -> ImageView.ScaleType.FIT_XY
            ImageView.ScaleType.FIT_CENTER.ordinal -> ImageView.ScaleType.FIT_CENTER
            ImageView.ScaleType.CENTER_CROP.ordinal -> ImageView.ScaleType.CENTER_CROP
            ImageView.ScaleType.CENTER_INSIDE.ordinal -> ImageView.ScaleType.CENTER_INSIDE
            else -> ImageView.ScaleType.CENTER
        }
    }

val SVGALoadInfo.source: String
    get() {
        if (assetUrl.isNotEmpty()) {
            return assetUrl
        }

        return remoteUrl
    }

val SVGALoadInfo.sourceUrl: URL?
    get() {
        return try {
            when (remoteUrl.isNotEmpty()) {
                true -> URL(remoteUrl)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }


