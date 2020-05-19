package com.anatawa12.fixRtm.scripting

import com.anatawa12.fixRtm.caching.TaggedFileManager
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.serialize.ScriptableInputStream
import org.mozilla.javascript.serialize.ScriptableOutputStream
import java.io.*

class ExecutedScript private constructor(
        /**
         * dependency name and sha1 hash of script.
         */
        val dependencies: Map<String, ByteArray>,
        val scopeData: ByteArray
) {
    constructor(
            dependencies: Map<String, ByteArray>,
            scope: NativeObject,
            base: Scriptable
    ): this(
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
            val stream = DataOutputStream(stream)
            stream.writeInt(value.dependencies.size)
            for ((name, data) in value.dependencies) {
                stream.writeUTF(name)
                stream.write(data)
            }
            stream.write(value.scopeData)

        }

        override fun deserialize(stream: InputStream): ExecutedScript {
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
        private fun writeScopeData(scope: NativeObject, base: Scriptable): ByteArray {
            val baos = ByteArrayOutputStream()
            ScriptableOutputStream(baos, base).use { stream ->
                stream.writeObject(scope)
            }
            return baos.toByteArray()
        }

        /**
         * user have to set [Context]
         */
        private fun readScopeData(data: ByteArray, base: Scriptable): NativeObject {
            val bais = ByteArrayInputStream(data)
            ScriptableInputStream(bais, base).use { stream ->
                return stream.readObject() as NativeObject
            }
        }
    }
}
