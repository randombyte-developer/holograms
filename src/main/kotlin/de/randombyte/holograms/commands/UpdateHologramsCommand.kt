package de.randombyte.holograms.commands

import de.randombyte.holograms.Holograms
import de.randombyte.holograms.config.ConfigManager
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors

class UpdateHologramsCommand : PermissionNeededCommandExecutor(Holograms.HOLOGRAMS_PERMISSION) {
    override fun executedWithPermission(player: Player, args: CommandContext): CommandResult {
        ConfigManager.getHolograms(player.world).forEach { it.second.forEach { line ->
            player.world.getEntity(line.armorStandUUID).ifPresent { it.offer(Keys.DISPLAY_NAME, line.displayText) }
        }}
        player.sendMessage(Text.of(TextColors.GREEN, "Updated Holograms!"))
        return CommandResult.success()
    }
}