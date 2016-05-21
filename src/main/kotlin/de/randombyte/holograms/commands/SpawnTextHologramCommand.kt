package de.randombyte.holograms.commands

import de.randombyte.holograms.Hologram
import de.randombyte.holograms.config.ConfigManager
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors
import org.spongepowered.api.text.serializer.TextSerializers

/**
 * Spawns an ArmorStand with various additional data; command expects a displayedText argument
 */
class SpawnTextHologramCommand : PlayerCommandExecutor() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        val plainText = args.getOne<String>("text").get()
        val text = TextSerializers.FORMATTING_CODE.deserialize(plainText)
        val hologram = Hologram(text)
        return if (hologram.spawn(player.location)) {
            ConfigManager.addHologram(hologram)
            CommandResult.success()
        } else {
            player.sendMessage(Text.of(TextColors.RED, "Couldn't spawn ArmorStand!"))
            CommandResult.empty()
        }
    }
}