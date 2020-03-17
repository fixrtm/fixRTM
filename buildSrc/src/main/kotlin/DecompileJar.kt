import com.anatawa12.fixrtm.gradle.decompileJar
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DecompileJar : DefaultTask() {
    @InputFile var jarFile: File? = null
    @OutputDirectory var sourceOutputTo: File? = null
    @InputFiles var libraries: Iterable<File> = listOf()

    @TaskAction
    fun download() {
        jarFile ?: error("jarFile not initialized") // check init
        sourceOutputTo ?: error("sourceOutputTo not initialized") // check init

        decompileJar(jarFile!!, sourceOutputTo!!, libraries)
    }
}
