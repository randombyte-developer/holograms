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
import org.spongepowered.api.text.serializer.TextSerializers
import java.nio.file.Path

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
                copyCallback = { hologram ->
                    if (hologram.checkIfExists(player)) {
                        ServiceUtils.getServiceOrFail(HologramsService::class).createHologram(player.location, hologram.text)
                        player.sendMessage("Copied Hologram to your location!".yellow())
                    }
                    sendHologramList(player, maxDistance, statusMessageWasSentBefore = true) // Display refreshed list
                },
                setTextFromFileCallback = { hologram ->
                    if (hologram.checkIfExists(player)) {
                        val newTextString = pluginInstance.inputFile.readText()
                        val newText = TextSerializers.FORMATTING_CODE.deserialize(newTextString)
                        hologram.text = newText
                        player.sendMessage("Hologram text set!".yellow())
                    }
                    sendHologramList(player, maxDistance, statusMessageWasSentBefore = true) // Display refreshed lis
                },
                moveCallback = { hologram ->
                    if (hologram.checkIfExists(player)) {
                        hologram.location = player.location
                        player.sendMessage("Moved Hologram!".yellow())
                    }
                    sendHologramList(player, maxDistance, statusMessageWasSentBefore = true) // Display refreshed list
                },
                deleteCallback = { hologram ->
                    if (hologram.checkIfExists(player)) {
                        hologram.remove()
                        player.sendMessage("Removed Hologram!".yellow())
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
                                    copyCallback: (Hologram) -> Unit,
                                    setTextFromFileCallback: (Hologram) -> Unit,
                                    moveCallback: (Hologram) -> Unit,
                                    deleteCallback: (Hologram) -> Unit): List<Text> = hologramsDistances.map { entry ->
        val hologram = entry.first
        val shortenedPlainText = hologram.text.toPlain().limit(4) + "…"
        "- '$shortenedPlainText' | Dis: ${entry.second.toInt()} |".toText() +
                " [TP]".yellow().action(executeCallback { teleportCallback(hologram) }) +
                " [CP]".yellow().action(executeCallback { copyCallback(hologram) }) +
                " [TEXT FROM FILE]".yellow().action(executeCallback { setTextFromFileCallback(hologram) }) +
                " [MV]".yellow().action(executeCallback { moveCallback(hologram) }) +
                " [DEL]".red().action(executeCallback { deleteCallback(hologram) })
    }

    fun Path.readText() = toFile().readText()
}