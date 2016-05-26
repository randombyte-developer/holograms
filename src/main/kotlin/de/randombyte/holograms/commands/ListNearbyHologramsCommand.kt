package de.randombyte.holograms.commands

import de.randombyte.holograms.Hologram
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

class ListNearbyHologramsCommand : PermissionNeededCommandExecutor("de.randombyte.holograms") {
    override fun executedWithPermission(player: Player, args: CommandContext): CommandResult {
        sendHologramList(player)
        return CommandResult.success()
    }

    companion object {
        fun sendHologramList(player: Player) {
            val hologramTextList = getHologramTextList(getNearbyHolograms(player, 10), deleteCallback = { hologramUUID ->
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

        fun getHologramTextList(holograms: List<Pair<UUID, List<Pair<UUID, Text>>>>, deleteCallback: (UUID) -> Unit) =
                holograms.map { hologram ->
                    Text.builder()
                            .append(Text.builder("- \"").append(hologram.second[0].second).append(Text.of("\""))
                                    .onHover(TextActions.showText(Text.of(hologram.first.toString()))).build())
                            .append(Text.builder(" [DELETE]")
                                    .color(TextColors.RED)
                                    .onClick(TextActions.executeCallback { deleteCallback.invoke(hologram.first) })
                                    .build())
                            .build()
                }

        fun getNearbyHolograms(player: Player, maxDistance: Int): List<Pair<UUID, List<Pair<UUID, Text>>>> =
                ConfigManager.getHolograms(player.world).filter { it.second.any { line ->
                    val optArmorStand = player.world.getEntity(line.first)
                    return@any optArmorStand.isPresent &&
                            optArmorStand.get().location.position.distance(player.location.position) < maxDistance
                }
        }
    }
}