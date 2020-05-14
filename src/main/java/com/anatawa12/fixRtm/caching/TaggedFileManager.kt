package com.anatawa12.fixRtm.caching

import com.google.common.collect.HashBiMap
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream

class TaggedFileManager {
    private val map = HashBiMap.create<Int, Serializer<*>>()
    private val classMap = HashBiMap.create<Class<*>, Serializer<*>>()

    fun deserialize(stream: InputStream): Any {
        val serializer = map[readVInt(stream)]
                ?: throw IllegalArgumentException("invalid stream: invalid id")
        return serializer.deserialize(stream)
    }

    fun serialize(stream: OutputStream, value: Any) {
        val serializer = getSerializerFor(value)
        val id = requireNotNull(map.inverse()[serializer]) { "serializer is not register" }
        writeVInt(stream, id)
        serializer.serialize(stream, value)
    }

    fun register(id: Int, serializer: Serializer<*>) {
        if (id in map) throw IllegalStateException("id duplicate: $id")
        map[id] = serializer
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> getSerializerFor(value: T): Serializer<T> {
        classMap[value.javaClass]?.let { return it as Serializer<T> }

        for (serializer in map.values) {
            if (serializer.type.isInstance(value)) {
                classMap[value.javaClass] = serializer
                return serializer as Serializer<T>
            }
        }
        throw IllegalArgumentException("invalid value: no serializer found.")
    }

    private fun writeVInt(stream: OutputStream, v: Int) {
        if (0 <= v) {
            throw IllegalArgumentException("too big: $v")
        } else if (v < 0x80) {
            // 00000000 0xxxxxxx -> 0xxxxxxx

            stream.write(v)
        } else if (v < 0x4000) {
            // 0xxxxxxx xxxxxxxx -> 1xxxxxxx xxxxxxxx
            stream.write(v ushr 8 or 0x80)
            stream.write(v and 0xFF)
        } else {
            throw IllegalArgumentException("too big: $v")
        }
    }

    private fun readVInt(stream: InputStream): Int {
        val first = stream.read().takeUnless { it == -1 } ?: throw EOFException()
        if (first and 0x80 == 0) {
            // 0xxxxxxx
            return first
        } else {
            // 1xxxxxxx xxxxxxxx
            val second = stream.read().takeUnless { it == -1 } ?: throw EOFException()
            return first and 0x7F shl 8 or second
        }
    }

    interface Serializer<T : Any> {
        val type: Class<T>
        fun serialize(stream: OutputStream, value: T)
        fun deserialize(stream: InputStream): T
    }
}
