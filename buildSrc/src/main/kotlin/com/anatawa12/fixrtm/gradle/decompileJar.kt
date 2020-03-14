package com.anatawa12.fixrtm.gradle

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger
import org.slf4j.LoggerFactory
import java.io.File

fun main() {
    decompileJar(
            jarFile = File("./mods/rtm.jar"),
            outputDir = File("./mods/rtm.jar.classes/")
    )
}

class ConsoleDecompilerImpl(destination: File, options: Map<String, Any>, logger: IFernflowerLogger)
    : ConsoleDecompiler(destination, options, logger) {
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

fun decompileJar(jarFile: File, outputDir: File) {
    outputDir.mkdirs()
    val logger = FernflowerLogger

    val decompiler = ConsoleDecompilerImpl(outputDir, mapOf(
            "dgs" to "1",
            "rsy" to "1"
    ), logger)

    decompiler.addSource(jarFile)

    decompiler.decompileContext()
}
