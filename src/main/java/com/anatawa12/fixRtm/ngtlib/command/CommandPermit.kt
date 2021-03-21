package com.anatawa12.fixRtm.ngtlib.command

import com.anatawa12.fixRtm.PermissionManager
import net.minecraft.command.CommandBase
import net.minecraft.server.MinecraftServer

fun getTabCompletions(
    server: MinecraftServer,
    args: Array<String>,
): List<String> = when (args.size) {
    1 -> CommandBase.getListOfStringsMatchingLastWord(args, "list", "myname", "add", "remove")
    2 -> when (args[0]) {
        "add", "remove" -> CommandBase.getListOfStringsMatchingLastWord(args,
            server.onlinePlayerNames.toMutableList().apply { add("-all") })
        else -> emptyList()
    }
    3 -> when (args[0]) {
        "add", "remove" -> CommandBase.getListOfStringsMatchingLastWord(args, PermissionManager.permissions)
        else -> emptyList()
    }
    else -> emptyList()
}
