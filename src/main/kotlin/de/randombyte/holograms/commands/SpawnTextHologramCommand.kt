package de.randombyte.holograms.commands

import de.randombyte.holograms.Hologram
import de.randombyte.holograms.Holograms
import de.randombyte.holograms.config.ConfigManager
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers

class SpawnTextHologramCommand : PermissionNeededCommandExecutor(Holograms.HOLOGRAMS_PERMISSION) {
    override fun executedWithPermission(player: Player, args: CommandContext): CommandResult {
        val text = TextSerializers.FORMATTING_CODE.deserialize(args.getOne<String>("text").get())
        val hologram = Hologram.spawn(listOf(text), player.location).orElseThrow {
            CommandException(Text.of("Couldn't spawn ArmorStand!"))
        }
        ConfigManager.addHologram(player.world, hologram)
        return CommandResult.success()
    }
}