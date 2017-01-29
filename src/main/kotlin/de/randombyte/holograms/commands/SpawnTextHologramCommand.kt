package de.randombyte.holograms.commands

import de.randombyte.holograms.api.HologramsService
import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.ServiceUtils
import de.randombyte.kosp.extensions.green
import de.randombyte.kosp.extensions.orNull
import de.randombyte.kosp.extensions.red
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.serializer.TextSerializers

class SpawnTextHologramCommand : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val text = TextSerializers.FORMATTING_CODE.deserialize(args.getOne<String>("text").get())
        ServiceUtils.getServiceOrFail(HologramsService::class)
                .createHologram(player.location, text).orNull() ?: throw CommandException("Couldn't spawn ArmorStand!".red())

        player.sendMessage("Hologram created!".green())
        Sponge.getCommandManager().process(player, "holograms list")

        return CommandResult.success()
    }
}