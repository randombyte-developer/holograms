package de.randombyte.holograms.commands

import de.randombyte.holograms.config.Config
import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.config.ConfigManager
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.entity.living.player.Player

class ForceDeleteArmorStandsCommand(val configManager: ConfigManager<Config>) : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        fun Entity.isNearby(): Boolean = player.location.position.distance(this.location.position) < 2.0
        player.world
                .getEntities { it.isNearby() && it.type == EntityTypes.ARMOR_STAND }
                .forEach { armorStand ->
                    armorStand.remove()
                    val newConfig = configManager.get().deleteHologram(armorStand.uniqueId, armorStand.world.uniqueId)
                    configManager.save(newConfig)
                }
        return CommandResult.success()
    }
}