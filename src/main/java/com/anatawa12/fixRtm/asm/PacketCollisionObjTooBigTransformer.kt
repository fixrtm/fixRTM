package com.anatawa12.fixRtm.asm

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type.INT_TYPE
import org.objectweb.asm.Type.VOID_TYPE
import org.objectweb.asm.commons.InstructionAdapter

@Suppress("PropertyName", "unused")
class PacketCollisionObjTooBigTransformer : IClassTransformer {
    val PacketCollisionObj = Type.getObjectType("jp/ngt/rtm/network/PacketCollisionObj")!!
    val ByteBuf = Type.getObjectType("io/netty/buffer/ByteBuf")
    val Map = Type.getObjectType("java/util/Map")
    val ResourceState = Type.getObjectType("jp/ngt/rtm/modelpack/state/ResourceState")
    val ResourceSet = Type.getObjectType("jp/ngt/rtm/modelpack/modelset/ResourceSet")
    val ResourceType = Type.getObjectType("jp/ngt/rtm/modelpack/ResourceType")
    val HocksKt = Type.getObjectType("com/anatawa12/fixRtm/HocksKt")

    override fun transform(name: String, transformedName: String, basicClass: ByteArray): ByteArray {
        if (transformedName == PacketCollisionObj.className) {
            val cr = ClassReader(basicClass)
            val cw = ClassWriter(0)
            val ca = object : ClassVisitor(ASM5, cw) {
                override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                    val mv = super.visitMethod(access, name, desc, signature, exceptions)
                    if (name == "toBytes" && desc == "($ByteBuf)$VOID_TYPE") {
                        return object : InstructionAdapter(ASM5, mv) {
                            override fun visitCode() {
                                super.visitCode()
                                load(1, OBJECT_TYPE)
                                invokestatic(HocksKt.internalName, "wrapWithDeflate", "($ByteBuf)$ByteBuf", false)
                                store(1, OBJECT_TYPE)
                            }
                            override fun areturn(t: Type?) {
                                load(1, OBJECT_TYPE)
                                invokestatic(HocksKt.internalName, "writeToDeflate", "($ByteBuf)$VOID_TYPE", false)
                                super.areturn(t)
                            }
                        }
                    } else if (name == "fromBytes" && desc == "($ByteBuf)$VOID_TYPE") {
                        return object : MethodVisitor(ASM5, mv) {
                            override fun visitCode() {
                                super.visitCode()
                                visitVarInsn(ALOAD, 1)
                                visitMethodInsn(INVOKESTATIC, HocksKt.internalName, "readFromDeflate", "($ByteBuf)$ByteBuf", false)
                                visitVarInsn(ASTORE, 1)
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
