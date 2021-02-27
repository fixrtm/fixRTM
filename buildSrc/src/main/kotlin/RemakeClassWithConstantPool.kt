import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File

open class RemakeClassWithConstantPool : DefaultTask() {
    @InputFiles
    var oldFiles: FileTree = project.files().asFileTree
    @InputFiles
    var newFiles: FileTree = project.files().asFileTree
    @OutputDirectory
    var outTo: File? = null

    @TaskAction
    fun run() {
        val oldFiles = getAllFiles(oldFiles)
        val newFiles = getAllFiles(newFiles)
        val outTo = outTo ?: error("outTo not inited")

        check((oldFiles.keys - newFiles.keys).isEmpty()) { "some files are deleted: ${oldFiles.keys - newFiles.keys}" }
        check((newFiles.keys - oldFiles.keys).isEmpty()) { "some files are added: ${newFiles.keys - oldFiles.keys}" }

        for ((newPath, newFile) in newFiles) {
            val oldFile = oldFiles[newPath]

            outTo.resolve(newPath).parentFile.mkdirs()

            if (oldFile == null) {
                newFile.copyTo(outTo.resolve(newPath), overwrite = true)
                continue
            }

            outTo.resolve(newPath).writeBytes(remakeClass(oldFile.readBytes(), newFile.readBytes()))
        }
    }

    private fun remakeClass(oldBytes: ByteArray, readBytes: ByteArray): ByteArray {
        return ClassWriter(ClassReader(oldBytes), 0)
            .also { ClassReader(readBytes).accept(it, 0) }
            .toByteArray()
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
