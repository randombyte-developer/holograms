package de.randombyte.holograms.commands

import de.randombyte.holograms.Hologram
import de.randombyte.holograms.config.ConfigManager
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers

/**
 * Spawns an ArmorStand with various additional data; command expects a displayedText argument
 */
class SpawnTextHologramCommand : PlayerCommandExecutor() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val plainText = args.getOne<String>("text").get()
        val text = TextSerializers.FORMATTING_CODE.deserialize(plainText)
        val hologram = Hologram.spawn(listOf(text), player.location)
        return if (hologram.isPresent) {
            ConfigManager.addHologram(player.world, hologram.get())
            CommandResult.success()
        } else {
            throw CommandException(Text.of("Couldn't spawn ArmorStand!"))
        }
    }
}