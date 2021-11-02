package com.svga.plugin.svga_plugin

import android.util.Log
import android.util.LongSparseArray
import androidx.annotation.NonNull
import com.svga.plugin.svga_plugin.constants.Methods
import com.svga.plugin.svga_plugin.flutter.FlutterLoadModel
import com.svga.plugin.svga_plugin.flutter.FlutterParseCompletion
import com.svga.plugin.svga_plugin.proto.SvgaInfo
import com.svga.plugin.svga_plugin.sound_ext.SoundPool
import com.svga.plugin.svga_plugin.svga_android_lib.SVGAParser
import com.svga.plugin.svga_plugin.utils.ResultUtil
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.view.TextureRegistry
import java.net.URL

/** SvgaPlugin */
class SvgaPlugin : FlutterPlugin, MethodCallHandler, FlutterParseCompletion.DataSource {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var parser: SVGAParser

    private val modelMap = LongSparseArray<FlutterLoadModel>()

    override val widgetIdList = mutableListOf<Long>()
    override lateinit var registry: TextureRegistry

    override fun onModelGenerated(model: FlutterLoadModel) {
        modelMap.put(model.widgetId, model)
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "svga_plugin")
        channel.setMethodCallHandler(this)

        registry = flutterPluginBinding.textureRegistry
        parser = SVGAParser(flutterPluginBinding.applicationContext)
        SoundPool.instance.updateContext(flutterPluginBinding.applicationContext)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            Methods.loadFromAsset -> loadSVGA(call, result)
            Methods.loadFromURL -> loadSVGA(call, result)
            Methods.pauseSVGAWidget -> pauseSVGA(call, result)
            Methods.resumeSVGAWidget -> resumeSVGA(call, result)
            Methods.releaseSVGAWidget -> releaseSVGA(call, result)
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    // region Channel Handlers
    private fun loadSVGA(call: MethodCall, result: Result) {
        if (!checkArgumentsType<ByteArray>(call, result)) {
            return
        }

        val info = try {
            SvgaInfo.SVGALoadInfo.parseFrom(call.arguments as ByteArray)
        } catch (e: Exception) {
            Log.e("TAG", "Parse SVGALoadInfo failed: $e")
            null
        }

        if (info == null) {
            result.success(ResultUtil.parsePBFailed("SVGALoadInfo"))
            return
        }

        if (info.assetUrl.isEmpty() && info.remoteUrl.isEmpty()) {
            result.success(ResultUtil.missingKey(arrayListOf("assetUrl", "remoteUrl")))
            return
        }

        widgetIdList.add(info.widgetId)

        val completion = FlutterParseCompletion(info, result, this)

        if (info.assetUrl.isNotEmpty()) {
            parser.decodeFromAssets(info.assetUrl, completion, null, info.mute)
        } else {
            val url = try {
                URL(info.remoteUrl)
            } catch (e: Exception) {
                Log.e("TAG", "URL parse failed: $e")
                result.success(ResultUtil.dataError("remoteUrl", "is not legal url"))
                return
            }

            parser.decodeFromURL(url, completion, null, info.mute)
        }
    }

    private fun releaseSVGA(call: MethodCall, result: Result) {
        if (!checkArgumentsType<Long>(call, result)) {
            return
        }

        val widgetId = call.arguments as Long

        modelMap[widgetId]?.release()
        modelMap.remove(widgetId)
        widgetIdList.remove(widgetId)

        result.success(ResultUtil.ok)
    }

    private fun resumeSVGA(call: MethodCall, result: Result) {
        if (!checkArgumentsType<Long>(call, result)) {
            return
        }

        modelMap[call.arguments as Long]?.drawer?.resume()
        result.success(ResultUtil.ok)
    }

    private fun pauseSVGA(call: MethodCall, result: Result) {
        if (!checkArgumentsType<Int>(call, result)) {
            return
        }

        modelMap[call.arguments as Long]?.drawer?.pause()
        result.success(ResultUtil.ok)
    }

    // endregion Channel Handlers

    private inline fun <reified T> checkArgumentsType(call: MethodCall, result: Result): Boolean {
        if (call.arguments !is T) {
            result.success(ResultUtil.argumentTypeError("arguments", "${T::class.java}"))
            return false
        }

        return true
    }
}
