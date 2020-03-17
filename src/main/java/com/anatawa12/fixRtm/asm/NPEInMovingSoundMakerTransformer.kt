package com.anatawa12.fixRtm.asm

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.*
import org.objectweb.asm.Type.VOID_TYPE

@Suppress("PropertyName", "unused")
class NPEInMovingSoundMakerTransformer : IClassTransformer {
    val MovingSoundMaker = Type.getObjectType("jp/ngt/rtm/sound/MovingSoundMaker")
    val HocksKt = Type.getObjectType("com/anatawa12/fixRtm/HocksKt")
    val Map = Type.getObjectType("java/util/Map")
    val String = Type.getObjectType("java/lang/String")

    //MovingSoundMaker
    val loadSoundJson = "loadSoundJson"
    val loadSoundJson_desc = "($String)$VOID_TYPE"

    //HocksKt
    val MovingSoundMaker_loadSoundJson_nullCheck = "MovingSoundMaker_loadSoundJson_nullCheck"
    val MovingSoundMaker_loadSoundJson_nullCheck_desc = "($Map$String)$Map"

    override fun transform(name: String?, transformedName: String?, basicClass: ByteArray?): ByteArray? {
        if (basicClass == null) return basicClass
        if (name != MovingSoundMaker.className!!) return basicClass
        val cw = ClassWriter(0)
        val cr = ClassReader(basicClass)
        cr.accept(Class(cw), 0)
        return cw.toByteArray()
    }

    inner class Class(visitor: ClassVisitor) : ClassVisitor(Opcodes.ASM5, visitor) {
        override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            val base = super.visitMethod(access, name, desc, signature, exceptions)
            if (name == loadSoundJson && desc == loadSoundJson_desc) {
                return LoadSoundJsonMethod(base)
            }
            return base
        }
    }

    inner class LoadSoundJsonMethod(visitor: MethodVisitor) : MethodVisitor(Opcodes.ASM5, visitor) {
        override fun visitVarInsn(opcode: Int, `var`: Int) {
            super.visitVarInsn(opcode, `var`)
            if (opcode == Opcodes.ALOAD && `var` == 3) {
                super.visitVarInsn(Opcodes.ALOAD, 0)
                super.visitMethodInsn(Opcodes.INVOKESTATIC, HocksKt.internalName, MovingSoundMaker_loadSoundJson_nullCheck, MovingSoundMaker_loadSoundJson_nullCheck_desc, false)
            }
        }
    }
}
