package de.randombyte.holograms.commands

import de.randombyte.holograms.Hologram
import de.randombyte.holograms.config.Config
import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.config.ConfigManager
import de.randombyte.kosp.extensions.green
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers

class SpawnTextHologramCommand(val configManager: ConfigManager<Config>) : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val text = TextSerializers.FORMATTING_CODE.deserialize(args.getOne<String>("text").get())
        val hologram = Hologram.spawn(text, player.location) ?: throw CommandException(Text.of("Couldn't spawn ArmorStand!"))
        val newConfig = configManager.get().addHologram(hologram, player.location.extent.uniqueId)
        configManager.save(newConfig)
        player.sendMessage("Hologram created!".green())

        return CommandResult.success()
    }
}