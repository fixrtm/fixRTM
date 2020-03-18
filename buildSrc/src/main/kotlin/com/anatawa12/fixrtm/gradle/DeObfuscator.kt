package com.anatawa12.fixrtm.gradle

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class DeObfuscator(srgFile: File) {
    private val remapper = SrgRemapper(srgFile.readText())

    fun deObfuscateJar(inputJar: InputStream, outputJar: OutputStream) {
        ZipInputStream(inputJar).use { zis ->
            ZipOutputStream(outputJar).use { zos ->
                while (true) {
                    val entry = zis.nextEntry ?: break
                    zos.putNextEntry(ZipEntry(entry.name).apply {
                        time = entry.time
                        if (entry.creationTime != null)
                            creationTime = entry.creationTime
                        if (entry.lastAccessTime != null)
                            lastAccessTime = entry.lastAccessTime
                        if (entry.lastModifiedTime != null)
                            lastModifiedTime = entry.lastModifiedTime
                    })
                    if (entry.name.endsWith(".class")) {
                        @Suppress("DEPRECATION")
                        zos.write(deObfuscate(zis.readBytes(1024)))
                    } else {
                        zis.copyTo(zos)
                    }
                    zos.closeEntry()
                    zis.closeEntry()
                }
            }
        }
    }

    fun deObfuscate(classFile: ByteArray): ByteArray {
        val writer = ClassWriter(0)
        ClassReader(classFile).accept(ClassRemapper(writer, remapper), 0)
        return writer.toByteArray()
    }

    private class SrgRemapper(srg: String) : Remapper() {
        val classMap: Map<String, String>
        val fieldMap: Map<String, String>
        val methodMap: Map<String, String>
        init {
            val classMap = mutableMapOf<String, String>()
            val fieldMap = mutableMapOf<String, String>()
            val methodMap = mutableMapOf<String, String>()
            for (s in srg.lineSequence()) {
                val elements = s.split(' ')
                if (elements.isEmpty()) continue
                when (elements[0]) {
                    "CL:" -> classMap[elements[1]] = elements[2]
                    "MD:" -> methodMap[elements[1].substringAfterLast('/')] = elements[3].substringAfterLast('/')
                    "FD:" -> fieldMap[elements[1].substringAfterLast('/')] = elements[2].substringAfterLast('/')
                }
            }
            this.classMap = classMap
            this.fieldMap = fieldMap
            this.methodMap = methodMap
        }

        override fun map(typeName: String): String = classMap[typeName] ?: typeName
        override fun mapFieldName(owner: String, name: String, desc: String): String
                = fieldMap[name] ?: name
        override fun mapMethodName(owner: String, name: String, desc: String): String
                = methodMap[name] ?: name
        override fun mapInvokeDynamicMethodName(name: String, desc: String): String
                = methodMap[name] ?: name
    }
}
