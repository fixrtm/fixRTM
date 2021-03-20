package com.anatawa12.fixRtm.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

// after sorting, but first
public abstract class CorePluginWrapper implements IFMLLoadingPlugin {
   IFMLLoadingPlugin plugin;

   private IFMLLoadingPlugin getPlugin() {
      if (plugin == null) plugin = newPlugin();
      return plugin;
   }

   protected abstract IFMLLoadingPlugin newPlugin();

   @Override
   public String getModContainerClass() {
      return getPlugin().getModContainerClass();
   }

   @Override
   public String[] getASMTransformerClass() {
      return getPlugin().getASMTransformerClass();
   }

   @Nullable
   @Override
   public String getSetupClass() {
      return getPlugin().getSetupClass();
   }

   @Override
   public void injectData(Map<String, Object> data) {
      getPlugin().injectData(data);
   }

   @Override
   public String getAccessTransformerClass() {
      return getPlugin().getAccessTransformerClass();
   }
}
