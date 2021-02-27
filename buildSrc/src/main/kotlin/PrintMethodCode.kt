import com.anatawa12.fixrtm.gradle.classHierarchy.ClassHierarchy
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.util.Textifier
import org.objectweb.asm.util.TraceClassVisitor
import java.io.File
import java.io.PrintWriter

open class PrintMethodCode : DefaultTask() {
    @InputFiles
    var files: FileTree = project.files().asFileTree

    @OutputFile
    var outTo: File? = null

    @Input
    var ofClass: String? = null

    @Input
    var methodName: String? = null

    @Input
    var methodDesc: String? = null

    @TaskAction
    fun run() {
        val classes = ClassHierarchy().apply { load(files) }
        val outTo = outTo ?: error("outTo not inited")
        val ofClass = ofClass ?: error("ofClass not inited")
        val methodName = methodName ?: error("methodName not inited")
        val methodDesc = methodDesc ?: error("methodDesc not inited")

        val theClass = classes.getByInternalName(ofClass.replace('.', '/'))
        if (!theClass.isLoaded)
            throw IllegalStateException("ofClass is not loaded")

        val method = theClass.methods.singleOrNull { it.name == methodName && it.desc == methodDesc }
            ?: throw IllegalStateException("method is not loaded")

        val node = method.node
        val printer = Textifier()
        node.accept(TraceClassVisitor(null, printer, null))
        PrintWriter(outTo.writer()).use { printer.print(it) }
    }
}
