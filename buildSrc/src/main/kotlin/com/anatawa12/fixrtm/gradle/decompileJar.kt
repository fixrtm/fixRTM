package com.anatawa12.fixrtm.gradle

import org.jd.core.v1.ClassFileToJavaSourceDecompiler
import java.io.File

fun main() {
    decompileJar(
            jarFile = File("./mods/rtm.jar"),
            outputDir = File("./mods/rtm.jar.classes/")
    )
}

fun decompileJar(jarFile: File, outputDir: File) {
    outputDir.mkdirs()
    val loader = JarLoader(jarFile)
    val decompiler = ClassFileToJavaSourceDecompiler()
    for (className in loader.getAllClassName()) {
        if (className.contains('$')) continue
        val printer = JdPrinter()

        decompiler.decompile(loader, printer, className)

        outputDir.resolve("$className.java")
                .apply { parentFile.mkdirs() }
                .writeText(printer.toString())
    }
}
