/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

package com.anatawa12.fixRtm

import com.anatawa12.fixRtm.api.IPermissionManager
import com.anatawa12.fixRtm.asm.config.MainConfig
import net.minecraft.command.ICommandSender
import net.minecraftforge.fml.common.Loader
import java.util.*
import jp.ngt.ngtlib.util.PermissionManager as NGTPermissionManager

object PermissionManager : IPermissionManager() {
    private val permissionsBacked = mutableSetOf<String>()

    val permissions get() = Collections.unmodifiableSet(permissionsBacked)

    override fun registerPermission(permission: String) {
        permissionsBacked.add(permission)
        if (com.anatawa12.fixRtm.asm.config.MainConfig.addNegativePermissionEnabled) {
            permissionsBacked.add("negative.$permission")
        }
    }

    override fun hasPermission(player: ICommandSender, permission: String) {
        require(permission in permissions) { "permission not registerd: $player" }
        NGTPermissionManager.INSTANCE.hasPermission(player, permission)
    }

    fun registerBuiltinPermissions() {
        registerFixRtmPermissions()
        registerRTMPermissions()
        if (Loader.instance().activeModList.any { it.modId == "mcte" })
            registerMCTEPermissions()
    }

    private fun registerFixRtmPermissions() {
        if (MainConfig.addAllowAllPermissionEnabled)
            registerPermission("fixrtm.all_permit")
    }

    private fun registerRTMPermissions() {
        registerPermission("driveTrain")
        registerPermission("editVehicle")
        registerPermission("useCannon")
        registerPermission("useRazer")
        registerPermission("useGun")
        registerPermission("editRail")
        registerPermission("changeModel")
        registerPermission("editOrnament")
    }

    private fun registerMCTEPermissions() {
        registerPermission("useEditor")
    }
}
