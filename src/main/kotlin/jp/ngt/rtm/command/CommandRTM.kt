package jp.ngt.rtm.command

import jp.ngt.rtm.entity.train.EntityBogie
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.entity.train.parts.EntityVehiclePart
import jp.ngt.rtm.entity.train.util.FormationManager
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity
import net.minecraft.server.MinecraftServer
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
