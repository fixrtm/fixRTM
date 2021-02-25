package com.anatawa12.fixRtm

import com.anatawa12.fixRtm.utils.ArrayPool
import com.anatawa12.fixRtm.utils.closeScope
import com.anatawa12.fixRtm.utils.sortedWalk
import com.google.common.collect.Iterators
import net.minecraftforge.fml.common.Loader
import java.io.*
import java.nio.charset.Charset
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

fun getThreadGroup() = System.getSecurityManager()?.threadGroup ?: Thread.currentThread().threadGroup!!

fun threadFactoryWithPrefix(prefix: String, group: ThreadGroup = getThreadGroup()) = object : ThreadFactory {
    private val threadNumber = AtomicInteger(1)

    override fun newThread(r: Runnable?): Thread? {
        val t = Thread(group, r,
            "$prefix-" + threadNumber.getAndIncrement(),
            0)
        if (t.isDaemon) t.isDaemon = false
        if (t.priority != Thread.NORM_PRIORITY) t.priority = Thread.NORM_PRIORITY
        return t
    }
}

fun <E> List<E?>.isAllNotNull(): Boolean = all { it != null }
fun <E> Array<E?>.isAllNotNull(): Boolean = all { it != null }

val minecraftDir = Loader.instance().configDir.parentFile!!
val fixCacheDir = minecraftDir.resolve("fixrtm-cache")
val MS932 = Charset.forName("MS932")
val fixRTMCommonExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
    threadFactoryWithPrefix("fixrtm-common-executor"))

fun File.directoryDigestBaseStream() = SequenceInputStream(Iterators.asEnumeration(
    this.sortedWalk()
        .flatMap {
            sequenceOf(
                it.toRelativeString(this).byteInputStream(),
                it.inputStream().buffered()
            )
        }
        .iterator()
))

fun DataOutput.writeUTFNullable(string: String?) = closeScope {
    if (string == null) return writeShort(0xFFFF)

    var utflen = 0

    for (c in string) {
        val c = c.toInt()
        if (c in 0x0001..0x007F) {
            utflen++
        } else if (c <= 0x07FF) {
            utflen += 2
        } else {
            utflen += 3
        }
    }

    val bytes = ArrayPool.bytePool.request(utflen).closer().array

    if (utflen >= 0xFFFF)
        throw UTFDataFormatException("encoded string too long: $utflen bytes")

    var count = 0;
    for (c in string) {
        val c = c.toInt()
        if (c >= 0x0001 && c <= 0x007F) {
            bytes[count++] = c.toByte()
        } else if (c > 0x07FF) {
            bytes[count++] = (0xE0 or (c shr 12 and 0x0F)).toByte()
            bytes[count++] = (0x80 or (c shr 6 and 0x3F)).toByte()
            bytes[count++] = (0x80 or (c shr 0 and 0x3F)).toByte()
        } else {
            bytes[count++] = (0xC0 or (c shr 6 and 0x1F)).toByte()
            bytes[count++] = (0x80 or (c shr 0 and 0x3F)).toByte()
        }
    }

    write(bytes)
}

fun DataInput.readUTFNullable(): String? = closeScope {
    val length = readUnsignedShort()
    if (length == 0xFFFF) return null
    val bytes = ArrayPool.bytePool.request(length).closer().array
    val chars = ArrayPool.charPool.request(length).closer().array

    readFully(bytes)
    var byteI = 0
    var charI = 0

    while (byteI < length) {
        val c = bytes.get(byteI).toInt() and 0xff
        when (c shr 4) {
            0, 1, 2, 3, 4, 5, 6, 7 -> {
                /* 0xxxxxxx*/byteI++
                chars[charI++] = c.toChar()
            }
            12, 13 -> {
                /* 110x xxxx   10xx xxxx*/
                byteI += 2
                if (byteI > length) throw UTFDataFormatException("malformed input: partial character at end")
                val char2 = bytes[byteI - 1].toInt()
                if (char2 and 0xC0 != 0x80) throw UTFDataFormatException("malformed input around byte $byteI")
                chars[charI++] = (c and 0x1F shl 6)
                    .or(char2 and 0x3F)
                    .toChar()
            }
            14 -> {
                /* 1110 xxxx  10xx xxxx  10xx xxxx */
                byteI += 3
                if (byteI > length) throw UTFDataFormatException("malformed input: partial character at end")
                val char2 = bytes[byteI - 2].toInt()
                val char3 = bytes[byteI - 1].toInt()
                if (char2 and 0xC0 != 0x80 || char3 and 0xC0 != 0x80) throw UTFDataFormatException("malformed input around byte " + (byteI - 1))
                chars[charI++] = (c and 0x0F shl 12)
                    .or(char2 and 0x3F shl 6)
                    .or(char3 and 0x3F shl 0)
                    .toChar()
            }
            else -> throw UTFDataFormatException(
                "malformed input around byte $byteI")
        }
    }

    return String(chars, 0, charI)
}

fun File.mkParent(): File = apply { parentFile.mkdirs() }
