package com.anatawa12.fixRtm.asm

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type.BOOLEAN_TYPE
import org.objectweb.asm.Type.INT_TYPE

@Suppress("unused")
class NPEInTickProcessQueueTransformer : IClassTransformer {
    val HocksKt = Type.getObjectType("com/anatawa12/fixRtm/HocksKt")
    val TickProcessQueue = Type.getObjectType("jp/ngt/ngtlib/event/TickProcessQueue")
    val TickProcessEntry = Type.getObjectType("jp/ngt/ngtlib/event/TickProcessEntry")
    val World = Type.getObjectType("net/minecraft/world/World")
    val List = Type.getObjectType("java/util/List")
    val Object = Type.getObjectType("java/lang/Object")

    override fun transform(name: String, transformedName: String, basicClass: ByteArray): ByteArray {
        if (transformedName == TickProcessQueue.className) {
            val cr = ClassReader(basicClass)
            val cw = ClassWriter(0)
            val ca = object : ClassVisitor(Opcodes.ASM5, cw) {
                override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                    val mv = super.visitMethod(access, name, desc, signature, exceptions)
                    if (name == "add" && (desc == "($TickProcessEntry)V"
                                    || desc == "($TickProcessEntry$INT_TYPE)V"
                                    || desc == "($TickProcessEntry$INT_TYPE$INT_TYPE)V"
                                    )) {
                        return object : MethodVisitor(ASM5, mv) {
                            override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
                                if (owner == List.internalName && name == "add" && desc == "($Object)Z") {
                                    mv.visitInsn(DUP)
                                    mv.visitVarInsn(ALOAD, 1)
                                    mv.visitMethodInsn(INVOKESTATIC, HocksKt.internalName, "eraseNullForAddTickProcessEntry", "($TickProcessEntry$TickProcessEntry)V", false)
                                }
                                super.visitMethodInsn(opcode, owner, name, desc, itf)
                            }
                        }
                    } else if (name == "onTick" && desc == "($World)V") {
                        return object : MethodVisitor(ASM5, mv) {
                            override fun visitCode() {
                                super.visitCode()
                                visitMethodInsn(INVOKESTATIC, HocksKt.internalName, "preProcess", "()V", false)
                            }
                            override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
                                var isDo = false
                                if (opcode == INVOKEINTERFACE && owner == TickProcessEntry.internalName &&
                                        name == "process" &&
                                        desc == "($World)$BOOLEAN_TYPE"){
                                    mv.visitInsn(SWAP);
                                    mv.visitInsn(DUP);
                                    mv.visitMethodInsn(INVOKESTATIC, HocksKt.internalName, "preProcess", "($TickProcessEntry)V", false)
                                    mv.visitInsn(SWAP);
                                    isDo = true
                                }
                                super.visitMethodInsn(opcode, owner, name, desc, itf)
                                if (isDo) {
                                    mv.visitInsn(DUP);
                                    mv.visitMethodInsn(INVOKESTATIC, HocksKt.internalName, "postProcess", "(Z)V", false)
                                }
                            }

                            override fun visitInsn(opcode: Int) {
                                if (opcode == RETURN) {
                                    mv.visitMethodInsn(INVOKESTATIC, HocksKt.internalName, "postProcess", "()V", false)
                                }
                                super.visitInsn(opcode)
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