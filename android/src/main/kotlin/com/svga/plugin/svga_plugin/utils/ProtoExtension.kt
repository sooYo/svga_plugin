package com.svga.plugin.svga_plugin.utils

import android.widget.ImageView
import com.svga.plugin.svga_plugin.proto.SvgaInfo.SVGALoadInfo

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


