package com.anatawa12.fixRtm.asm;

import com.anatawa12.fixRtm.asm.hooking.HookingFixRtmCorePluginWrapper;
import com.anatawa12.fixRtm.asm.mckt.MCKTResolver;
import com.anatawa12.fixRtm.asm.patching.PatchingFixRtmCorePluginWrapper;
import com.anatawa12.fixRtm.asm.preprocessing.PreprocessingFixRtmCorePluginWrapper;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions({
   "kotlin.",
   "kotlinx.",
   "com.anatawa12.fixRtm.asm.",
   "org.jetbrains.annotations.",
   "org.intellij.lang.annotations.",
   "io.sigpipe.",
   "org.apache.commons.compress."
})
public class FixRtmCorePlugin implements IFMLLoadingPlugin {
   public FixRtmCorePlugin() throws NoSuchMethodException, URISyntaxException, InvocationTargetException, IllegalAccessException {
      MCKTResolver.requestResolve();
      Method loadCoreMod = CoreModManager.class.getDeclaredMethod("loadCoreMod",
         LaunchClassLoader.class,
         String.class,
         File.class);
      loadCoreMod.setAccessible(true);

      LaunchClassLoader classLoader = Launch.classLoader;
      File jar = getJarPath();

      Class<?>[] classes = new Class<?>[]{
         PreprocessingFixRtmCorePluginWrapper.class,
         PatchingFixRtmCorePluginWrapper.class,
         HookingFixRtmCorePluginWrapper.class,
      };

      for (Class<?> clazz : classes) {
         loadCoreMod.invoke(null, classLoader, clazz.getName(), jar);
      }
   }

   private File getJarPath() throws URISyntaxException {
      File jarFile = new File(FixRtmCorePlugin.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      if (!jarFile.getName().contains(".")) return null;
      if (!jarFile.getName().substring(jarFile.getName().lastIndexOf('.')).equals("jar")) return null;
      return jarFile;
   }

   @Override
   public String getModContainerClass() {
      return null;
   }

   @Override
   public String[] getASMTransformerClass() {
      return new String[0];
   }

   @Nullable
   @Override
   public String getSetupClass() {
      return null;
   }

   @Override
   public void injectData(Map<String, Object> data) {
   }

   @Override
   public String getAccessTransformerClass() {
      return null;
   }
}
