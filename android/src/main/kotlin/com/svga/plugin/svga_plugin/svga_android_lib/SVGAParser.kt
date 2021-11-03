package com.svga.plugin.svga_plugin.svga_android_lib

import android.content.Context
import android.net.http.HttpResponseCache
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.svga.plugin.svga_plugin.svga_android_lib.proto.Svga
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.Inflater
import java.util.zip.ZipInputStream

/**
 * Created by PonyCui 16/6/18.
 */
private var fileLock: Int = 0
private var isUnzipping = false

class SVGAParser(context: Context?) {
    private var mContext = context?.applicationContext

    init {
        SVGACache.onCreate(context)
    }

    @Volatile
    private var mFrameWidth: Int = 0

    @Volatile
    private var mFrameHeight: Int = 0

    interface ParseCompletion {
        fun onComplete(videoItem: SVGAVideoEntity)
        fun onError()
    }

    interface PlayCallback {
        fun onPlay(file: List<File>)
    }

    open class FileDownloader {

        var noCache = false

        open fun resume(
            url: URL,
            complete: (inputStream: InputStream) -> Unit,
            failure: (e: Exception) -> Unit
        ): () -> Unit {
            var cancelled = false
            val cancelBlock = {
                cancelled = true
            }
            threadPoolExecutor.execute {
                try {
                    if (HttpResponseCache.getInstalled() == null && !noCache) {
                        Log.e(
                            TAG,
                            "SVGAParser can not handle cache before install HttpResponseCache. see https://github.com/yyued/SVGAPlayer-Android#cache"
                        )
                    }

                    (url.openConnection() as? HttpURLConnection)?.let { it ->
                        it.connectTimeout = 20 * 1000
                        it.requestMethod = "GET"
                        it.setRequestProperty("Connection", "close")
                        it.connect()
                        it.inputStream.use { inputStream ->
                            ByteArrayOutputStream().use { outputStream ->
                                val buffer = ByteArray(4096)
                                var count: Int
                                while (true) {
                                    if (cancelled) {
                                        break
                                    }

                                    count = inputStream.read(buffer, 0, 4096)
                                    if (count == -1) {
                                        break
                                    }

                                    outputStream.write(buffer, 0, count)
                                }

                                if (cancelled) {
                                    return@execute
                                }

                                ByteArrayInputStream(outputStream.toByteArray()).use {
                                    complete(it)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    failure(e)
                }
            }

            return cancelBlock
        }
    }

    var fileDownloader = FileDownloader()

    companion object {
        private const val TAG = "SVGAParser"

        private val threadNum = AtomicInteger(0)

        internal var threadPoolExecutor = Executors.newCachedThreadPool { r ->
            Thread(r, "SVGAParser-Thread-${threadNum.getAndIncrement()}")
        }
    }

    fun setFrameSize(frameWidth: Int, frameHeight: Int) {
        mFrameWidth = frameWidth
        mFrameHeight = frameHeight
    }

    fun decodeFromAssets(
        name: String,
        callback: ParseCompletion?,
        playCallback: PlayCallback? = null
    ) {
        if (mContext == null) {
            return
        }

        try {
            mContext?.assets?.open(name)?.let {
                this.decodeFromInputStream(
                    it,
                    SVGACache.buildCacheKey("file:///assets/$name"),
                    callback,
                    true,
                    playCallback
                )
            }
        } catch (e: Exception) {
            this.invokeErrorCallback(e, callback)
        }
    }

    fun decodeFromURL(
        url: URL,
        callback: ParseCompletion?
    ): (() -> Unit)? {
        if (mContext == null) {
            return null
        }

        url.toString()
        val cacheKey = SVGACache.buildCacheKey(url)

        return if (SVGACache.isCached(cacheKey)) {
            threadPoolExecutor.execute {
                if (SVGACache.isDefaultCache()) {
                    this.decodeFromCacheKey(cacheKey, callback)
                } else {
                    this.decodeFromSVGAFileCacheKey(cacheKey, callback)
                }
            }

            return null
        } else {
            fileDownloader.resume(url, {
                this.decodeFromInputStream(it, cacheKey, callback, false)
            }, {
                this.invokeErrorCallback(it, callback)
            })
        }
    }

    /**
     * 读取解析本地缓存的 svga 文件.
     */
    private fun decodeFromSVGAFileCacheKey(
        cacheKey: String,
        callback: ParseCompletion?
    ) {
        threadPoolExecutor.execute {
            try {
                FileInputStream(SVGACache.buildSvgaFile(cacheKey)).use { inputStream ->
                    readAsBytes(inputStream)?.let { bytes ->
                        if (isZipFile(bytes)) {
                            this.decodeFromCacheKey(cacheKey, callback)
                        } else {
                            inflate(bytes)?.let {
                                val videoItem = SVGAVideoEntity(
                                    Svga.MovieEntity.parseFrom(it),
                                    File(cacheKey),
                                    mFrameWidth,
                                    mFrameHeight
                                )

                                invokeCompleteCallback(videoItem, callback)

                            } ?: this.invokeErrorCallback(
                                Exception("inflate(bytes) cause exception"),
                                callback
                            )
                        }
                    } ?: this.invokeErrorCallback(
                        Exception("readAsBytes(inputStream) cause exception"),
                        callback
                    )
                }
            } catch (e: java.lang.Exception) {
                this.invokeErrorCallback(e, callback)
            }
        }
    }

    private fun decodeFromInputStream(
        inputStream: InputStream,
        cacheKey: String,
        callback: ParseCompletion?,
        closeInputStream: Boolean = false,
        playCallback: PlayCallback? = null
    ) {
        if (mContext == null) {
            return
        }

        threadPoolExecutor.execute {
            try {
                readAsBytes(inputStream)?.let { bytes ->
                    if (isZipFile(bytes)) {
                        if (!SVGACache.buildCacheDir(cacheKey).exists() || isUnzipping) {
                            synchronized(fileLock) {
                                if (!SVGACache.buildCacheDir(cacheKey).exists()) {
                                    isUnzipping = true
                                    ByteArrayInputStream(bytes).use {
                                        unzip(it, cacheKey)
                                        isUnzipping = false
                                    }
                                }
                            }
                        }

                        this.decodeFromCacheKey(cacheKey, callback)
                    } else {
                        if (!SVGACache.isDefaultCache()) {
                            // 如果 SVGACache 设置类型为 FILE
                            threadPoolExecutor.execute {
                                SVGACache.buildSvgaFile(cacheKey).let { cacheFile ->
                                    try {
                                        cacheFile.takeIf { !it.exists() }?.createNewFile()
                                        FileOutputStream(cacheFile).write(bytes)
                                    } catch (e: Exception) {
                                        cacheFile.delete()
                                    }
                                }
                            }
                        }

                        inflate(bytes)?.let {
                            val videoItem = SVGAVideoEntity(
                                Svga.MovieEntity.parseFrom(it),
                                File(cacheKey),
                                mFrameWidth,
                                mFrameHeight
                            )

                            invokeCompleteCallback(videoItem, callback)
                        } ?: this.invokeErrorCallback(
                            Exception("inflate(bytes) cause exception"),
                            callback
                        )
                    }
                } ?: this.invokeErrorCallback(
                    Exception("readAsBytes(inputStream) cause exception"),
                    callback
                )
            } catch (e: java.lang.Exception) {
                this.invokeErrorCallback(e, callback)
            } finally {
                if (closeInputStream) {
                    inputStream.close()
                }
            }
        }
    }

    private fun invokeCompleteCallback(
        videoItem: SVGAVideoEntity,
        callback: ParseCompletion?
    ) {
        Handler(Looper.getMainLooper()).post {
            callback?.onComplete(videoItem)
        }
    }

    private fun invokeErrorCallback(
        e: Exception,
        callback: ParseCompletion?
    ) {
        e.printStackTrace()
        Handler(Looper.getMainLooper()).post {
            callback?.onError()
        }
    }

    private fun decodeFromCacheKey(
        cacheKey: String,
        callback: ParseCompletion?
    ) {
        if (mContext == null) {
            return
        }

        try {
            val cacheDir = SVGACache.buildCacheDir(cacheKey)
            File(cacheDir, "movie.binary").takeIf { it.isFile }?.let { binaryFile ->
                try {
                    FileInputStream(binaryFile).use {
                        this.invokeCompleteCallback(
                            SVGAVideoEntity(
                                Svga.MovieEntity.parseFrom(it),
                                cacheDir,
                                mFrameWidth,
                                mFrameHeight
                            ),
                            callback
                        )
                    }
                } catch (e: Exception) {
                    cacheDir.delete()
                    binaryFile.delete()
                    throw e
                }
            }

            File(cacheDir, "movie.spec").takeIf { it.isFile }?.let { jsonFile ->
                try {
                    FileInputStream(jsonFile).use { fileInputStream ->
                        ByteArrayOutputStream().use { byteArrayOutputStream ->
                            val buffer = ByteArray(2048)
                            while (true) {
                                val size = fileInputStream.read(buffer, 0, buffer.size)
                                if (size == -1) {
                                    break
                                }

                                byteArrayOutputStream.write(buffer, 0, size)
                            }

                            byteArrayOutputStream.toString().let { it ->
                                JSONObject(it).let {
                                    this.invokeCompleteCallback(
                                        SVGAVideoEntity(
                                            it,
                                            cacheDir,
                                            mFrameWidth,
                                            mFrameHeight
                                        ),
                                        callback
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    cacheDir.delete()
                    jsonFile.delete()
                    throw e
                }
            }
        } catch (e: Exception) {
            this.invokeErrorCallback(e, callback)
        }
    }

    private fun readAsBytes(inputStream: InputStream): ByteArray? {
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            val byteArray = ByteArray(2048)
            while (true) {
                val count = inputStream.read(byteArray, 0, 2048)
                if (count <= 0) {
                    break
                } else {
                    byteArrayOutputStream.write(byteArray, 0, count)
                }
            }
            return byteArrayOutputStream.toByteArray()
        }
    }

    private fun inflate(byteArray: ByteArray): ByteArray? {
        val inflater = Inflater().apply { setInput(byteArray, 0, byteArray.size) }
        val inflatedBytes = ByteArray(2048)

        ByteArrayOutputStream().use { inflatedOutputStream ->
            while (true) {
                val count = inflater.inflate(inflatedBytes, 0, 2048)
                if (count <= 0) {
                    break
                } else {
                    inflatedOutputStream.write(inflatedBytes, 0, count)
                }
            }

            inflater.end()
            return inflatedOutputStream.toByteArray()
        }
    }

    // 是否是 zip 文件
    private fun isZipFile(bytes: ByteArray): Boolean {
        return bytes.size > 4 &&
                bytes[0].toInt() == 80 &&
                bytes[1].toInt() == 75 &&
                bytes[2].toInt() == 3 &&
                bytes[3].toInt() == 4
    }

    // 解压
    private fun unzip(inputStream: InputStream, cacheKey: String) {
        val cacheDir = SVGACache.buildCacheDir(cacheKey).also {
            it.mkdir()
        }

        try {
            BufferedInputStream(inputStream).use {
                ZipInputStream(it).use { zipInputStream ->
                    while (true) {
                        val zipItem = zipInputStream.nextEntry ?: break
                        if (zipItem.name.contains("../")) {
                            // 解压路径存在路径穿越问题，直接过滤
                            continue
                        }

                        if (zipItem.name.contains("/")) {
                            continue
                        }

                        val file = File(cacheDir, zipItem.name)
                        ensureUnzipSafety(file, cacheDir.absolutePath)
                        FileOutputStream(file).use { fileOutputStream ->
                            val buff = ByteArray(2048)
                            while (true) {
                                val readBytes = zipInputStream.read(buff)
                                if (readBytes <= 0) {
                                    break
                                }
                                fileOutputStream.write(buff, 0, readBytes)
                            }
                        }

                        zipInputStream.closeEntry()
                    }
                }
            }
        } catch (e: Exception) {
            SVGACache.clearDir(cacheDir.absolutePath)
            cacheDir.delete()
            throw e
        }
    }

    // 检查 zip 路径穿透
    private fun ensureUnzipSafety(outputFile: File, dstDirPath: String) {
        val dstDirCanonicalPath = File(dstDirPath).canonicalPath
        val outputFileCanonicalPath = outputFile.canonicalPath
        if (!outputFileCanonicalPath.startsWith(dstDirCanonicalPath)) {
            throw IOException("Found Zip Path Traversal Vulnerability with $dstDirCanonicalPath")
        }
    }
}
