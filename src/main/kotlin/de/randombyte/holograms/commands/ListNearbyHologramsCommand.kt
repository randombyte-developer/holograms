package de.randombyte.holograms.commands

import de.randombyte.holograms.Holograms
import de.randombyte.holograms.api.HologramsService
import de.randombyte.holograms.api.HologramsService.Hologram
import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.ServiceUtils
import de.randombyte.kosp.extensions.*
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.scheduler.Task
import org.spongepowered.api.service.pagination.PaginationService
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions.executeCallback
import org.spongepowered.api.text.action.TextActions.suggestCommand

class ListNearbyHologramsCommand(val pluginInstance: Holograms) : PlayerExecutedCommand() {
    companion object {
        const val DEFAULT_DISTANCE = 10
        const val CHAT_MAX_LINES_SEEN = 20
    }

    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val maxDistance = args.getOne<Int>("maxDistance").orNull()
        sendHologramList(player, maxDistance ?: DEFAULT_DISTANCE)
        return CommandResult.success()
    }

    private fun sendHologramList(player: Player, maxDistance: Int, statusMessageWasSentBefore: Boolean = false) {
        val nearbyHolograms = ServiceUtils.getServiceOrFail(HologramsService::class).getHolograms(player.location, maxDistance.toDouble())

        fun Hologram.checkIfExists(player: Player): Boolean {
            val exists = doesExist()
            if (!exists) player.sendMessage("Hologram does not exist!".red())
            return exists
        }

        val hologramTextList = getHologramTextList(nearbyHolograms,
                teleportCallback = { hologram ->
                    if (hologram.checkIfExists(player)) {
                        player.location = hologram.location
                        player.sendMessage("Teleported to Hologram!".yellow())
                    }
                    sendHologramList(player, maxDistance, statusMessageWasSentBefore = true) // Display refreshed list
                },
                moveCallback = { hologram ->
                    if (hologram.checkIfExists(player)) {
                        hologram.location = player.location
                        player.sendMessage("Hologram moved!".yellow())
                    }
                    sendHologramList(player, maxDistance, statusMessageWasSentBefore = true) // Display refreshed list
                },
                deleteCallback = { hologram ->
                    if (hologram.checkIfExists(player)) {
                        hologram.remove()
                        player.sendMessage("Hologram removed!".yellow())
                    }

                    // Delay displaying the Holograms to allow the game to remove the deleted Hologram
                    Task.builder().delayTicks(1).execute { ->
                        sendHologramList(player, maxDistance, statusMessageWasSentBefore = true) // Display refreshed list
                    }.submit(pluginInstance)
                }
        )

        val linesPerPage = if (statusMessageWasSentBefore) CHAT_MAX_LINES_SEEN - 1 else {
            player.sendMessage(Text.EMPTY) // clear line above the following list
            CHAT_MAX_LINES_SEEN
        }
        ServiceUtils.getServiceOrFail(PaginationService::class).builder()
                .linesPerPage(linesPerPage)
                .title(getHeaderText(maxDistance))
                .contents(hologramTextList)
                .sendTo(player)
        }

    private fun getHeaderText(radius: Int) = "[CREATE]".green().action(suggestCommand("/holograms create text")) + " | In radius $radius:"

    private fun getHologramTextList(hologramsDistances: List<Pair<Hologram, Double>>,
                                    teleportCallback: (Hologram) -> Unit,
                                    moveCallback: (Hologram) -> Unit,
                                    deleteCallback: (Hologram) -> Unit): List<Text> = hologramsDistances.map { entry ->
        val hologram = entry.first
        val shortenedPlainText = hologram.text.toPlain().limit(8)
        "- \"$shortenedPlainText\" | Distance: ${entry.second.toInt()} |".toText() +
                " [TELEPORT] ".yellow().action(executeCallback { teleportCallback(hologram) }) +
                " [MOVE]".yellow().action(executeCallback { moveCallback(hologram) }) +
                " [DELETE]".red().action(executeCallback { deleteCallback(hologram) })
    }
}