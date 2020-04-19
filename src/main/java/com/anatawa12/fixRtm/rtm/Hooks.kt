package com.anatawa12.fixRtm.rtm

import jp.ngt.ngtlib.io.NGTLog
import jp.ngt.ngtlib.renderer.model.ModelFormatException
import jp.ngt.rtm.modelpack.ModelPackManager
import jp.ngt.rtm.modelpack.ResourceType
import jp.ngt.rtm.modelpack.modelset.ResourceSet
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World

@Suppress("unused") // Used with Transform
fun eraseNullForModelSet(inSet: ResourceSet<*>?, type: ResourceType<*, *>): ResourceSet<*> {
    if (inSet != null) return inSet
    if (type.hasSubType) {
        return ModelPackManager.INSTANCE.dummyMap[type.name]
                ?: (ModelPackManager.INSTANCE.dummyMap[type.subType])
                ?: error("ResourceType(${type.name}) and ResourceType(${type.subType}) don't have dummy ResourceSet")
    } else {
        return ModelPackManager.INSTANCE.dummyMap[type.name]
                ?: error("ResourceType(${type.name}) don't have dummyMap")
    }
}

@Suppress("unused") // Used with Transform
fun <K, V>MovingSoundMaker_loadSoundJson_nullCheck(map: Map<K, V>?, domain: String): Map<K, V> {
    if (map == null)
        throw ModelFormatException("sound.json for $domain is invalid.")
    return map
}


var BlockMarker_onMarkerActivated_player: EntityPlayer? = null

@Suppress("unused") // Used with Transform
fun BlockMarker_onMarkerActivated(player: EntityPlayer) {
    if (!player.world.isRemote)
        BlockMarker_onMarkerActivated_player = player
}

@Suppress("unused") // Used with Transform
fun sendSwitchTypeError(message: String, vararg objects: Any?) {
    val player = BlockMarker_onMarkerActivated_player
    if (player == null) {
        Exception("fixRTM Bug!!! BlockMarker_onMarkerActivated_player is not init-ed").printStackTrace()
        NGTLog.sendChatMessageToAll(message, objects)
        return
    }
    player.sendMessage(TextComponentTranslation(message, *objects))
    BlockMarker_onMarkerActivated_player = null
}

@Suppress("unused") // Used from jasm
fun fixRiderPosOnDismount_remakeAABB(aabb: AxisAlignedBB?): Boolean {
    if (aabb == null) return true
    if (!aabb.minX.isFinite()) return true
    if (!aabb.minY.isFinite()) return true
    if (!aabb.minZ.isFinite()) return true
    if (!aabb.maxX.isFinite()) return true
    if (!aabb.maxY.isFinite()) return true
    if (!aabb.maxZ.isFinite()) return true
    return false
}

@Suppress("unused") // Used from jasm
fun fixRiderPosOnDismount_calcBestBlockPos(world: World, pos: BlockPos): BlockPos {
    for (i in 0..10) {
        val pos1 = BlockPos(pos.x, pos.y + i, pos.z)
        val pos2 = BlockPos(pos.x, pos.y - i, pos.z)

        if (canBeSpawnedAt(world, pos1)) return pos1
        if (canBeSpawnedAt(world, pos2)) return pos2
    }
    return world.getTopSolidOrLiquidBlock(pos)
}

private fun canBeSpawnedAt(world: World, pos: BlockPos): Boolean {
    if (world.isOutsideBuildHeight(pos)) return false
    return world.getBlockState(pos).material == Material.AIR
            && world.getBlockState(pos.up()).material == Material.AIR
}
