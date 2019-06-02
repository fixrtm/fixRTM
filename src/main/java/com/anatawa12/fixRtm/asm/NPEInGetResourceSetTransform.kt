package com.anatawa12.fixRtm.asm

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*

@Suppress("PropertyName", "unused")
class NPEInGetResourceSetTransform : IClassTransformer {
    val ModelPackManager = Type.getObjectType("jp/ngt/rtm/modelpack/ModelPackManager")!!
    val ExModelPackManager = Type.getObjectType("com/anatawa12/fixRtm/ExModelPackManager")
    val Map = Type.getObjectType("java/util/Map")
    val ResourceState = Type.getObjectType("jp/ngt/rtm/modelpack/state/ResourceState")
    val ResourceSet = Type.getObjectType("jp/ngt/rtm/modelpack/modelset/ResourceSet")
    val ResourceType = Type.getObjectType("jp/ngt/rtm/modelpack/ResourceType")
    val HocksKt = Type.getObjectType("com/anatawa12/fixRtm/HocksKt")

    override fun transform(name: String, transformedName: String, basicClass: ByteArray): ByteArray {
        if (transformedName == ModelPackManager.className) {
            val cr = ClassReader(basicClass)
            val cw = ClassWriter(0)
            val ca = object : ClassVisitor(Opcodes.ASM5, cw) {
                override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor {
                    if (name == "dummyMap") {
                        return super.visitField(access and (ACC_PRIVATE.inv()) or ACC_PUBLIC, name, desc, signature, value)
                    }
                    return super.visitField(access, name, desc, signature, value)
                }
            }
            cr.accept(ca, 0)
            return cw.toByteArray()
        } else if (transformedName == ExModelPackManager.className) {
            val cr = ClassReader(basicClass)
            val cw = ClassWriter(0)
            val ca = object : ClassVisitor(Opcodes.ASM5, cw) {
                override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                    val mv = super.visitMethod(access, name, desc, signature, exceptions)
                    if (name == "getDummyMap" && desc == "()$Map") {
                        mv.apply {
                            visitCode()
                            visitFieldInsn(GETSTATIC, ModelPackManager.internalName, "INSTANCE", "$ModelPackManager")
                            visitFieldInsn(GETFIELD, ModelPackManager.internalName, "dummyMap", "$Map")
                            visitInsn(ARETURN)
                            visitEnd()
                        }
                        return DummyMethodVisitor
                    } else if (name == "setDummyMap" && desc == "($Map)V") {
                        mv.apply {
                            visitCode()
                            visitFieldInsn(GETSTATIC, ModelPackManager.internalName, "INSTANCE", "$ModelPackManager")
                            visitVarInsn(ALOAD, 1)
                            visitFieldInsn(PUTFIELD, ModelPackManager.internalName, "dummyMap", "$Map")
                            visitInsn(RETURN)
                            visitEnd()
                        }
                        return DummyMethodVisitor
                    }
                    return mv
                }
            }
            cr.accept(ca, 0)
            return cw.toByteArray()
        } else if (transformedName == ResourceState.className) {
            val cr = ClassReader(basicClass)
            val cw = ClassWriter(0)
            val ca = object : ClassVisitor(Opcodes.ASM5, cw) {
                override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                    val mv = super.visitMethod(access, name, desc, signature, exceptions)
                    if (name == "getResourceSet" && desc == "()$ResourceSet") {
                        return object : MethodVisitor(ASM5, mv) {
                            override fun visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) {
                                if (opcode == PUTFIELD
                                        && owner == ResourceState.internalName
                                        && name == "modelSet"
                                        && desc == ResourceSet.descriptor) {
                                    visitVarInsn(ALOAD, 0)
                                    visitFieldInsn(GETFIELD, ResourceState.internalName, "type", "$ResourceType")
                                    visitMethodInsn(INVOKESTATIC, HocksKt.internalName, "eraseNullForModelSet", "($ResourceSet$ResourceType)$ResourceSet", false)
                                }
                                super.visitFieldInsn(opcode, owner, name, desc)
                            }
                        }
                    }
                    return mv
                }
            }
            cr.accept(ca, 0)
            return cw.toByteArray()
        }
        return basicClass
    }
}

/*
jp.ngt.rtm.modelpack.state.ResourceState.getResourceSet
 */