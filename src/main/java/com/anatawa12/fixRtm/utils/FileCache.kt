package com.anatawa12.fixRtm.ngtlib.renderer.model

import java.io.*
import java.util.*
import java.util.concurrent.*

class FileCache<TValue>(
        private val baseDir: File,
        baseDigest: String,
        private val executor: ExecutorService,
        private val serialize: (OutputStream, TValue) -> Unit,
        private val deserialize: (InputStream) -> TValue,
        private val withTwoCharDir: Boolean = true
) {
    private val cache = ConcurrentHashMap<String, TValue>()
    private var writings = Collections.newSetFromMap<String>(ConcurrentHashMap())

    init {
        baseDir.mkdirs()
        val modsDigest = baseDir.resolve("base-digest")
                .also { it.appendText("") }
                .readText()
        if (baseDigest != modsDigest) {
            baseDir.deleteRecursively()
            baseDir.mkdirs()
            baseDir.resolve("base-digest").writeText(baseDigest)
        }
    }

    fun loadAll() {
        baseDir.listFiles()!!
                .asSequence()
                .also { filesInBaseDir ->
                    if (withTwoCharDir) {
                        filesInBaseDir
                                .filter { it.isDirectory }
                                .filter { isHex2(it.name) }
                                .flatMap { it.listFiles()!!.asSequence() }
                    } else {
                        filesInBaseDir
                    }
                }
                .filter { it.isFile }
                .filter { isHex40(it.name) }
                .forEach { file ->
                    executor.submit {
                        if (cache[file.name] == null)
                        deserialize(file.inputStream().buffered())
                                .also { cache[file.name] = it }
                    }
                }
    }

    private fun getCacheValue(sha1: String) = cache[sha1]

    fun getCachedValue(sha1: String): TValue? {
        require(isHex40IgnoreCase(sha1)) { "invalid sha hash" }
        getCacheValue(sha1)?.let { return it }
        if (sha1 in writings) return null
        val file = getFile(sha1)
        if (!file.exists()) return null

        try {
            return deserialize(file.inputStream().buffered())
                    .also { cache[sha1] = it }
        } catch (e: IOException) {
            file.delete()
            return null
        } finally {
        }
    }

    fun putCachedValue(sha1: String, value: TValue) {
        require(isHex40IgnoreCase(sha1)) { "invalid sha hash" }
        executor.submit {
            val file = getFile(sha1)
            writings.add(sha1)
            try {
                val bas = ByteArrayOutputStream()
                serialize(bas, value)
                file.outputStream().buffered().use { bas.writeTo(it) }
            } catch (e: IOException) {
                file.delete()
            } finally {
                writings.remove(sha1)
            }
        }
    }

    private fun getFile(sha1In: String): File {
        val sha1 = sha1In.toLowerCase()
        if (withTwoCharDir) {
            return baseDir.resolve(sha1.substring(0, 2))
                    .also { it.mkdirs() }
                    .resolve(sha1)
        } else {
            return baseDir.resolve(sha1)
        }
    }

    companion object {
        private fun hexDigest(c: Char) = c in '0' .. '9' || c in 'a' .. 'f'
        private fun isHex2(v: String) = v.length == 2 && v.all { hexDigest(it) }
        private fun isHex40(v: String) = v.length == 40 && v.all { hexDigest(it) }

        private fun hexDigestIgnoreCase(c: Char) = c in '0' .. '9' || c in 'a' .. 'f' || c in 'A' .. 'F'
        private fun isHex40IgnoreCase(v: String) = v.length == 40 && v.all { hexDigestIgnoreCase(it) }
    }
}
