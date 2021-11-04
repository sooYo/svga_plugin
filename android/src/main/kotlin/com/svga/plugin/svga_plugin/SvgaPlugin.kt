package com.svga.plugin.svga_plugin

import android.content.Context
import android.net.http.HttpResponseCache
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
import com.svga.plugin.svga_plugin.utils.source
import com.svga.plugin.svga_plugin.utils.sourceUrl
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.view.TextureRegistry
import java.io.File

/** SvgaPlugin */
class SvgaPlugin : FlutterPlugin, MethodCallHandler, FlutterParseCompletion.DataSource {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var parser: SVGAParser
    private lateinit var context: Context

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
        context = flutterPluginBinding.applicationContext

        parser = SVGAParser(context)
        SoundPool.instance.updateContext(context)

        val cacheDir = File(context.applicationContext.cacheDir, "http")
        HttpResponseCache.install(cacheDir, 1024 * 1024 * 128)
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

        // Handle same widget rebuild
        if (filterRepeatedLoadAction(info, result)) {
            return
        }

        widgetIdList.add(info.widgetId)

        val completion = FlutterParseCompletion(info, result, context, this)

        if (info.assetUrl.isNotEmpty()) {
            parser.decodeFromAssets(info.assetUrl, completion)
        } else {
            val remoteUrl = info.sourceUrl
            if (remoteUrl == null) {
                result.success(ResultUtil.dataError("remoteUrl", "is not legal url"))
                return
            }

            parser.decodeFromURL(remoteUrl, completion)
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

        modelMap[call.arguments as Long]?.apply {
            when (continual) {
                true -> drawer.resume()
                else -> drawer.start()
            }
        }

        result.success(ResultUtil.ok)
    }

    private fun pauseSVGA(call: MethodCall, result: Result) {
        if (!checkArgumentsType<Long>(call, result)) {
            return
        }

        modelMap[call.arguments as Long]?.apply {
            when (continual) {
                true -> drawer.pause()
                else -> drawer.stop()
            }
        }

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

    /**
     * Handle the same widget rebuild situation
     *
     * Return `true` if this actions should be filtered. This method will automatically release
     * all resource of the older model if load action should be perform as a source update request
     */
    private fun filterRepeatedLoadAction(loadInfo: SvgaInfo.SVGALoadInfo, result: Result): Boolean {
        if (!widgetIdList.contains(loadInfo.widgetId)) {
            return false
        }

        val model = modelMap[loadInfo.widgetId] ?: return false
        if (model.source == loadInfo.source) {
            // Reuse the old texture
            result.success(ResultUtil.successWithTexture(model.textureEntry.id()))
            return true
        }

        // Release resource of the old task
        model.drawer.release()
        modelMap.remove(loadInfo.widgetId)
        widgetIdList.remove(loadInfo.widgetId)
        return false
    }
}
