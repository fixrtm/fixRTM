@file:JvmName("RenderHocksKt")

package com.anatawa12.fixRtm

import jp.ngt.rtm.RTMResource.*
import jp.ngt.rtm.block.tileentity.TileEntityOrnament
import jp.ngt.rtm.modelpack.ResourceType
import jp.ngt.rtm.modelpack.cfg.OrnamentConfig
import jp.ngt.rtm.modelpack.modelset.ModelSetOrnament
import jp.ngt.rtm.modelpack.modelset.ResourceSet
import net.minecraftforge.client.MinecraftForgeClient
import org.lwjgl.opengl.GL11

@Suppress("UNCHECKED_CAST")
fun renderRenderOrnament(tile: TileEntityOrnament, x: Double, y: Double, z: Double): Boolean {
    val state = tile.resourceState!!
    val modelSet = state.resourceSet!!
    if (!modelSet.isDummy) return false

    state.resourceName

    val type = state.type as ResourceType<OrnamentConfig, ModelSetOrnament>

    val cfg = modelSet.config!!
    val pass = MinecraftForgeClient.getRenderPass()
    val scale = tile.randomScale

    //modelSet.modelObj.render()

    GL11.glPushMatrix()
    GL11.glEnable(32826)
    GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5)
    GL11.glScalef(scale, scale, scale)

    val set = type.getDefalutResouceSet()



    //modelSet.modelObj.render()

    GL11.glPopMatrix()
    return true
}

val ModelPackManager = jp.ngt.rtm.modelpack.ModelPackManager.INSTANCE
fun <T : ResourceSet<*>> getResourceSet(type: ResourceType<*, *>, name: String): T
        = ModelPackManager.getResourceSet(type, name)
fun <Set : ModelSetOrnament>ResourceType<*, Set>.getDefalutResouceSet(): Set {
    var set: Set? = null
    when (name) {
        "ModelFirearm" -> set = getResourceSet(FIREARM, "40cmArtillery")
        "ModelRail" -> set = getResourceSet(RAIL, "1067mm_Wood")
        "ModelSignal" -> set = getResourceSet(SIGNAL, "4colorB")
        "ModelContainer" -> set = getResourceSet(CONTAINER, "19g_JRF_0")
        "ModelNPC" -> set = getResourceSet(NPC, "MannequinNGT01")
        "ModelWire" -> set = getResourceSet(WIRE, "BasicWireBlack")
        "SignBoard" -> set = getResourceSet(SIGNBOARD, "textures/signboard/ngt_a01.png")
        "Flag" -> set = getResourceSet(FLAG, "textures/flag/flag_RTM3Anniversary.png")
        "RRS" -> set = getResourceSet(RRS, "textures/rrs/rrs_01.png")
        "ModelConnector" -> when (subType) {
            "Relay" -> set = getResourceSet(CONNECTOR_RELAY, "Insulator01")
            "Input" -> set = getResourceSet(CONNECTOR_INPUT, "Input01")
            "Output" -> set = getResourceSet(CONNECTOR_OUTPUT, "Output01")
        }
        "ModelMachine" -> when (subType) {
            "Gate" -> set = getResourceSet(MACHINE_GATE, "CrossingGate01L")
            "Point" -> set = getResourceSet(MACHINE_POINT, "Point01M")
            "Turnstile" -> set = getResourceSet(MACHINE_TURNSTILE, "Turnstile01")
            "Vendor" -> set = getResourceSet(MACHINE_VENDOR, "Vendor01")
            "Light" -> set = getResourceSet(MACHINE_LIGHT, "SearchLight01")
            "BumpingPost" -> set = getResourceSet(MACHINE_BUMPINGPOST, "BumpingPost_Type2")
            "Antenna_Send" -> set = getResourceSet(MACHINE_ANTENNA_SEND, "ATC_01")
            "Antenna_Receive" -> set = getResourceSet(MACHINE_ANTENNA_RECEIVE, "TrainDetector_01")
        }
        "ModelOrnament" -> when (subType) {
            "Lamp" -> set = getResourceSet(ORNAMENT_LAMP, "Fluorescent01")
            "Stair" -> set = getResourceSet(ORNAMENT_STAIR, "ScaffoldStair01")
            "Scaffold" -> set = getResourceSet(ORNAMENT_SCAFFOLD, "Scaffold01")
            "Pole" -> set = getResourceSet(ORNAMENT_POLE, "LinePole01")
            "Pipe" -> set = getResourceSet(ORNAMENT_PIPE, "Pipe01")
            "Plant" -> set = getResourceSet(ORNAMENT_PLANT, "Tree01")
        }
        "ModelTrain" -> when (subType) {
            "DC" -> set = getResourceSet(TRAIN_DC, "kiha600")
            "EC" -> set = getResourceSet(TRAIN_EC, "223h")
            "CC" -> set = getResourceSet(TRAIN_CC, "koki100")
            "TC" -> set = getResourceSet(TRAIN_TC, "torpedo-car")
        }
        "ModelVehicle" -> when (subType) {
            "Car" -> set = getResourceSet(VEHICLE_CAR, "CV33")
            "Plane" -> set = getResourceSet(VEHICLE_PLANE, "NGT-1")
            "Ship" -> set = getResourceSet(VEHICLE_SHIP, "WoodBoat")
        }
    }

    return set
            ?: throw IllegalArgumentException("ResourceType($name${if (subType == null) "" else ", sub: $subType"}) not supported in fix rtm")
}
