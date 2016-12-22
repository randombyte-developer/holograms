package de.randombyte.holograms.commands

import de.randombyte.holograms.Hologram
import de.randombyte.holograms.config.Config
import de.randombyte.kosp.*
import de.randombyte.kosp.config.ConfigManager
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.service.pagination.PaginationService
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions
import java.util.*

class ListNearbyHologramsCommand(val configManager: ConfigManager<Config>) : PlayerExecutedCommand() {
    companion object {
        const val DEFAULT_DISTANCE = 10
    }

    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val maxDistance = args.getOne<Int>("maxDistance").value()
        sendHologramList(player, maxDistance ?: DEFAULT_DISTANCE)
        return CommandResult.success()
    }

    private fun sendHologramList(player: Player, maxDistance: Int) {
        val nearbyHolograms = getNearbyHolograms(player, maxDistance)

        val hologramTextList = getHologramTextList(nearbyHolograms,moveCallback = { hologramUUID ->
            player.world.getEntity(hologramUUID).value()?.location = player.location
            player.sendMessage("Hologram moved!".yellow())
            sendHologramList(player, maxDistance) // Display refreshed list
        }, deleteCallback = { hologramUUID ->
            player.world.getEntity(hologramUUID).value()?.remove()
            val newConfig = configManager.get().deleteHologram(hologramUUID, player.world.uniqueId)
            configManager.save(newConfig)
            player.sendMessage("Hologram deleted!".yellow())
            sendHologramList(player, maxDistance) // Display refreshed list
        })

        ServiceUtils.getServiceOrFail(PaginationService::class.java, "Could not load PaginationService!").builder()
                .title(getHeaderText(maxDistance))
                .contents(hologramTextList)
                .sendTo(player)
        }

    private fun getHeaderText(radius: Int) =
            "[CREATE]".green().action(TextActions.suggestCommand("/holograms create text")) + " | In radius $radius:"

    private fun getHologramTextList(hologramsDistances: Map<Hologram, Int>, moveCallback: (UUID) -> Unit,
                                    deleteCallback: (UUID) -> Unit): List<Text> = hologramsDistances.map {
        val shortenedPlainText = it.key.text.toPlain().cut(8)
        val uuid = it.key.armorStandUUID
        val shortenedUUIDText = uuid.toString().cut(8) + "..."
        val uuidText = uuid.toString().toText()

        "- \"$shortenedPlainText\" ".toText() +
                "UUID: $shortenedUUIDText".action(TextActions.showText(uuidText)) +
                " [MOVE]".yellow().action(TextActions.executeCallback { moveCallback.invoke(uuid) }) +
                " [DELETE]".red().action(TextActions.executeCallback { deleteCallback.invoke(uuid) })
    }

    /**
     * @return nearby holograms between [player] and [maxDistance] mapped to its distance to the [player]
     */
    private fun getNearbyHolograms(player: Player, maxDistance: Int): Map<Hologram, Int> {
        fun Entity.distanceTo(other: Entity) = location.position.distance(other.location.position)
        val playersExtent = player.location.extent

        val configHolograms = configManager.get().worlds[playersExtent.uniqueId]?.holograms ?: return emptyMap()
        val hologramEntities = configHolograms.mapNotNull { playersExtent.getEntity(it.key).value() }
        val hologramsDistances = hologramEntities.map { it to it.distanceTo(player) }
        val nearbyHolograms = hologramsDistances.filter { it.second < maxDistance }

        return nearbyHolograms.mapNotNull {
            (Hologram.fromArmorStand(it.first) ?: return@mapNotNull null) to it.second.toInt()
        }.toMap()
    }
}