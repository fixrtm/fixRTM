package com.anatawa12.fixRtm.asm

import com.anatawa12.fixRtm.Loggers
import io.sigpipe.jbsdiff.Patch
import net.minecraft.launchwrapper.IClassTransformer
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

@Suppress("unused")
class PatchApplier : IClassTransformer {
    override fun transform(name: String, transformedName: String, basicClass: ByteArray): ByteArray {
        val patch = getPatchAndSha1(name)
        if (patch == null) {
            logger.trace("no patch found for $name")
            return basicClass
        }
        logger.trace("patch found for $name")

        checkSha(patch.oldSha1, basicClass) {"sha1 digest not match for class: $name. please check your mod version"}

        if (patch.jbsdiff == null) return basicClass

        val out = ByteArrayOutputStream()
        Patch.patch(basicClass, patch.jbsdiff, out)
        val patched = out.toByteArray()

        logger.trace("patched: $name")

        if (patch.newSha1 == null) return basicClass

        checkSha(patch.newSha1, patched) {"patched sha1 digest not match for class: $name. please check your mod version"}

        return patched
    }

    private fun getPatchAndSha1(className: String): PatchAndSha1? {
        val classFilePath = "${className.replace('.', '/')}.class"
        return PatchAndSha1(
                jbsdiff = getStream("$classFilePath.bsdiff")?.readBytes(),
                oldSha1 = getStream("$classFilePath.old.sha1")?.readBytes() ?: return null,
                newSha1 = getStream("$classFilePath.new.sha1")?.readBytes()
        )
    }

    private class PatchAndSha1(
            val jbsdiff: ByteArray?,
            val oldSha1: ByteArray,
            val newSha1: ByteArray?
    )

    private inline fun checkSha(sha: ByteArray, file: ByteArray, message: () -> String) {
        val oldSha1 = sha1.get().digest(file)!!
        if (!oldSha1.contentEquals(sha)) {
            throw Exception(message())
        }
    }

    private val pathBase = "com/anatawa12/fixRtm/asm/patches"

    private fun getStream(name: String) = classLoader.getResourceAsStream("$pathBase/$name")

    private val classLoader = PatchApplier::class.java.classLoader

    private val logger = Loggers.getLogger("jasm-patch-applier")

    private val sha1 = ThreadLocal.withInitial { MessageDigest.getInstance("SHA-1") }
}
