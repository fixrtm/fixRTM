import io.sigpipe.jbsdiff.Diff
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.MessageDigest

open class MakeClassBsdiffPatch : DefaultTask() {
    @InputFiles
    var oldFiles: FileTree = project.files().asFileTree
    @InputFiles
    var newFiles: FileTree = project.files().asFileTree
    @OutputDirectory
    var outTo: File? = null
    @Input
    var patchPrefix: String? = null

    @TaskAction
    fun run() {
        val oldFiles = getAllFiles(oldFiles)
        val newFiles = getAllFiles(newFiles)
        val outTo = outTo ?: error("outTo not inited")
        val patchPrefix = patchPrefix ?: error("patchPrefix not inited")

        check((oldFiles.keys - newFiles.keys).isEmpty()) { "some files are deleted: ${oldFiles.keys - newFiles.keys}" }
        check((newFiles.keys - oldFiles.keys).isEmpty()) { "some files are added: ${newFiles.keys - oldFiles.keys}" }

        val patchDir = outTo.resolve(patchPrefix)
        val sha1 = MessageDigest.getInstance("SHA-1")

        for ((newPath, newFile) in newFiles) {
            val oldFile = oldFiles[newPath] ?: continue

            val oldBytes = oldFile.readBytes()
            val newBytes = newFile.readBytes()

            val oldHashFile = patchDir.resolve("$newPath.old.sha1")
            oldHashFile.parentFile.mkdirs()
            oldHashFile.writeBytes(sha1.digest(oldBytes))

            if (oldBytes.contentEquals(newBytes))
                continue

            val bsDiffFile = patchDir.resolve("$newPath.bsdiff")
            val newHashFile = patchDir.resolve("$newPath.new.sha1")

            bsDiffFile.parentFile.mkdirs()
            newHashFile.parentFile.mkdirs()

            Diff.diff(oldBytes, newBytes, bsDiffFile.outputStream())
            newHashFile.writeBytes(sha1.digest(newBytes))
        }
    }

    private fun getAllFiles(fileTree: FileTree): Map<String, File> {
        val files = mutableMapOf<String, File>()
        fileTree.visit {
            if (isDirectory) return@visit
            files[path] = file
        }
        return files
    }
}
