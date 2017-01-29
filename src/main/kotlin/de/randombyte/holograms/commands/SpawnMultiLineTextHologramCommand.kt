package de.randombyte.holograms.commands

import de.randombyte.holograms.api.HologramsService
import de.randombyte.kosp.PlayerExecutedCommand
import de.randombyte.kosp.ServiceUtils
import de.randombyte.kosp.extensions.green
import de.randombyte.kosp.extensions.orNull
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers

class SpawnMultiLineTextHologramCommand : PlayerExecutedCommand() {
    companion object {
        const val SEPARATOR = "%"
    }

    override fun executedByPlayer(player: Player, args: CommandContext): CommandResult {
        // One of these two parameters is definitely not null
        val numberLines = args.getOne<Int>("numberOfLines").orNull()
        val textsString = args.getOne<String>("texts").orNull()

        val texts = if (numberLines != null) textListOfSize(numberLines) else {
            textsString!!.split(SEPARATOR).map { TextSerializers.FORMATTING_CODE.deserialize(it) }
        }

        ServiceUtils.getServiceOrFail(HologramsService::class).createMultilineHologram(player.location, texts)

        player.sendMessage("Holograms created!".green())

        return CommandResult.successCount(texts.size)
    }

    private fun textListOfSize(size: Int): List<Text> = (0..(size - 1)).map { Text.of(it) }
}