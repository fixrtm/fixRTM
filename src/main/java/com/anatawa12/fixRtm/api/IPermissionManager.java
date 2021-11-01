/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

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
