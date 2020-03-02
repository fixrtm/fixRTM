package com.anatawa12.fixRtm.asm

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.Remapper

@Suppress("PropertyName", "unused")
class ModelPackConstructThreadTransformer : IClassTransformer {
    val ModelPackConstructThread = Type.getObjectType("jp/ngt/rtm/modelpack/init/ModelPackConstructThread")!!
    val ModelPackLoadThread = Type.getObjectType("jp/ngt/rtm/modelpack/init/ModelPackLoadThread")!!
    val ExModelPackConstructThread = Type.getObjectType("com/anatawa12/fixRtm/modelpcakInit/ExModelPackConstructThread")!!

    override fun transform(name: String, transformedName: String, basicClass: ByteArray?): ByteArray? {
        if (basicClass == null) return basicClass
        if (transformedName == ModelPackLoadThread.className) {
            val cr = ClassReader(basicClass)
            val cw = ClassWriter(0)
            val mapper = object : Remapper() {
                override fun map(typeName: String?): String? {
                    if (typeName == ModelPackConstructThread.internalName)
                        return ExModelPackConstructThread.internalName
                    return typeName
                }
            }
            cr.accept(ClassRemapper(cw, mapper), 0)
            return cw.toByteArray()
        }
        return basicClass
    }
}
