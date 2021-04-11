package com.anatawa12.fixRtm.scripting.adapter

import jp.ngt.ngtlib.math.Vec3
import jp.ngt.rtm.block.tileentity.TileEntityMachineBase
import jp.ngt.rtm.block.tileentity.TileEntityMechanism
import jp.ngt.rtm.block.tileentity.TileEntityOrnament
import jp.ngt.rtm.electric.Connection
import jp.ngt.rtm.electric.TileEntityElectricalWiring
import jp.ngt.rtm.electric.TileEntitySignal
import jp.ngt.rtm.entity.npc.EntityNPC
import jp.ngt.rtm.entity.train.EntityBogie
import jp.ngt.rtm.entity.train.parts.EntityArtillery
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase
import jp.ngt.rtm.modelpack.modelset.*
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import jp.ngt.rtm.render.ActionParts
import jp.ngt.rtm.render.ModelObject
import net.minecraft.entity.Entity

interface BasicPartsRendererScript<T, MS : ModelSetBase<*>> {
    fun init(obj: MS, model: ModelObject)
    fun onRightClick(obj: T, parts: ActionParts)
    fun onRightDrag(obj: T, parts: ActionParts, move: Int)
    fun render(obj: T, pass: Int, partialTick: Float)
}

// tile entity renderers

interface WirePartsRendererScript : BasicPartsRendererScript<TileEntityElectricalWiring, ModelSetWire> {
    fun renderWireStatic(wiring: TileEntityElectricalWiring, connection: Connection, target: Vec3, partialTick: Float, pass: Int)
    fun renderWireDynamic(wiring: TileEntityElectricalWiring, connection: Connection, target: Vec3, partialTick: Float, pass: Int)
}

interface SignalPartsRendererScript : BasicPartsRendererScript<TileEntitySignal, ModelSetSignal>

interface RailPartsRendererScript : BasicPartsRendererScript<TileEntityLargeRailCore, ModelSetRail> {
    fun renderRailStatic(tileEntity: TileEntityLargeRailCore, x: Double, y: Double, z: Double, par8: Float)
    fun renderRailDynamic(tileEntity: TileEntityLargeRailCore, x: Double, y: Double, z: Double, par8: Float)
    fun shouldRenderObject(tileEntity: TileEntityLargeRailCore, objName: String, len: Int, pos: Int): Boolean
}

interface OrnamentPartsRendererScript : BasicPartsRendererScript<TileEntityOrnament, ModelSetOrnament>

interface MechanismPartsRendererScript : BasicPartsRendererScript<TileEntityMechanism, ModelSetMechanism>

interface MachinePartsRendererScript : BasicPartsRendererScript<TileEntityMachineBase, ModelSetMachine>

// entity renderers

interface FirearmPartsRendererScript : BasicPartsRendererScript<EntityArtillery, ModelSetFirearm>

interface NPCPartsRendererScript : BasicPartsRendererScript<EntityNPC, ModelSetNPC>

// for VehiclePartsRenderer
interface VehiclePartsRendererScriptBase<E: Entity> : BasicPartsRendererScript<E, ModelSetVehicleBase<*>>

interface BogiePartsRendererScript : VehiclePartsRendererScriptBase<EntityBogie>

interface VehiclePartsRendererScript : VehiclePartsRendererScriptBase<EntityVehicleBase<*>>
