package de.randombyte.holograms.commands

import de.randombyte.holograms.Hologram
import de.randombyte.holograms.Holograms
import de.randombyte.holograms.config.ConfigManager
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text

class SpawnMultiLineTextHologramCommand : PermissionNeededCommandExecutor(Holograms.HOLOGRAMS_PERMISSION) {
    override fun executedWithPermission(player: Player, args: CommandContext): CommandResult {
        val numberLines = args.getOne<Int>("numberOfLines").get()
        ConfigManager.addHologram(player.world,
                Hologram.spawn(textListOfSize(numberLines), player.location).orElseThrow {
                    CommandException(Text.of("Couldn't spawn ArmorStand!"))
        })
        return CommandResult.success()
    }

    private fun textListOfSize(size: Int): List<Text> = (0..(size - 1)).map { Text.of("$it") }
}