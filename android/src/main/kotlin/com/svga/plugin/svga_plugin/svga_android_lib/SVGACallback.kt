package com.svga.plugin.svga_plugin.svga_android_lib

/**
 * Created by cuiminghui on 2017/3/30.
 */
interface SVGACallback {

    fun onPause()
    fun onFinished()
    fun onRepeat()
    fun onStep(frame: Int, percentage: Double)

}