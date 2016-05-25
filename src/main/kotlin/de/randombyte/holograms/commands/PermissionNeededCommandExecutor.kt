package de.randombyte.holograms.commands

import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text

abstract class PermissionNeededCommandExecutor(val permission: String) : PlayerCommandExecutor() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        if (!player.hasPermission(permission)) throw CommandException(Text.of("Permission '$permission' needed!"))
        return executedWithPermission(player, args)
    }

    abstract fun executedWithPermission(player: Player, args: CommandContext):CommandResult
}