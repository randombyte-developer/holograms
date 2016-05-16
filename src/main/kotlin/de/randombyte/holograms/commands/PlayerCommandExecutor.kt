package de.randombyte.holograms.commands

import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors

/**
 * A command that must be executed by a [Player].
 */
abstract class PlayerCommandExecutor : CommandExecutor {
    override fun execute(src: CommandSource, args: CommandContext): CommandResult? =
            if (src !is Player) {
                src.sendMessage(Text.of(TextColors.RED, "Command must be executed by a player!"))
                CommandResult.empty()
            } else executedByPlayer(src, args)

    /**
     * Gets called when this command is executed by a [Player].
     */
    abstract fun executedByPlayer(player: Player, args: CommandContext): CommandResult
}