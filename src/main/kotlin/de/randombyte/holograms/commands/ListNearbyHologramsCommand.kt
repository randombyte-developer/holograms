package de.randombyte.holograms.commands

import de.randombyte.holograms.Hologram
import de.randombyte.holograms.Holograms
import de.randombyte.holograms.config.ConfigManager
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.service.pagination.PaginationService
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions
import org.spongepowered.api.text.format.TextColors
import java.util.*

class ListNearbyHologramsCommand : PermissionNeededCommandExecutor(Holograms.HOLOGRAMS_PERMISSION) {
    override fun executedWithPermission(player: Player, args: CommandContext): CommandResult {
        sendHologramList(player)
        return CommandResult.success()
    }

    companion object {
        private fun sendHologramList(player: Player) {
            val hologramTextList = getHologramTextList(getNearbyHolograms(player, 10), moveCallback = { hologramUUID ->
                ConfigManager.getHolograms(player.world).filter { it.uuid.equals(hologramUUID) }.forEach { hologram ->
                    val topLocation = Hologram.getHologramTopLocation(player.location, hologram.lines.size)
                    hologram.lines.forEachIndexed { i, line ->
                        player.world.getEntity(line.armorStandUUID).ifPresent { it.location = topLocation.sub(0.0, i * Hologram.MULTI_LINE_SPACE, 0.0) }
                    }
                }
                player.sendMessage(Text.of(TextColors.YELLOW, "Hologram moved!"))
            }, deleteCallback = { hologramUUID ->
                Hologram.delete(player.world, hologramUUID)
                ConfigManager.deleteHologram(player.world, hologramUUID)
                player.sendMessage(Text.of(TextColors.YELLOW, "Hologram deleted!"))
                sendHologramList(player) //Display new list
            })
            if (hologramTextList.size > 0) {
                Sponge.getServiceManager().provide(PaginationService::class.java).ifPresent { it.builder()
                        .header(Text.of(TextColors.GREEN, "In 10 blocks range nearby Holograms:"))
                        .contents(hologramTextList)
                        .sendTo(player)
                }
            } else {
                player.sendMessage(Text.of(TextColors.YELLOW, "No Holograms in 10 blocks range!"))
            }
        }

        private fun getHologramTextList(holograms: List<Hologram>, moveCallback: (UUID) -> Unit,
                                        deleteCallback: (UUID) -> Unit) =
                holograms.map { hologram ->
                    Text.builder()
                            .append(Text.builder("- \"").append(hologram.lines.first().displayText).append(Text.of("\""))
                                    .onHover(TextActions.showText(Text.of(hologram.uuid.toString()))).build())
                            .append(Text.builder(" [MOVE]")
                                    .color(TextColors.YELLOW)
                                    .onClick(TextActions.executeCallback { moveCallback.invoke(hologram.uuid) })
                                    .build())
                            .append(Text.builder(" [DELETE]")
                                    .color(TextColors.RED)
                                    .onClick(TextActions.executeCallback { deleteCallback.invoke(hologram.uuid) })
                                    .build())
                            .build()
                }

        private fun getNearbyHolograms(player: Player, maxDistance: Int): List<Hologram> =
                ConfigManager.getHolograms(player.world).filter { it.lines.any { line ->
                    val optArmorStand = player.world.getEntity(line.armorStandUUID)
                    return@any optArmorStand.isPresent &&
                            optArmorStand.get().location.position.distance(player.location.position) < maxDistance
                }
        }
    }
}