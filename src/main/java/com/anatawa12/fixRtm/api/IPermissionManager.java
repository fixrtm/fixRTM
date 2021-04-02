package com.anatawa12.fixRtm.api;

import net.minecraft.command.ICommandSender;

/**
 * the endpoint for permissions.
 * This is not stable for implementation, just for use
 */
public abstract class IPermissionManager {
   @SuppressWarnings("deprecation")
   IPermissionManager INSTANCE = ApiBridge.INSTANCE.getPermissions();

   public abstract void registerPermission(String permission);

   public abstract void hasPermission(ICommandSender player, String permission);
}
