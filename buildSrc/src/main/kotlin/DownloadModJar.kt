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

    lateinit var to: File

    @TaskAction
    fun download() {
        val files = CurseAPI.files(projectId)
        if (!files.isPresent)
            error("project#$projectId files not found")

        val file = files.get()
                .filterNot { "1.12.2" !in it.gameVersionStrings() }
                .maxBy { getVersionString(it.displayName()) }
                ?: error("project#$projectId files not found")

        println("file name is: ${file.displayName()}, id: ${file.id()}")

        val url = file.downloadURL()

        val response = OkHttpClient().newCall(Request.Builder().url(url).get().build()).execute()

        if (!response.isSuccessful)
            error("http request is not succeed: $url returns ${response.code()}")

        to.parentFile.mkdirs()
        response.body()!!.byteStream().copyTo(to.outputStream())
    }

    private fun getVersionString(name: String): Version {
        val regex = """.*?([0-9]+(\.[0-9]+)+).*""".toRegex()
        val match = regex.matchEntire(name)
                ?: throw NullPointerException("Expression '$regex.matchEntire($name)' must not be null")
        return Version(match.groupValues[1])
    }

}

private inline class Version(val version: String) : Comparable<Version> {
    override fun compareTo(other: Version): Int {
        val a = version.split('.')
        val b = version.split('.')

        for (i in 0 until minOf(a.size, b.size)) {
            val res = a[i].toInt().compareTo(b[i].toInt())
            if (res != 0) return res
        }

        return 0
    }
}
