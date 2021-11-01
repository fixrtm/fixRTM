/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm.api;

/**
 * internal bridge. please do not use
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class ApiBridge {
   private ApiBridge() {
   }

   @Deprecated
   public interface IApiBridge {
      IPermissionManager getPermissions();
   }

   @Deprecated
   static IApiBridge INSTANCE;

   static {
      try {
         INSTANCE = (IApiBridge) Class.forName("com.anatawa12.fixRtm.ApiBridgeImpl").newInstance();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
         throw new RuntimeException("api bridge init failed", e);
      }
   }
}
