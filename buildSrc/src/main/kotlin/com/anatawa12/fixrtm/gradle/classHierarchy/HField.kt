/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixrtm.gradle.classHierarchy

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldNode

class HField(val declaringClass: HClass, private val node: FieldNode) {
    val isStatic get() = node.access and Opcodes.ACC_STATIC != 0
    val name get() = node.name
    val type get() = node.desc
}
