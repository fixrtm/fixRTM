/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.utils

import java.io.OutputStream

/**
 * append as Latin-1
 */
class ToStringOutputStream(private val out: Appendable) : OutputStream() {
    override fun write(b: Int) {
        write(byteArrayOf(b.toByte()))
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    override fun write(b: ByteArray, off: Int, len: Int) {
        out.append(String(CharArray(len) { b[off + it].toUByte().toInt().toChar() }))
    }
}
