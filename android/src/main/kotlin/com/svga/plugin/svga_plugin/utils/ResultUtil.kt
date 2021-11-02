package com.svga.plugin.svga_plugin.utils

import com.svga.plugin.svga_plugin.constants.StatusCodes
import com.svga.plugin.svga_plugin.proto.SvgaInfo.ResultInfo

object ResultUtil {
    private fun result(code: Int, message: String?, textureId: Long = -1): ByteArray {
        return ResultInfo.newBuilder()
            .setCode(code)
            .setMessage(message ?: "")
            .setTextureId(textureId)
            .build()
            .toByteArray()
    }

    val ok: ByteArray get() = result(StatusCodes.OK.code, "Success")

    val textureError: ByteArray
        get() = result(StatusCodes.TEXTURE_ERROR.code, "Texture creation error")

    fun parsePBFailed(name: String?): ByteArray =
        result(StatusCodes.PB_PARSE_FAILED.code, "${name ?: "PB"} parse failed")

    fun argumentTypeError(arg: String?, expectedType: String?): ByteArray {
        val message = "Argument type error for $arg, expected: $expectedType "
        return result(StatusCodes.ARGUMENT_TYPE_ERROR.code, message)
    }

    fun missingKey(keys: List<String>, intro: String? = null): ByteArray {
        val keyIntro = "missing value of ${keys.joinToString(",")}"
        val message = "${intro ?: "Lack of data! "} $keyIntro"
        return result(StatusCodes.DATA_MISSING.code, message)
    }

    fun dataError(key: String, intro: String? = null): ByteArray {
        val message = "$key ${intro ?: "is wrong"}"
        return result(StatusCodes.DATA_ERROR.code, message)
    }

    fun successWithTexture(textureId: Long): ByteArray {
        assert(textureId >= 0) { "Texture ID should always be a positive integer" }
        return result(StatusCodes.OK.code, "Success", textureId)
    }
}