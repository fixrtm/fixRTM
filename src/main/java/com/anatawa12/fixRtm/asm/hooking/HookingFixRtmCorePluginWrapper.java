package com.anatawa12.fixRtm.asm.hooking;

import com.anatawa12.fixRtm.asm.CorePluginWrapper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

// after sorting, but first
@IFMLLoadingPlugin.SortingIndex(2000)
public class HookingFixRtmCorePluginWrapper extends CorePluginWrapper {
   @Override
   protected IFMLLoadingPlugin newPlugin() {
      return new HookingFixRtmCorePlugin();
   }
}
