package com.anatawa12.fixRtm.asm.patching;

import com.anatawa12.fixRtm.asm.CorePluginWrapper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

// after sorting, but first
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 1)
public class PatchingFixRtmCorePluginWrapper extends CorePluginWrapper {
   @Override
   protected IFMLLoadingPlugin newPlugin() {
      return new PatchingFixRtmCorePlugin();
   }
}
