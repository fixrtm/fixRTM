package com.anatawa12.fixRtm.asm

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import java.io.File

@Suppress("PropertyName", "unused")
class DummyModelSetTransform : IClassTransformer {
    val ModelPackManager = Type.getObjectType("jp/ngt/rtm/modelpack/ModelPackManager")!!
    val DummyModelPackManager = Type.getObjectType("com/anatawa12/fixRtm/DummyModelPackManager")
    val ResourceState = Type.getObjectType("jp/ngt/rtm/modelpack/state/ResourceState")
    val ResourceSet = Type.getObjectType("jp/ngt/rtm/modelpack/modelset/ResourceSet")
    val ResourceType = Type.getObjectType("jp/ngt/rtm/modelpack/ResourceType")
    val String = Type.getObjectType("java/lang/String")
    val Map = Type.getObjectType("java/util/Map")

    override fun transform(name: String, transformedName: String, basicClass: ByteArray): ByteArray {
        if (transformedName == ModelPackManager.className) {
            val cr = ClassReader(basicClass)
            val cw = ClassWriter(0)
            val ca = object : ClassVisitor(Opcodes.ASM5, cw) {
                override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                    if (name == "getResourceSet" && desc == "($ResourceType$String)$ResourceSet") {
                        val mv1 = super.visitMethod(access, name, desc, signature, exceptions)
                        var l: Label? = null
                        val START = 0
                        val FirstALOAD1Was = 1
                        val OldSTART = 2
                        val IfNullLabelWas = 3
                        val IFNONNULLWas = 4
                        val IFNONNULLLabelWas = 5
                        var state = START
                        var isNewVersion = false
                        return object : MethodVisitor(ASM5, mv1) {
                            override fun visitCode() {
                                mv = null
                                super.visitCode()
                            }

                            override fun visitJumpInsn(opcode: Int, label: Label) {
                                if (state == OldSTART && opcode == IFNULL) {
                                    l = label
                                } else if (state == IfNullLabelWas && opcode == IFNONNULL) {
                                    l = label
                                    mv1.visitJumpInsn(opcode, label)
                                    mv1.visitVarInsn(ALOAD, 1)
                                    mv1.visitVarInsn(ALOAD, 2)
                                    mv1.visitMethodInsn(INVOKESTATIC, DummyModelPackManager.internalName, "getSet", "($ResourceType$String)$ResourceSet", false)
                                    mv1.visitInsn(ARETURN)
                                    mv = null
                                    state = IFNONNULLWas
                                } else {
                                    super.visitJumpInsn(opcode, label)
                                }
                            }

                            var type = 0
                            var nlocal = 0
                            var local: Array<out Any>? = arrayOf()
                            var nstack = 0
                            var stack: Array<out Any>? = arrayOf()
                            var tmp = false
                            override fun visitFrame(type: Int, nLocal: Int, local: Array<out Any>?, nStack: Int, stack: Array<out Any>?) {
                                if (state == OldSTART) return
                                println("frame state: $state, type: $type, local: $nLocal, stack: $nStack")
                                if (state == IFNONNULLWas && type == 0 && !tmp) {
                                    tmp = true
                                    this.type = type
                                    nlocal = nLocal
                                    this.local = local
                                    nstack = nStack
                                    this.stack = stack
                                    println("read stack to tmps")
                                } else if (state == IFNONNULLLabelWas && tmp) {
                                } else {
                                    mv?.visitFrame(type, nLocal, local, nStack, stack)
                                }
                            }

                            override fun visitLabel(label: Label) {
                                if (label == l) {
                                    mv = mv1
                                    l = null
                                    if (state == IFNONNULLWas) {
                                        println("write stack from tmps")
                                        tmp = false
                                        mv?.visitFrame(this.type, nlocal, this.local, 0, arrayOf())
                                    }
                                    state++
                                }
                                super.visitLabel(label)
                            }

                            override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, desc: String?) {
                                if (state == FirstALOAD1Was) {
                                    state = OldSTART
                                }
                                super.visitFieldInsn(opcode, owner, name, desc)
                            }

                            override fun visitVarInsn(opcode: Int, `var`: Int) {
                                println("op: $opcode, var: $`var`, state: $state, ")
                                if (state == START && opcode == ALOAD && `var` == 1) {
                                    state = FirstALOAD1Was
                                } else if (state == FirstALOAD1Was) {
                                    if (opcode == ASTORE && `var` == 3) {
                                        mv1.visitVarInsn(ALOAD, 1)
                                        mv1.visitMethodInsn(INVOKESTATIC, ModelPackManager.internalName, "get parent or me", "($ResourceType)$ResourceType", false)
                                        mv1.visitVarInsn(ASTORE, 3)
                                        isNewVersion = true
                                    }
                                    state = OldSTART
                                } else if (opcode == ALOAD && !isNewVersion && `var` == 1) {
                                    super.visitVarInsn(opcode, `var`)
                                    super.visitMethodInsn(INVOKESTATIC, ModelPackManager.internalName, "get parent or me", "($ResourceType)$ResourceType", false)
                                } else {
                                    super.visitVarInsn(opcode, `var`)
                                }
                            }

                            override fun visitEnd() {
                                super.visitEnd()
                                cw.visitMethod(ACC_STATIC or ACC_PUBLIC, "get parent or me", "($ResourceType)$ResourceType", null, null).apply {
                                    visitCode()
                                    visitFrame(F_FULL, 1, arrayOf(ResourceType.internalName), 0, arrayOf())
                                    visitVarInsn(ALOAD, 0)
                                    visitFieldInsn(GETFIELD, ResourceType.internalName, "parent", "$ResourceType")
                                    val ifNull = Label()
                                    visitJumpInsn(IFNULL, ifNull)
                                    visitFrame(F_FULL, 1, arrayOf(ResourceType.internalName), 0, arrayOf())
                                    visitVarInsn(ALOAD, 0)
                                    visitFieldInsn(GETFIELD, ResourceType.internalName, "parent", "$ResourceType")
                                    visitInsn(ARETURN)
                                    visitLabel(ifNull)
                                    visitFrame(F_FULL, 1, arrayOf(ResourceType.internalName), 0, arrayOf())
                                    visitVarInsn(ALOAD, 0)
                                    visitInsn(ARETURN)
                                    visitMaxs(1, 1)
                                    visitEnd()
                                }
                            }
                        }
                    }
                    return super.visitMethod(access, name, desc, signature, exceptions)
                }
            }
            cr.accept(ca, 0)
            return cw.toByteArray().apply { File("TestClass.class").writeBytes(this) }
        }
        return basicClass
    }
}

/*
jp.ngt.rtm.modelpack.state.ResourceState.getResourceSet
 */