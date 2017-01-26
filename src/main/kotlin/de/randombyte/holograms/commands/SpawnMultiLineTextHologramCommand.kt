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

class SpawnMultiLineTextHologramCommand(val configManager: ConfigManager<Config>) : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val numberLines = args.getOne<Int>("numberOfLines").get()
        val holograms = Hologram.spawn(textListOfSize(numberLines), player.location) ?:
                throw CommandException(Text.of("Couldn't spawn ArmorStand!"))

        val newConfig = configManager.get().apply {
            holograms.forEach {
                addHologram(it, player.location.extent.uniqueId)
            }
        }
        configManager.save(newConfig)
        player.sendMessage("Holograms created!".green())

        return CommandResult.successCount(numberLines)
    }

    private fun textListOfSize(size: Int): List<Text> = (0..(size - 1)).map { Text.of(it) }
}