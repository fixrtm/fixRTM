/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.scripting

import com.anatawa12.fixRtm.Loggers
import com.anatawa12.fixRtm.caching.TaggedFileManager
import com.anatawa12.sai.Context
import com.anatawa12.sai.Scriptable
import com.anatawa12.sai.ScriptableObject
import java.io.*

class ExecutedScript private constructor(
    /**
     * dependency name and sha1 hash of script.
     */
    val dependencies: Map<String, ByteArray>,
    val scopeData: ByteArray?,
) {
    constructor(
        dependencies: Map<String, ByteArray>,
        scope: ScriptableObject,
        base: Scriptable,
    ) : this(
        dependencies, writeScopeData(scope, base)
    ) {
        for ((name, hash) in dependencies) {
            require(hash.size == 20) { "dependencies[$name] is not valid sha1: size is not 20" }
        }
    }

    fun getScope(base: Scriptable) = readScopeData(scopeData, base)

    object Serializer : TaggedFileManager.Serializer<ExecutedScript> {
        override val type: Class<ExecutedScript> get() = ExecutedScript::class.java

        override fun serialize(stream: OutputStream, value: ExecutedScript) {
            if (value.scopeData == null)
                throw IOException("this is not valid ExecutedScript, failed to make scopeData")
            @Suppress("NAME_SHADOWING")
            val stream = DataOutputStream(stream)
            stream.writeInt(value.dependencies.size)
            for ((name, data) in value.dependencies) {
                stream.writeUTF(name)
                stream.write(data)
            }
            stream.write(value.scopeData)

        }

        override fun deserialize(stream: InputStream): ExecutedScript {
            @Suppress("NAME_SHADOWING")
            val stream = DataInputStream(stream)

            val dependencies = mutableMapOf<String, ByteArray>()
            val count = stream.readInt()
            repeat(count) {
                val name = stream.readUTF()
                val hash = ByteArray(20)
                stream.readFully(hash)
                dependencies[name] = hash
            }

            val scopeData = stream.readBytes()

            return ExecutedScript(
                dependencies,
                scopeData
            )
        }
    }

    companion object {
        /**
         * user have to set [Context]
         */
        private fun writeScopeData(
            scope: ScriptableObject,
            @Suppress("UNUSED_PARAMETER") base: Scriptable,
        ): ByteArray? {
            try {
                val baos = ByteArrayOutputStream()
                ObjectOutputStream(baos).use { stream ->
                    stream.writeObject(scope)
                }
                return baos.toByteArray()
            } catch (e: NotSerializableException) {
                logger.error("cannot serialize scope data: {}", e.toString())
                return null
            } catch (e: IOException) {
                logger.error("writing scope data: ", e)
                return null
            }
        }

        /**
         * user have to set [Context]
         */
        private fun readScopeData(data: ByteArray?, @Suppress("UNUSED_PARAMETER") base: Scriptable): ScriptableObject? {
            if (data == null) return null
            try {
                val bais = ByteArrayInputStream(data)
                ObjectInputStream(bais).use { stream ->
                    return stream.readObject() as ScriptableObject
                }
            } catch (e: IOException) {
                return null
            } catch (e: ClassNotFoundException) {
                return null
            } catch (e: NoClassDefFoundError) {
                return null
            } catch (e: ClassCastException) {
                return null
            }
        }

        private val logger = Loggers.getLogger("ExecutedScript")
    }
}
