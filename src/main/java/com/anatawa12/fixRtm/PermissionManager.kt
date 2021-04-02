package com.anatawa12.fixRtm

import com.anatawa12.fixRtm.api.IPermissionManager
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
    }

    private fun registerMCTEPermissions() {
        registerPermission("useEditor")
    }
}
