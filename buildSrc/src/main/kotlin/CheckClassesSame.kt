import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.aQute.bnd.service.diff.Diff
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.*
import org.slf4j.LoggerFactory
import java.io.File
import java.util.zip.ZipFile

open class CheckClassesSame : DefaultTask() {
    private val logger = LoggerFactory.getLogger("CheckClassesSame")
    @InputFile var src: File? = null
    @InputFile var dst: File? = null
    @Input var rootPackage: String? = null

    private val differences = mutableSetOf<Difference>()

    @TaskAction
    fun check() {
        val src = src ?: error("src not initialized")
        val dst = dst ?: error("dst not initialized")
        val rootPackagePath = rootPackage?.replace('.', '/') ?: error("rootPackage not initialized")

        differences.clear()

        val srcZip = ZipFile(src)
        val dstZip = ZipFile(dst)

        val srcClasses = mutableSetOf<String>()

        srcZip.entries().asSequence().forEach { srcEntry ->
            if(!srcEntry.name.endsWith(".class")) return@forEach
            if(!srcEntry.name.startsWith(rootPackagePath)) return@forEach
            srcClasses.add(srcEntry.name)
            val dstEntry = dstZip.getEntry(srcEntry.name)
            if (dstEntry == null) return@forEach addDiff(Difference.ClassOnlyInSrc(srcEntry.name.removeSuffix(".class")))
            val srcClass = readClass(srcZip.getInputStream(srcEntry).readBytes(1024))
            val dstClass = readClass(dstZip.getInputStream(dstEntry).readBytes(1024))

            checkClass(srcEntry.name.removeSuffix(".class"), srcClass, dstClass)
        }

        srcZip.entries().asSequence().forEach { dstEntry ->
            if(!dstEntry.name.endsWith(".class")) return@forEach
            if(!dstEntry.name.startsWith(rootPackagePath)) return@forEach
            if (dstEntry.name !in srcClasses)
                addDiff(Difference.ClassOnlyInDst(dstEntry.name.removeSuffix(".class")))
        }

        for (difference in differences) {
            when (difference) {
                is Difference.ClassOnlyInSrc ->
                    logger.error("${difference.name} is only in src")
                is Difference.ClassOnlyInDst ->
                    logger.error("${difference.name} is only in dst")
                is Difference.FieldOnlyInSrc ->
                    logger.error("${difference.owner}.${difference.name}:${difference.desc} is only in src")
                is Difference.FieldOnlyInDst ->
                    logger.error("${difference.owner}.${difference.name}:${difference.desc} is only in dst")
                is Difference.MethodOnlyInSrc ->
                    logger.error("${difference.owner}.${difference.name}:${difference.desc} is only in dst")
                is Difference.MethodOnlyInDst ->
                    logger.error("${difference.owner}.${difference.name}:${difference.desc} is only in dst")
                is Difference.ClassSignatureChanged ->
                    logger.error("${difference.className} signature changed. src: '${difference.src}' dst: '${difference.dst}'")
                is Difference.FieldSignatureChanged ->
                    logger.error("${difference.owner}.${difference.name}:${difference.desc} signature changed. src: '${difference.src}' dst: '${difference.dst}'")
                is Difference.MethodSignatureChanged ->
                    logger.error("${difference.owner}.${difference.name}:${difference.desc} signature changed. src: '${difference.src}' dst: '${difference.dst}'")
                is Difference.FieldAccessChanged ->
                    logger.error("${difference.owner}.${difference.name}:${difference.desc} access changed")
                is Difference.MethodAccessChanged ->
                    logger.error("${difference.owner}.${difference.name}:${difference.desc} access changed")
                is Difference.FieldValueChanged ->
                    logger.error("${difference.owner}.${difference.name}:${difference.desc} value changed")
                is Difference.MethodCodeChanged ->
                    logger.error("${difference.owner}.${difference.name}:${difference.desc} code changed")
                is Difference.AnnotationDefaultChanged ->
                    logger.error("${difference.owner}.${difference.name}:${difference.desc} annotation default changed")
            }
        }
    }

    private fun checkClass(className: String, srcClass: ClassNode, dstClass: ClassNode) {
        if (srcClass.signature != dstClass.signature)
            addDiff(Difference.ClassSignatureChanged(className, srcClass.signature, dstClass.signature))

        val srcFields = srcClass.fields.map { it.name to it.desc to it }.toMap()
        val dstFields = dstClass.fields.map { it.name to it.desc to it }.toMap()
        srcFields.zipNullable(dstFields).forEach { (k, v) ->
            val (name, desc) = k
            val (srcField, dstField) = v
            if (dstField == null) return@forEach addDiff(Difference.FieldOnlyInSrc(className, name, desc))
            if (srcField == null) return@forEach addDiff(Difference.FieldOnlyInDst(className, name, desc))
            checkField(className, name, desc, srcField, dstField)
        }
        val srcMethods = srcClass.methods.map { it.name to it.desc to it }.toMap()
        val dstMethods = dstClass.methods.map { it.name to it.desc to it }.toMap()
        srcMethods.zipNullable(dstMethods).forEach { (k, v) ->
            val (name, desc) = k
            val (srcMethod, dstMethod) = v
            if (dstMethod == null) return@forEach addDiff(Difference.MethodOnlyInSrc(className, name, desc))
            if (srcMethod == null) return@forEach addDiff(Difference.MethodOnlyInDst(className, name, desc))
            checkMethod(className, name, desc, srcMethod, dstMethod)
        }
    }

    private fun checkField(owner: String, name: String, desc: String, srcField: FieldNode, dstField: FieldNode) {
        if (srcField.access != dstField.access)
            addDiff(Difference.FieldAccessChanged(owner, name, desc))
        if (srcField.signature != dstField.signature)
            addDiff(Difference.FieldSignatureChanged(owner, name, desc, srcField.signature, dstField.signature))
        if (srcField.value != dstField.value)
            addDiff(Difference.FieldValueChanged(owner, name, desc))
    }

    private fun checkMethod(owner: String, name: String, desc: String, srcMethod: MethodNode, dstMethod: MethodNode) {
        if (srcMethod.access != dstMethod.access)
            addDiff(Difference.MethodAccessChanged(owner, name, desc))
        if (srcMethod.signature != dstMethod.signature)
            addDiff(Difference.MethodSignatureChanged(owner, name, desc, srcMethod.signature, dstMethod.signature))
        if (srcMethod.annotationDefault != dstMethod.annotationDefault)
            addDiff(Difference.AnnotationDefaultChanged(owner, name, desc))
        val srcInsns = srcMethod.instructions.iterator().asSequence().filter { it.opcode != -1 }.toList()
        val dstInsns = srcMethod.instructions.iterator().asSequence().filter { it.opcode != -1 }.toList()
        if (srcInsns.size != dstInsns.size)
            return addDiff(Difference.MethodCodeChanged(owner, name, desc))
        for ((srcInsn, dstInsn) in srcInsns.zip(dstInsns)) {
            if (!checkInsn(srcInsn, dstInsn))
                return addDiff(Difference.MethodCodeChanged(owner, name, desc))
        }
    }

    private fun checkInsn(srcInsn: AbstractInsnNode, dstInsn: AbstractInsnNode): Boolean {
        if (srcInsn.opcode != dstInsn.opcode) return false
        if (srcInsn.javaClass != dstInsn.javaClass) return false
        when (srcInsn) {
            is FieldInsnNode -> { dstInsn as FieldInsnNode
                if (srcInsn.owner != dstInsn.owner) return false
                if (srcInsn.name != dstInsn.name) return false
                if (srcInsn.desc != dstInsn.desc) return false
            }
            is IincInsnNode -> { dstInsn as IincInsnNode
                if (srcInsn.`var` != dstInsn.`var`) return false
                if (srcInsn.incr != dstInsn.incr) return false
            }
            is InsnNode -> { dstInsn as InsnNode
            }
            is IntInsnNode -> { dstInsn as IntInsnNode
                if (srcInsn.operand != dstInsn.operand) return false
            }
            is InvokeDynamicInsnNode -> { dstInsn as InvokeDynamicInsnNode
                if (srcInsn.name != dstInsn.name) return false
                if (srcInsn.desc != dstInsn.desc) return false
                if (srcInsn.bsm != dstInsn.bsm) return false
                if (!srcInsn.bsmArgs.contentEquals(dstInsn.bsmArgs)) return false
            }
            is JumpInsnNode -> { dstInsn as JumpInsnNode
                //if (srcInsn.label != dstInsn.label) return false
            }
            is LdcInsnNode -> { dstInsn as LdcInsnNode
                if (srcInsn.cst != dstInsn.cst) return false
            }
            is LookupSwitchInsnNode -> { dstInsn as LookupSwitchInsnNode
                //if (srcInsn.dflt != dstInsn.dflt) return false
                if (srcInsn.keys != dstInsn.keys) return false
                //if (srcInsn.labels != dstInsn.labels) return false
            }
            is MethodInsnNode -> { dstInsn as MethodInsnNode
                if (srcInsn.owner != dstInsn.owner) return false
                if (srcInsn.name != dstInsn.name) return false
                if (srcInsn.desc != dstInsn.desc) return false
                if (srcInsn.itf != dstInsn.itf) return false
            }
            is MultiANewArrayInsnNode -> { dstInsn as MultiANewArrayInsnNode
                if (srcInsn.desc != dstInsn.desc) return false
                if (srcInsn.dims != dstInsn.dims) return false
            }
            is TableSwitchInsnNode -> { dstInsn as TableSwitchInsnNode
                if (srcInsn.min != dstInsn.min) return false
                if (srcInsn.max != dstInsn.max) return false
                if (srcInsn.dflt != dstInsn.dflt) return false
                if (srcInsn.labels != dstInsn.labels) return false
            }
            is TypeInsnNode -> { dstInsn as TypeInsnNode
                if (srcInsn.desc != dstInsn.desc) return false
            }
            is VarInsnNode -> { dstInsn as VarInsnNode
                if (srcInsn.`var` != dstInsn.`var`) return false
            }
            else -> error("Not yet implemented: ${srcInsn.javaClass.simpleName}")
        }
        return true
    }

    private fun readClass(byteArray: ByteArray): ClassNode
            = ClassNode().apply { ClassReader(byteArray)
            .accept(this, ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES) }

    private fun <K, V> Map<K, V>.zipNullable(other: Map<K, V>): Map<K, Pair<V?, V?>> {
        val result = mutableMapOf<K, Pair<V?, V?>>()
        for ((k, v) in this) {
            result[k] = v to other[k]
        }
        for (k in (other.keys - keys)) {
            result[k] = null to other[k]
        }
        return result
    }

    private fun addDiff(diff: Difference) {
        differences.add(diff)
    }

    private sealed class Difference {
        data class ClassOnlyInSrc(val name: String) : Difference()
        data class ClassOnlyInDst(val name: String) : Difference()
        data class FieldOnlyInSrc(val owner: String, val name: String, val desc: String) : Difference()
        data class FieldOnlyInDst(val owner: String, val name: String, val desc: String) : Difference()
        data class MethodOnlyInSrc(val owner: String, val name: String, val desc: String) : Difference()
        data class MethodOnlyInDst(val owner: String, val name: String, val desc: String) : Difference()
        data class ClassSignatureChanged(val className: String, val src: String?, val dst: String?) : Difference()
        data class FieldSignatureChanged(val owner: String, val name: String, val desc: String, val src: String?, val dst: String?) : Difference()
        data class MethodSignatureChanged(val owner: String, val name: String, val desc: String, val src: String?, val dst: String?) : Difference()
        data class FieldAccessChanged(val owner: String, val name: String, val desc: String) : Difference()
        data class MethodAccessChanged(val owner: String, val name: String, val desc: String) : Difference()
        data class FieldValueChanged(val owner: String, val name: String, val desc: String) : Difference()
        data class MethodCodeChanged(val owner: String, val name: String, val desc: String) : Difference()
        data class AnnotationDefaultChanged(val owner: String, val name: String, val desc: String) : Difference()
    }
}
