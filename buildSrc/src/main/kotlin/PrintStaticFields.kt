import com.anatawa12.fixrtm.gradle.classHierarchy.ClassHierarchy
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class PrintStaticFields : DefaultTask() {
    @InputFiles
    var files: FileTree = project.files().asFileTree

    @OutputFile
    var outTo: File? = null
    var ofClass: String? = null

    @TaskAction
    fun run() {
        val classes = ClassHierarchy().apply { load(files) }
        val outTo = outTo ?: error("outTo not inited")
        val ofClass = ofClass ?: error("ofClass not inited")

        val theClass = classes.getByInternalName(ofClass.replace('.', '/'))
        if (!theClass.isLoaded)
            throw IllegalStateException("ofClass is not loaded")

        val file = buildString {
            for (field in theClass.fields.filter { it.isStatic }) {
                appendln("name: ${field.name}")
                appendln("type: ${field.type}")
                appendln()
            }
        }

        outTo.apply { parentFile.mkdirs() }
            .writeText(file)
    }
}
