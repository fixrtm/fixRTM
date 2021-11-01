/// Copyright (c) 2021 anatawa12 and other contributors
/// This file is/was part of fixRTM, released under GNU LGPL v3 with few exceptions
/// See LICENSE at https://github.com/fixrtm/fixRTM for more details

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
