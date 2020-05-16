package com.anatawa12.fixrtm.gradle.classHierarchy

import org.gradle.api.Incubating
import org.objectweb.asm.tree.MethodNode

class HMethod(val declaringClass: HClass, @Incubating val node: MethodNode) {
    val name get() = node.name
    val desc get() = node.desc
}
