package com.anatawa12.fixrtm.gradle

import org.jd.core.v1.api.loader.Loader
import java.io.File
import java.util.zip.ZipFile

class JarLoader(jarFile: File) : Loader {
    val zipFile = ZipFile(jarFile)

    override fun canLoad(internalName: String): Boolean {
        return zipFile.getEntry("$internalName.class") != null
    }

    override fun load(internalName: String): ByteArray {
        val entry = zipFile.getEntry("$internalName.class") ?: error("class not found: $internalName")
        @Suppress("DEPRECATION")
        return zipFile.getInputStream(entry).readBytes(1024)
    }

    fun getAllClassName() = zipFile.entries().asSequence()
            .filter { it.name.endsWith(".class") }
            .map { it.name.removeSuffix(".class") }.toList()
}
