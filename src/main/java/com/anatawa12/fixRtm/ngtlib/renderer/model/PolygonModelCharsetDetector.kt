/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.ngtlib.renderer.model

import com.anatawa12.fixRtm.MS932
import jp.ngt.ngtlib.renderer.model.ModelFormatException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.SequenceInputStream
import java.lang.ref.SoftReference
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object PolygonModelCharsetDetector {
    // allocating huge array makes GC many times so cache them
    // but the buffer will be not necessary after finish loading models,
    // so it should be weak/soft reference.
    private val buffer: ThreadLocal<SoftReference<ByteArray>> = ThreadLocal<SoftReference<ByteArray>>()

    // windows-31j: Shift_JIS with Microsoft Extension. Also known as Microsoft Code Page 932
    private val tryingCharsets = arrayOf(StandardCharsets.UTF_8, MS932)

    private fun getBuffer(): ByteArray? {
        val ref: SoftReference<ByteArray>? = buffer.get()
        var bytes: ByteArray? = if (ref == null) null else ref.get()
        if (bytes == null) {
            // 4 Mi bytes
            buffer.set(SoftReference(ByteArray(1024 * 1024).also { bytes = it }))
        }
        return bytes
    }

    // TODO: test
    fun detectCharset(inputStream: InputStream): Pair<Charset, InputStream> =
        detectCharset(inputStream, StandardCharsets.UTF_8)

    fun detectCharset(inputStream: InputStream, default: Charset): Pair<Charset, InputStream> {
        val buf = getBuffer()
        var c = 0
        try {
            // read bytes fully to buf.
            var i: Int
            while (inputStream.read(buf, c, buf!!.size - c).also { i = it } != -1) {
                c += i
                if (c == buf.size) break
            }
        } catch (e: IOException) {
            throw ModelFormatException("On read file for charset detection", e)
        }
        // empty: any charset should return empty string so use default one
        if (c == 0) return Pair(Charset.defaultCharset(), inputStream)
        val returnInputStream: InputStream = SequenceInputStream(ByteArrayInputStream(buf, 0, c), inputStream)
        for (tryingCharset in tryingCharsets) {
            var s = String(buf, 0, c, tryingCharset)
            // trim last few chars to not make error for last bytes
            s = s.substring(0, s.length - 10)

            // No U+FFFD should mean no decoding error.
            if (s.indexOf('\ufffd') == -1) return Pair(tryingCharset, returnInputStream)
        }
        // no charsets are valid: use UTF8
        return Pair(default, returnInputStream)
    }
}
