package com.anatawa12.fixRtm.asm

import net.minecraft.launchwrapper.Launch
import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.Side
import java.io.File
import java.net.URL
import java.net.URLClassLoader


@IFMLLoadingPlugin.TransformerExclusions(
        "kotlin",
        "com.anatawa12.fixRtm.asm",
        "org.jetbrains.annotations",
        "org.intellij.lang.annotations",
        "io.sigpipe",
        "org.apache.commons.compress"
)
class FixRtmCorePlugin : IFMLLoadingPlugin {
    init {
        if (FMLLaunchHandler.side() == Side.SERVER) {
            val url = "https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.9/commons-compress-1.9.jar"
            val file = File("./mods/fixrtm-deps/${url.substringAfterLast('/')}").apply { parentFile.mkdirs() }
            if (!file.exists())
                URL(url).openStream().use { uis ->
                    file.outputStream().buffered().use { fos ->
                        uis.copyTo(fos)
                    }
                }
            val addURL = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
            addURL.isAccessible = true
            addURL.invoke(Launch.classLoader.javaClass.classLoader, file.toURI().toURL())
            Launch.classLoader.addURL(file.toURI().toURL())
        }
        Launch.classLoader.registerTransformer("com.anatawa12.fixRtm.asm.PatchApplier")
    }

    override fun getModContainerClass(): String? = null

    override fun getASMTransformerClass(): Array<String>? = arrayOf(
            "com.anatawa12.fixRtm.asm.PreprocessorTransformer",
    )

    override fun getSetupClass(): String? = null

    override fun injectData(p0: MutableMap<String, Any>?){
    }

    override fun getAccessTransformerClass(): String? = null
}
