package de.randombyte.holograms.commands

import de.randombyte.holograms.api.HologramsService
import de.randombyte.holograms.api.HologramsService.Hologram
import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.config.serializers.texttemplate.SimpleTextTemplateTypeSerializer
import de.randombyte.kosp.extensions.*
import de.randombyte.kosp.getServiceOrFail
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.service.pagination.PaginationService
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.action.TextActions.*

class ShowHologramCommand(
        val getSelectedHologram: (Player) -> Hologram,
        val getRawStringFromFile: () -> String) : PlayerExecutedCommand() {
    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        sendPaginationList(player, getSelectedHologram(player))
        return CommandResult.success()
    }

    private fun sendPaginationList(player: Player, selectHologram: Hologram) {
        getServiceOrFail(PaginationService::class).builder()
                .header("".toText())
                .contents(getHologramActionsTextList(selectHologram))
                .build().sendTo(player)
    }

    fun getHologramActionsTextList(selectedHologram: Hologram): List<Text> {
        return listOf(
                " [TP]".yellow()
                        .action(showText("Teleport to hologram".toText()))
                        .action(checkedCallbackWithRefresh(selectedHologram, "holograms.teleport") { player ->
                            player.location = selectedHologram.location
                        }),
                " [CP]".yellow()
                        .action(showText("Copy hologram to your location".toText()))
                        .action(checkedCallbackWithRefresh(selectedHologram, "holograms.copy") { player ->
                            val optHologram = getServiceOrFail(HologramsService::class)
                                    .createHologram(player.location, selectedHologram.text)
                            if (!optHologram.isPresent) player.sendMessage("Failed to copy Hologram!".red())
                        }),
                " [MV]".yellow()
                        .action(showText("Move hologram to your location".toText()))
                        .action(checkedCallbackWithRefresh(selectedHologram, "holograms.move") { player ->
                            selectedHologram.location = player.location
                        }),
                " [ST]".yellow()
                        .action(showText("Set text of hologram".toText()))
                        .action(suggestCommand("/holograms setText <text>")),
                " [TFF]".yellow()
                        .action(showText("Set text from config/holograms/input.txt".toText()))
                        .action(checkedCallbackWithRefresh(selectedHologram, "holograms.setTextFromFile") { player ->
                            val newTextString = getRawStringFromFile().removeNewLineCharacters()
                            val newText = SimpleTextTemplateTypeSerializer.deserialize(newTextString)
                            //selectedHologram.text = newText
                        }),
                " [DEL]".red()
                        .action(showText("Delete hologram".toText()))
                        .action(executePlayerCallback {

                        }),
                " [▲]".run {
                    toText()
                },
                " [▼]".run {
                    toText()
                }
        )
    }

    private fun checkedCallbackWithRefresh(
            selectedHologram: Hologram, permission: String,
            callback: (Player) -> Unit) = executePlayerCallback { player ->
        if (player.checkPermission(permission) && selectedHologram.checkIfExists(player)) callback(player)
        // Refresh
        player.executeCommand("holograms show")
    }

    private fun executePlayerCallback(callback: (Player) -> Unit) = executeCallback { source ->
        if (source !is Player) throw RuntimeException("Callback executed by a non-Player CommandSource!")
        callback(source)
    }

    fun Player.checkPermission(permission: String): Boolean = if (hasPermission(permission)) true else {
        sendMessage("You don't have the permission to do this!".red())
        false
    }

    fun Hologram.checkIfExists(player: Player): Boolean {
        val exists = exists()
        if (!exists) player.sendMessage("Hologram does not exist!".red())
        return exists
    }


    private fun String.removeNewLineCharacters() = replace("\n", "").replace("\r", "").replace("\r\n", "")
}