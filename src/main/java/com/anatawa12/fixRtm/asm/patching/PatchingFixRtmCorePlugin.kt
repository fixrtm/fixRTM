package com.anatawa12.fixRtm.asm.patching

import com.anatawa12.fixRtm.asm.FileDownloader
import net.minecraft.launchwrapper.Launch
import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.Side
import java.io.File
import java.net.URL
import java.net.URLClassLoader

// after sorting, but first
@IFMLLoadingPlugin.SortingIndex(Int.MIN_VALUE + 1)
class PatchingFixRtmCorePlugin : IFMLLoadingPlugin {
    init {
        if (FMLLaunchHandler.side() == Side.SERVER) {
            val file = FileDownloader.downloadOrUseLocal(
                url = "https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.9/commons-compress-1.9.jar",
                toDir = File("./mods/fixrtm-deps/"),
                sha1 = "cc18955ff1e36d5abd39a14bfe82b19154330a34")
            val addURL = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
            addURL.isAccessible = true
            addURL.invoke(Launch.classLoader.javaClass.classLoader, file.toURI().toURL())
            Launch.classLoader.addURL(file.toURI().toURL())
        }
    }

    override fun getModContainerClass(): String? = null

    override fun getASMTransformerClass(): Array<String> = arrayOf(
        PatchApplier::class.qualifiedName!!,
    )

    override fun getSetupClass(): String? = null

    override fun injectData(p0: MutableMap<String, Any>?) {
    }

    override fun getAccessTransformerClass(): String? = null
}
