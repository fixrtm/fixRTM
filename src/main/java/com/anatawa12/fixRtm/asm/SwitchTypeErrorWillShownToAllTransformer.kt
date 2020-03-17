package com.anatawa12.fixRtm.asm

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type.BOOLEAN_TYPE
import org.objectweb.asm.Type.INT_TYPE

@Suppress("PropertyName", "unused")
class SwitchTypeErrorWillShownToAllTransformer : IClassTransformer {
    val RailMaker = Type.getObjectType("jp/ngt/rtm/rail/util/RailMaker")!!
    val SwitchType = Type.getObjectType("jp/ngt/rtm/rail/util/SwitchType")!!
    val BlockMarker = Type.getObjectType("jp/ngt/rtm/rail/BlockMarker")!!
    val NGTLog = Type.getObjectType("jp/ngt/ngtlib/io/NGTLog")!!

    val World = Type.getObjectType("net/minecraft/world/World")!!
    val EntityPlayer = Type.getObjectType("net/minecraft/entity/player/EntityPlayer")!!

    val HocksKt = Type.getObjectType("com/anatawa12/fixRtm/HocksKt")!!

    val String = Type.getObjectType("java/lang/String")!!
    val Object = Type.getObjectType("java/lang/Object")!!

    // NGTLog
    val sendChatMessageToAll = "sendChatMessageToAll"
    val sendChatMessageToAll_desc = "($String[$Object)V"

    // RailMaker
    val getSwitch = "getSwitch"
    val getSwitch_desc = "()$SwitchType"

    // BlockMarker
    val onMarkerActivated = "onMarkerActivated"
    val onMarkerActivated_desc = "($World$INT_TYPE$INT_TYPE$INT_TYPE$EntityPlayer$BOOLEAN_TYPE)$BOOLEAN_TYPE"

    // HocksKt
    val BlockMarker_onMarkerActivated = "BlockMarker_onMarkerActivated"
    val BlockMarker_onMarkerActivated_desc = "($EntityPlayer)V"

    val sendSwitchTypeError = "sendSwitchTypeError"
    val sendSwitchTypeError_desc = "($String[$Object)V"

    override fun transform(name: String, transformedName: String, basicClass: ByteArray?): ByteArray? {
        if (basicClass == null) return basicClass
        if (transformedName == RailMaker.className) {
            val cr = ClassReader(basicClass)
            val cw = ClassWriter(0)
            val ca = object : ClassVisitor(ASM5, cw) {
                override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                    if (name == getSwitch && desc == getSwitch_desc) {
                        val mv1 = super.visitMethod(access, name, desc, signature, exceptions)
                        return object : MethodVisitor(ASM5, mv1) {
                            override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
                                @Suppress("SimplifyBooleanWithConstants")
                                if (opcode == INVOKESTATIC &&
                                        owner == NGTLog.internalName &&
                                        name == sendChatMessageToAll &&
                                        desc == sendChatMessageToAll_desc &&
                                        itf == false) {
                                    super.visitMethodInsn(INVOKESTATIC, HocksKt.internalName, sendSwitchTypeError, sendSwitchTypeError_desc, false)
                                } else {
                                    super.visitMethodInsn(opcode, owner, name, desc, itf)
                                }
                            }
                        }
                    }
                    return super.visitMethod(access, name, desc, signature, exceptions)
                }
            }
            cr.accept(ca, 0)
            return cw.toByteArray()
        } else if (transformedName == BlockMarker.className) {
            val cr = ClassReader(basicClass)
            val cw = ClassWriter(0)
            val ca = object : ClassVisitor(ASM5, cw) {
                override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                    if (name == onMarkerActivated && desc == onMarkerActivated_desc) {
                        val mv1 = super.visitMethod(access, name, desc, signature, exceptions)
                        return object : MethodVisitor(ASM5, mv1) {
                            override fun visitCode() {
                                super.visitCode()
                                visitVarInsn(ALOAD, 5)
                                visitMethodInsn(INVOKESTATIC, HocksKt.internalName, BlockMarker_onMarkerActivated, BlockMarker_onMarkerActivated_desc, false)
                            }
                        }
                    }
                    return super.visitMethod(access, name, desc, signature, exceptions)
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
