/// Copyright (c) 2020 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixrtm.gradle.classHierarchy

import org.gradle.api.Incubating
import org.objectweb.asm.tree.MethodNode

class HMethod(val declaringClass: HClass, @Incubating val node: MethodNode) {
    val name get() = node.name
    val desc get() = node.desc
}
