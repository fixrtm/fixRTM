import com.therandomlabs.curseapi.CurseAPI
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.properties.Delegates

open class DownloadModJar : DefaultTask() {
    var projectId: Int by Delegates.notNull()

    @OutputFile lateinit var to: File
    @Input lateinit var version: String

    @TaskAction
    fun download() {
        version // check init
        to // check init

        val files = CurseAPI.files(projectId)
        if (!files.isPresent)
            error("project#$projectId files not found")

        val file = files.get()
                .filterNot { "1.12.2" !in it.gameVersionStrings() }
                .filter { version in it.displayName() }
                .singleOrNull()
                ?: error("project#$projectId version $version not found or found two or more")

        println("file name is: ${file.displayName()}, id: ${file.id()}")

        val url = file.downloadURL()

        val response = OkHttpClient().newCall(Request.Builder().url(url).get().build()).execute()

        if (!response.isSuccessful)
            error("http request is not succeed: $url returns ${response.code()}")

        to.parentFile.mkdirs()
        response.body()!!.byteStream().copyTo(to.outputStream())
    }

}
