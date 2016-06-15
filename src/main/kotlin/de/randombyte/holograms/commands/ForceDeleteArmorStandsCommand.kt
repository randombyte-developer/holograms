package de.randombyte.holograms.commands

import de.randombyte.holograms.config.ConfigManager
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.entity.living.player.Player

class ForceDeleteArmorStandsCommand : PlayerCommandExecutor() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        player.getNearbyEntities(2.0).filter { it.type.equals(EntityTypes.ARMOR_STAND) }.forEach { armorStand ->
            ConfigManager.deleteHologram(armorStand.world, armorStand.uniqueId)
            armorStand.remove()
        }
        return CommandResult.success()
    }
}