import com.anatawa12.fixrtm.gradle.DeObfuscator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DeObfuscateJar : DefaultTask() {
    @InputFile
    var srgFile: File? = null

    @InputFile
    var obfuscatedJar: File? = null

    @OutputFile
    var deObfuscatedJar: File? = null

    @TaskAction
    fun run() {
        srgFile ?: error("srgFile not initialized")
        obfuscatedJar ?: error("obfuscatedJar not initialized")
        deObfuscatedJar ?: error("deObfuscatedJar not initialized")

        val deObfuscator = DeObfuscator(srgFile!!)
        deObfuscatedJar!!.parentFile.mkdirs()
        deObfuscator.deObfuscateJar(obfuscatedJar!!.inputStream(), deObfuscatedJar!!.outputStream())
    }
}
