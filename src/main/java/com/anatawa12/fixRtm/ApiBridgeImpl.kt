package com.anatawa12.fixRtm

import com.anatawa12.fixRtm.api.IPermissionManager

@Suppress("DEPRECATION")
class ApiBridgeImpl : com.anatawa12.fixRtm.api.ApiBridge.IApiBridge {
    override fun getPermissions(): IPermissionManager = PermissionManager
}
