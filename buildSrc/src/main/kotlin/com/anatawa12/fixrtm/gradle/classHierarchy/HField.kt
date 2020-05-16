package com.anatawa12.fixrtm.gradle.classHierarchy

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldNode

class HField(val declaringClass: HClass, private val node: FieldNode) {
    val isStatic get() = node.access and Opcodes.ACC_STATIC != 0
    val name get() = node.name
    val type get() = node.desc
}
