/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixrtm.gradle.classHierarchy

import org.gradle.api.file.FileTree
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File

class ClassHierarchy {
    var classes = mutableMapOf<String, HClass>()

    fun getByInternalName(internalName: String) = classes.getOrPut(internalName, { HClass(this, internalName) })

    fun load(classFile: ByteArray) {
        val node = ClassNode().also { ClassReader(classFile).accept(it, 0) }
        val hClass = getByInternalName(node.name)
        hClass.classNode = node
        hClass.load()
    }

    fun load(classFiles: FileTree) {
        val files = mutableSetOf<File>()
        classFiles.visit {
            if (isDirectory) return@visit
            files.add(file)
        }
        for (file in files) {
            load(file.readBytes())
        }
    }
}
