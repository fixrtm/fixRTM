/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

import com.anatawa12.fixrtm.gradle.classHierarchy.ClassHierarchy
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class PrintStaticFields : DefaultTask() {
    @InputFiles
    var files: FileTree = project.files().asFileTree

    @OutputFile
    var outTo: File? = null

    @Input
    var ofClass: String? = null

    @TaskAction
    fun run() {
        val classes = ClassHierarchy().apply { load(files) }
        val outTo = outTo ?: error("outTo not inited")
        val ofClass = ofClass ?: error("ofClass not inited")

        val theClass = classes.getByInternalName(ofClass.replace('.', '/'))
        if (!theClass.isLoaded)
            throw IllegalStateException("ofClass is not loaded")

        val file = buildString {
            for (field in theClass.fields.filter { it.isStatic }) {
                append("name: ${field.name}\n")
                append("type: ${field.type}\n")
                append("\n")
            }
        }

        outTo.apply { parentFile.mkdirs() }
            .writeText(file)
    }
}
