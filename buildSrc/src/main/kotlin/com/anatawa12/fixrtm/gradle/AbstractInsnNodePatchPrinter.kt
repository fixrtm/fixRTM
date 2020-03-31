package com.anatawa12.fixrtm.gradle

import com.github.difflib.patch.Patch
import org.objectweb.asm.tree.*
import org.objectweb.asm.util.Printer
import kotlin.math.max

object AbstractInsnNodePatchPrinter {
    private val size = 10

    fun print(source: List<AbstractInsnNode>, patch: Patch<AbstractInsnNode>) = buildString {
        var insnIndex = 0
        for (delta in patch.deltas) {
            printNonPatches(source, insnIndex, delta.source.position)
            insnIndex = delta.source.position + delta.source.lines.size
            for (line in delta.source.lines) {
                append('-').printInsn(line)
            }
            for (line in delta.target.lines) {
                append('+').printInsn(line)
            }
        }
        val end = kotlin.math.min(insnIndex + size, source.size)
        for (line in source.subList(insnIndex, end)) {
            append(' ').printInsn(line)
        }
        if (end != source.size)
            appendln("@@@ $end ${source.size} @@@")
    }

    fun Appendable.printNonPatches(source: List<AbstractInsnNode>, insnIndex: Int, sourcePosition: Int) {
        if (insnIndex == 0) {
            val start = max(0, sourcePosition - size)
            if (start != 0)
                appendln("@@@ 0 $start @@@")
            for (line in source.subList(start, sourcePosition)) {
                append(' ').printInsn(line)
            }
        } else if (sourcePosition - insnIndex <= size * 2) {
            for (line in source.subList(insnIndex, sourcePosition)) {
                append(' ').printInsn(line)
            }
        } else {
            for (line in source.subList(insnIndex, insnIndex + size)) {
                append(' ').printInsn(line)
            }
            appendln("@@@ ${insnIndex + size} ${sourcePosition - size} @@@")
            for (line in source.subList(sourcePosition - size, sourcePosition)) {
                append(' ').printInsn(line)
            }
        }
    }


    private fun Appendable.printInsn(insn: AbstractInsnNode) {
        val insnName = Printer.OPCODES[insn.opcode]
        insn.run {
            when (this) {
                is FieldInsnNode -> {
                    appendln("$insnName $owner.$name $desc")
                }
                is IincInsnNode -> {
                    appendln("$insnName $`var` $incr")
                }
                is InsnNode -> {
                    appendln(insnName)
                }
                is IntInsnNode -> {
                    appendln("$insnName $operand")
                }
                is InvokeDynamicInsnNode -> {
                    appendln("$insnName $name$desc $bsm ${bsmArgs!!.contentToString()}")
                }
                is JumpInsnNode -> {
                    appendln("$insnName :Label")
                }
                is LdcInsnNode -> {
                    appendln("$insnName $cst")
                }
                is LookupSwitchInsnNode -> {
                    appendln("$insnName $keys")
                }
                is MethodInsnNode -> {
                    appendln("$insnName $owner.$name $desc $itf")
                }
                is MultiANewArrayInsnNode -> {
                    appendln("$insnName $desc $dims")
                }
                is TableSwitchInsnNode -> {
                    appendln("$insnName $min $max")
                }
                is TypeInsnNode -> {
                    appendln("$insnName $desc")
                }
                is VarInsnNode -> {
                    appendln("$insnName $`var`")
                }
                else -> error("Not yet implemented: ${insn.javaClass.simpleName}")
            }
        }
    }
}
