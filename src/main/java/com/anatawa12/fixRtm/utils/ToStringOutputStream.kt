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
