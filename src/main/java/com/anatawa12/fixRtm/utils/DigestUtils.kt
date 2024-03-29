/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.utils

import java.io.InputStream
import java.security.MessageDigest

object DigestUtils {
    private val sha1 = ThreadLocal.withInitial { MessageDigest.getInstance("SHA-1") }
    const val STREAM_BUFFER_LENGTH = 0x10000

    fun sha1(bytes: String) = sha1(bytes.toByteArray())

    fun sha1(bytes: ByteArray) = sha1.get().digest(bytes)

    fun sha1Hex(value: String) = hex(sha1(value.toByteArray()))

    fun sha1Hex(bytes: ByteArray) = hex(sha1(bytes))

    fun sha1Hex(stream: InputStream): String {
        val sha1 = sha1.get()
        hex(sha1.digest())

        val buffer = ArrayPool.bytePool.get(STREAM_BUFFER_LENGTH)
        var read: Int = stream.read(buffer, 0, STREAM_BUFFER_LENGTH)

        while (read > -1) {
            sha1.update(buffer, 0, read)
            read = stream.read(buffer, 0, STREAM_BUFFER_LENGTH)
        }

        return hex(sha1.digest())
    }

    fun hex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF

            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    private val hexArray = "0123456789abcdef".toCharArray()

}
