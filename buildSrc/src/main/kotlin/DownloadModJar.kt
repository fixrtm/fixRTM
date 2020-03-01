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
    @get:Input
    var projectId: Int by Delegates.notNull()

    @get:OutputFile
    lateinit var to: File

    @TaskAction
    fun download() {
        val files = CurseAPI.files(projectId)
        if (!files.isPresent)
            error("project#$projectId files not found")

        val url = files.get().last().downloadURL()

        val response = OkHttpClient().newCall(Request.Builder().url(url).get().build()).execute()

        if (!response.isSuccessful)
            error("http request is not succeed: $url returns ${response.code()}")

        to.parentFile.mkdirs()
        response.body()!!.byteStream().copyTo(to.outputStream())
    }
}
