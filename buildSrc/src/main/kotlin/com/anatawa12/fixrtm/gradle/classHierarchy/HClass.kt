package com.anatawa12.fixrtm.gradle.classHierarchy

import org.objectweb.asm.tree.ClassNode

class HClass(
    val loader: ClassHierarchy,
    val internalName: String
) {
    internal var classNode: ClassNode? = null
    val isLoaded get() = classNode != null

    private val node get() = classNode ?: throw IllegalStateException("not loaded")

    private val _childClasses = mutableListOf<HClass>()
    val childClasses get() = _childClasses as List<HClass>

    val parentClass get() = node.superName?.let { loader.getByInternalName(it) }
    val fields get() = node.fields.asSequence().map { HField(this, it) }

    val methods get() = node.methods.asSequence().map { HMethod(this, it) }

    internal fun load() {
        val classNode = checkNotNull(classNode) { "classNode is null" }
        for (interfaceName in classNode.interfaces) {
            loader.getByInternalName(interfaceName)._childClasses.add(this)
        }
        if (classNode.superName != null) {
            loader.getByInternalName(classNode.superName)._childClasses.add(this)
        }
    }
}
