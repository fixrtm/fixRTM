package com.anatawa12.fixRtm.utils

import java.io.OutputStream

class Base64OutputStream(private val out: OutputStream) : OutputStream() {
    // the index next char should be put to
    private var bufIndex = 0
    private val buf = ByteArray(3)
    private val outBuf = ByteArray(4)

    override fun write(b: Int) {
        buf[bufIndex++] = b.toByte()
        if (bufIndex == 3) emitBuf()
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (off < 0 || len < 0) throw IndexOutOfBoundsException("some argument less than 0")
        if (off > b.size || off + len > b.size) throw IndexOutOfBoundsException("some argument too big")
        if (off + len < 0) throw IndexOutOfBoundsException("overflow off+len")
        if (len == 0) return
        // for short one, use single byte method
        if (len == 1) return write(b[off].toInt())
        if (len == 2) {
            write(b[off].toInt())
            write(b[off + 1].toInt())
            return
        }
        @Suppress("NAME_SHADOWING")
        var off = off

        @Suppress("NAME_SHADOWING")
        var len = len

        when (bufIndex) {
            0 -> Unit // nop
            1 -> {
                buf[1] = b[off]
                buf[2] = b[off + 1]
                emitBuf()
                off += 2
                len -= 2
            }
            2 -> {
                buf[2] = b[off]
                emitBuf()
                off += 1
                len -= 1
            }
            else -> throw AssertionError("invalid bufIndex: $bufIndex")
        }

        when (len % 3) {
            0 -> Unit // nop
            1 -> {
                buf[0] = b[off + len - 1]
                bufIndex = 1
                len -= 1
            }
            2 -> {
                buf[0] = b[off + len - 2]
                buf[1] = b[off + len - 1]
                bufIndex = 2
                len -= 2
            }
            else -> throw AssertionError("len%3 !in 0..2")
        }
        assert(len % 3 == 0) { "len % 3 != 0: $len" }

        val writeBuf = ByteArray(len / 3 * 4)
        for (i in 0 until len / 3) {
            emit(b, off + i * 3, writeBuf, i * 4)
        }
        out.write(writeBuf)
    }

    private fun emitBuf() {
        emit(buf, 0, outBuf, 0)
        out.write(outBuf)
        bufIndex = 0
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun emit(buf: ByteArray, bufStart: Int, outBuf: ByteArray, outBufStart: Int) {
        val bits = buf[bufStart + 0].toUByte().toInt().shl(16)
            .or(buf[bufStart + 1].toUByte().toInt().shl(8))
            .or(buf[bufStart + 2].toUByte().toInt().shl(0))
        outBuf[outBufStart + 0] = base64_chars[bits.ushr(18).and(mask)]
        outBuf[outBufStart + 1] = base64_chars[bits.ushr(12).and(mask)]
        outBuf[outBufStart + 2] = base64_chars[bits.ushr(6).and(mask)]
        outBuf[outBufStart + 3] = base64_chars[bits.ushr(0).and(mask)]
    }

    override fun close() {
        if (bufIndex != 0) {
            for (i in bufIndex until buf.size) {
                buf[i] = 0
            }
            emit(buf, 0, outBuf, 0)
            out.write(outBuf, 0, bufIndex + 1)
        }

        out.close()
    }

    companion object {
        private const val mask = 0b111111

        @JvmStatic
        private val base64_chars = (
                "ABCDEFGHIJKLMNOP" +
                        "QRSTUVWXYZabcdef" +
                        "ghijklmnopqrstuv" +
                        "wxyz0123456789+/")
            .map { it.toByte() }
            .toByteArray()
    }
}
