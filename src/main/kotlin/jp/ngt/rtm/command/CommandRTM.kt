package jp.ngt.rtm.command

import jp.ngt.rtm.RTMResource
import jp.ngt.rtm.entity.train.*
import jp.ngt.rtm.entity.train.parts.EntityVehiclePart
import jp.ngt.rtm.entity.train.util.FormationManager
import jp.ngt.rtm.entity.train.util.TrainState
import jp.ngt.rtm.item.ItemTrain
import jp.ngt.rtm.modelpack.ModelPackManager
import jp.ngt.rtm.modelpack.modelset.ModelSetTrain
import jp.ngt.rtm.rail.TileEntityLargeRailBase
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity
import net.minecraft.nbt.JsonToNBT
import net.minecraft.nbt.NBTException
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.text.TextComponentString

@Suppress("unused") // IDE bug: there are some references from library
class CommandRTM : CommandBase() {
    override fun getName(): String = "rtm"

    override fun getUsage(commandSender: ICommandSender): String = "commands.rtm.usage"

    inline fun <reified T> List<Entity>.killOllTypeOf() = this
        .asSequence()
        .filterIsInstance<EntityTrainBase>()
        .filter { it.isDead }
        .onEach { it.setDead() }
        .count()

    @Throws(CommandException::class)
    override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
        when (args.getOrNull(0)) {
            "delAllTrain" -> {
                val trainCount = sender.entityWorld.loadedEntityList.killOllTypeOf<EntityTrainBase>()
                var entityCount = trainCount
                entityCount += sender.entityWorld.loadedEntityList.killOllTypeOf<EntityBogie>()
                entityCount += sender.entityWorld.loadedEntityList.killOllTypeOf<EntityVehiclePart>()
                val formationMap: MutableMap<*, *> = FormationManager.getInstance().formations
                val formationCount = formationMap.size
                formationMap.clear()
                sender.sendMessage(TextComponentString("Deleted $trainCount trains."))
                sender.sendMessage(TextComponentString("Deleted $entityCount entities in total."))
                sender.sendMessage(TextComponentString("Deleted $formationCount formations."))
            }
            "twitter_tag" -> {
            }
            "door", "pan", "speed" -> {
                // changing state of nearby train

                val player = getCommandSenderAsPlayer(sender)
                if (args.size <= 1) throw CommandException("fix-rtm.rtm.commands.rtm.state.usage")
                val state = parseInt(args[1])

                val entities = sender.entityWorld.getEntitiesWithinAABB(EntityTrainBase::class.java,
                    AxisAlignedBB(player.positionVector, player.positionVector).grow(16.0))

                when (args[0]) {
                    "door" -> for (entity in entities) {
                        entity.setVehicleState(TrainState.TrainStateType.Door, state.coerceIn(-128..127).toByte())
                    }
                    "pan" -> for (entity in entities) {
                        entity.setVehicleState(TrainState.TrainStateType.Pantograph, state.coerceIn(-128..127).toByte())
                    }
                    "speed" -> for (entity in entities) {
                        entity.speed = state / 72.0f
                    }
                }
            }
            "summon" -> {
                val player = getCommandSenderAsPlayer(sender)
                if (args.size <= 5) throw CommandException("fix-rtm.rtm.commands.rtm.summon.usage")
                val world = sender.entityWorld
                val trainType = args[1]
                val modelName = args[2]
                val x = parseInt(args[3])
                val y = parseInt(args[4])
                val z = parseInt(args[5])

                val cYaw = if (args.size >= 7) parseDouble(args[6]).toFloat() else 0f
                val state = if (args.size >= 8) try { JsonToNBT.getTagFromJson(buildString(args, 7)) }
                catch (e: NBTException) { throw CommandException("commands.summon.tagError", e.message) }
                else NBTTagCompound()

                val (type, train) = when (trainType) {
                    "ModelTrain:CC" -> RTMResource.TRAIN_CC to EntityFreightCar(world, "dummy")
                    "ModelTrain:TC" -> RTMResource.TRAIN_TC to EntityTanker(world, "dummy")
                    "ModelTrain:EC" -> RTMResource.TRAIN_EC to EntityTrainElectricCar(world, "dummy")
                    "ModelTrain:Test" -> RTMResource.TRAIN_TEST to EntityTrainTest(world, "dummy")
                    "ModelTrain:DC" -> RTMResource.TRAIN_DC to EntityTrainDieselCar(world, "dummy")
                    else -> throw CommandException("fix-rtm.rtm.commands.rtm.summon.no-type", trainType)
                }

                val modelSet = ModelPackManager.INSTANCE.getResourceSet<ModelSetTrain>(type, modelName)

                if (modelSet.isDummy || modelSet.config.subType != type.subType)
                    throw CommandException("fix-rtm.rtm.commands.rtm.summon.model-not-found", modelName, trainType)

                val railMap = TileEntityLargeRailBase.getRailMapFromCoordinates(world, player, x + .5, y + .5, z + .5)
                    ?: throw CommandException("fix-rtm.rtm.commands.rtm.summon.rail-not-found", "$x, $y, $z")

                // check obstacle
                if (!ItemTrain.checkObstacle(modelSet.config, player, world, x, y, z, railMap)) {
                    throw CommandException("commands.summon.failed")
                }

                val pr = ItemTrain.computePosRotation(railMap, x, z, cYaw)

                train.setPositionAndRotation(pr.posX, pr.posY, pr.posZ, pr.yaw, pr.pitch)
                train.resourceState.readFromNBT(state)
                train.resourceState.resourceName = modelName
                train.spawnTrain(world)
                train.updateResourceState()
                notifyCommandListener(sender, this, "commands.summon.success")
            }
            "dismount" -> {
                getCommandSenderAsPlayer(sender).dismountRidingEntity()
            }
            else -> {
                sender.sendMessage(TextComponentString("/rtm delAllTrain : Delete all train"))
                sender.sendMessage(TextComponentString("/rtm dismount : Dismount player from vehicle"))
            }
        }
    }
}
