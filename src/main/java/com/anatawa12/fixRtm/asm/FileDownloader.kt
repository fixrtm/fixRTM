/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.asm

import java.io.File
import java.io.OutputStream
import java.net.URL
import java.security.MessageDigest

object FileDownloader {
    fun downloadOrUseLocal(url: String, toDir: File, sha1: String): File {
        check(sha1.length == 20 * 2)

        val file = toDir.resolve(url.substringAfterLast('/')).apply { parentFile.mkdirs() }
        if (!file.exists() || !checkHash(file, sha1)) {
            URL(url).openStream().use { uis ->
                file.outputStream().buffered().use { fos ->
                    uis.copyTo(fos)
                }
            }
        }
        return file
    }

    private fun checkHash(file: File, sha1: String): Boolean {
        val digestOut = object : OutputStream() {
            val digest = MessageDigest.getInstance("SHA-1")

            override fun write(b: Int) {
                write(byteArrayOf(b.toByte()))
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                digest.update(b, off, len)
            }
        }

        file.inputStream().use { fis ->
            fis.copyTo(digestOut)
        }

        val digest = digestOut.digest.digest()

        check(digest.size == 20)

        for (i in digest.indices) {
            val b = digest[i].toInt()
            val high = sha1[i * 2]
            val low = sha1[i * 2 + 1]
            if (!matchHex(b.shl(4).and(0xF), high)) return false
            if (!matchHex(b.shl(0).and(0xF), low)) return false
        }

        return true
    }

    private fun matchHex(bits: Int, hex: Char) = if (bits < 10) {
        // 0..9: '0'+bits == '0'..'9'
        (bits + '0'.toInt()).toChar() == hex
    } else {
        // a..f: 'A'+bits-10 == 'A'..'F'
        // hex | 0x20 -> lowercase
        (bits - 10 + 'a'.toInt()) == (hex.toInt() or 0x20)
    }
}
