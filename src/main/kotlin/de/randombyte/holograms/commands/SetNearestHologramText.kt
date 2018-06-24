package de.randombyte.holograms.commands

import de.randombyte.holograms.api.HologramsService
import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.extensions.deserialize
import de.randombyte.kosp.extensions.executeCommand
import de.randombyte.kosp.extensions.getServiceOrFail
import de.randombyte.kosp.extensions.toText
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player

class SetNearestHologramText : PlayerExecutedCommand() {
    val errorText = "No nearby Hologram found! Please move closely or teleport to one.".toText()

    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val nearestHologram = HologramsService::class.getServiceOrFail()
                .getHolograms(player.location, 1.0).firstOrNull()?.first ?: throw CommandException(errorText)
        val text = args.getOne<String>("text").get().deserialize()

        nearestHologram.text = text

        player.executeCommand("holograms list")

        return CommandResult.success()
    }
}