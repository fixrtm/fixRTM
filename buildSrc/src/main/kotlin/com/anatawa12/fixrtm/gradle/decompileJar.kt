package com.anatawa12.fixrtm.gradle

import org.jetbrains.java.decompiler.main.DecompilerContext
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.jar.Manifest

fun main() {
    decompileJar(
            jarFile = File("./mods/rtm.jar"),
            outputDir = File("./mods/rtm.jar.classes/"),
            libraries = listOf()
    )
}

class ConsoleDecompilerImpl(val destination: File, options: Map<String, Any>, logger: IFernflowerLogger)
    : ConsoleDecompiler(destination, options, logger) {

    override fun createArchive(path: String?, archiveName: String, manifest: Manifest?) {
        destination.mkdirs()
    }

    override fun copyEntry(source: String, path: String?, archiveName: String?, entryName: String) {
    }

    override fun saveClassEntry(path: String?, archiveName: String?, qualifiedName: String?, entryName: String, content: String?) {
        try {
            if (content != null) {
                destination.resolve(entryName).apply { parentFile.mkdirs() }.writeText(content)
            }
        } catch (ex: IOException) {
            val message = "Cannot write $entryName"
            DecompilerContext.getLogger().writeMessage(message, ex)
        }
    }

    override fun closeArchive(path: String, archiveName: String) {
    }
}

object FernflowerLogger : IFernflowerLogger() {
    val slf4jLogger = LoggerFactory.getLogger("decompile-jar")

    override fun writeMessage(message: String, severity: Severity) {
        when (severity) {
            Severity.TRACE -> slf4jLogger.trace(message)
            Severity.INFO -> slf4jLogger.info(message)
            Severity.WARN -> slf4jLogger.warn(message)
            Severity.ERROR -> slf4jLogger.error(message)
        }
    }

    override fun writeMessage(message: String, severity: Severity, throwable: Throwable?) {
        when (severity) {
            Severity.TRACE -> slf4jLogger.trace(message, throwable)
            Severity.INFO -> slf4jLogger.info(message, throwable)
            Severity.WARN -> slf4jLogger.warn(message, throwable)
            Severity.ERROR -> slf4jLogger.error(message, throwable)
        }
    }

}

fun decompileJar(jarFile: File, outputDir: File, libraries: Iterable<File>) {
    outputDir.mkdirs()
    val logger = FernflowerLogger

    val decompiler = ConsoleDecompilerImpl(outputDir, mapOf(
            IFernflowerPreferences.HIDE_DEFAULT_CONSTRUCTOR to "0",
            IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES to "1",
            IFernflowerPreferences.REMOVE_SYNTHETIC to "1",
            IFernflowerPreferences.REMOVE_BRIDGE to "1",
            IFernflowerPreferences.LITERALS_AS_IS to "1",
            IFernflowerPreferences.NEW_LINE_SEPARATOR to "1",
            IFernflowerPreferences.MAX_PROCESSING_METHOD to 60,
            IFernflowerPreferences.INDENT_STRING to "    ",
            IFernflowerPreferences.UNIT_TEST_MODE to "0")
            , logger)

    decompiler.addSource(jarFile)

    for (library in libraries) {
        decompiler.addLibrary(library)
    }

    decompiler.decompileContext()
}
