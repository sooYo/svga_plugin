package com.svga.plugin.svga_plugin.svga_android_lib

import android.graphics.Bitmap
import com.svga.plugin.svga_plugin.svga_android_lib.bitmap.SVGABitmapByteArrayDecoder
import com.svga.plugin.svga_plugin.svga_android_lib.bitmap.SVGABitmapFileDecoder
import com.svga.plugin.svga_plugin.svga_android_lib.entities.SVGAVideoSpriteEntity
import com.svga.plugin.svga_plugin.svga_android_lib.proto.Svga.*
import com.svga.plugin.svga_plugin.svga_android_lib.utils.SVGARect
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Created by PonyCui on 16/6/18.
 */
class SVGAVideoEntity {
    var antiAlias: Boolean = true

    private var movieItem: MovieEntity? = null

    var videoSize = SVGARect(0.0, 0.0, 0.0, 0.0)
        private set

    var fps = 15
        private set

    var frames: Int = 0
        private set

    internal var spriteList: List<SVGAVideoSpriteEntity> = emptyList()

    val imageMap = HashMap<String, Bitmap>()

    private var mCacheDir: File
    private var mFrameHeight = 0
    private var mFrameWidth = 0

    val movie: MovieEntity? get() = movieItem

    constructor(json: JSONObject, cacheDir: File, frameWidth: Int, frameHeight: Int) {
        mFrameWidth = frameWidth
        mFrameHeight = frameHeight
        mCacheDir = cacheDir

        val movieJsonObject = json.optJSONObject("movie") ?: return
        setupByJson(movieJsonObject)

        try {
            parserImages(json)
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }

        resetSprites(json)
    }

    private fun setupByJson(movieObject: JSONObject) {
        movieObject.optJSONObject("viewBox")?.let { viewBoxObject ->
            val width = viewBoxObject.optDouble("width", 0.0)
            val height = viewBoxObject.optDouble("height", 0.0)
            videoSize = SVGARect(0.0, 0.0, width, height)
        }

        fps = movieObject.optInt("fps", 20)
        frames = movieObject.optInt("frames", 0)
    }

    constructor(
        entity: MovieEntity,
        cacheDir: File,
        frameWidth: Int,
        frameHeight: Int
    ) {
        this.mFrameWidth = frameWidth
        this.mFrameHeight = frameHeight
        this.mCacheDir = cacheDir
        this.movieItem = entity

        entity.params?.let(this::setupByMovie)

        try {
            parserImages(entity)
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }

        resetSprites(entity)
    }

    private fun setupByMovie(movieParams: MovieParams) {
        val width = movieParams.viewBoxWidth.toDouble()
        val height = movieParams.viewBoxHeight.toDouble()
        videoSize = SVGARect(0.0, 0.0, width, height)

        fps = movieParams.fps
        frames = movieParams.frames
    }

    private fun parserImages(json: JSONObject) {
        val imgJson = json.optJSONObject("images") ?: return
        imgJson.keys().forEach { imgKey ->
            val filePath = generateBitmapFilePath(imgJson[imgKey].toString(), imgKey)
            if (filePath.isEmpty()) {
                return
            }
            val bitmapKey = imgKey.replace(".matte", "")
            val bitmap = createBitmap(filePath)
            if (bitmap != null) {
                imageMap[bitmapKey] = bitmap
            }
        }
    }

    private fun generateBitmapFilePath(imgName: String, imgKey: String): String {
        val path = mCacheDir.absolutePath + "/" + imgName
        val path1 = "$path.png"
        val path2 = mCacheDir.absolutePath + "/" + imgKey + ".png"

        return when {
            File(path).exists() -> path
            File(path1).exists() -> path1
            File(path2).exists() -> path2
            else -> ""
        }
    }

    private fun createBitmap(filePath: String): Bitmap? {
        return SVGABitmapFileDecoder.decodeBitmapFrom(filePath, mFrameWidth, mFrameHeight)
    }

    private fun parserImages(obj: MovieEntity) {
        obj.imagesMap?.entries?.forEach { entry ->
            val byteArray = entry.value.toByteArray()
            if (byteArray.count() < 4) {
                return@forEach
            }
            val fileTag = byteArray.slice(IntRange(0, 3))
            if (fileTag[0].toInt() == 73 && fileTag[1].toInt() == 68 && fileTag[2].toInt() == 51) {
                return@forEach
            }
            val filePath = generateBitmapFilePath(entry.value.toStringUtf8(), entry.key)
            createBitmap(byteArray, filePath)?.let { bitmap ->
                imageMap[entry.key] = bitmap
            }
        }
    }

    private fun createBitmap(byteArray: ByteArray, filePath: String): Bitmap? {
        val bitmap = SVGABitmapByteArrayDecoder.decodeBitmapFrom(byteArray, mFrameWidth, mFrameHeight)
        return bitmap ?: createBitmap(filePath)
    }

    private fun resetSprites(json: JSONObject) {
        val mutableList: MutableList<SVGAVideoSpriteEntity> = mutableListOf()
        json.optJSONArray("sprites")?.let { item ->
            for (i in 0 until item.length()) {
                item.optJSONObject(i)?.let { entryJson ->
                    mutableList.add(SVGAVideoSpriteEntity(entryJson))
                }
            }
        }
        spriteList = mutableList.toList()
    }

    private fun resetSprites(entity: MovieEntity) {
        spriteList = entity.spritesList?.map {
            return@map SVGAVideoSpriteEntity(it)
        } ?: listOf()
    }

    fun clear() {
        spriteList = emptyList()
        imageMap.clear()
    }
}

