package com.anatawa12.fixRtm.asm

import com.anatawa12.fixRtm.asm.hooking.HookingFixRtmCorePlugin
import com.anatawa12.fixRtm.asm.patching.PatchingFixRtmCorePlugin
import com.anatawa12.fixRtm.asm.preprocessing.PreprocessingFixRtmCorePlugin
import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraftforge.fml.relauncher.CoreModManager
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import java.io.File


@IFMLLoadingPlugin.TransformerExclusions(
    "kotlin.",
    "kotlinx.",
    "com.anatawa12.fixRtm.asm.",
    "org.jetbrains.annotations.",
    "org.intellij.lang.annotations.",
    "io.sigpipe.",
    "org.apache.commons.compress."
)
class FixRtmCorePlugin : IFMLLoadingPlugin {
    init {
        val loadCoreMod = CoreModManager::class.java.getDeclaredMethod("loadCoreMod",
            LaunchClassLoader::class.java,
            String::class.java,
            File::class.java)
        loadCoreMod.isAccessible = true

        val classLoader = Launch.classLoader
        val jar = getJarPath()

        val classes = arrayOf(
            PreprocessingFixRtmCorePlugin::class,
            PatchingFixRtmCorePlugin::class,
            HookingFixRtmCorePlugin::class,
        )
        for (clazz in classes) {
            loadCoreMod.invoke(null, classLoader, clazz.qualifiedName!!, jar)
        }
    }

    private fun getJarPath(): File? {
        val jarFile = File(FixRtmCorePlugin::class.java.protectionDomain.codeSource.location.toURI())
        if (jarFile.extension == "jar") return null
        return jarFile
    }

    override fun getModContainerClass(): String? = null

    override fun getASMTransformerClass(): Array<String> = arrayOf()

    override fun getSetupClass(): String? = null

    override fun injectData(p0: MutableMap<String, Any>?) {
    }

    override fun getAccessTransformerClass(): String? = null
}
