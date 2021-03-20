package com.anatawa12.fixRtm.asm.preprocessing;

import com.anatawa12.fixRtm.asm.CorePluginWrapper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

// after Patch, but first
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 2)
public class PreprocessingFixRtmCorePluginWrapper extends CorePluginWrapper {
   @Override
   protected IFMLLoadingPlugin newPlugin() {
      return new PreprocessingFixRtmCorePlugin();
   }
}
