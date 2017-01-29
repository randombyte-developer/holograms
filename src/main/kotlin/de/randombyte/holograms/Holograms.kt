package de.randombyte.holograms

import com.google.inject.Inject
import de.randombyte.holograms.api.HologramsService
import de.randombyte.holograms.commands.ListNearbyHologramsCommand
import de.randombyte.holograms.commands.SpawnMultiLineTextHologramCommand
import de.randombyte.holograms.commands.SpawnTextHologramCommand
import de.randombyte.holograms.data.HologramData
import de.randombyte.kosp.extensions.toText
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text
import java.nio.file.Path

@Plugin(id = Holograms.ID, name = Holograms.NAME, version = Holograms.VERSION, authors = arrayOf(Holograms.AUTHOR))
class Holograms @Inject constructor(logger: Logger, @ConfigDir(sharedRoot = false) configPath: Path) {

    companion object {
        const val NAME = "Holograms"
        const val ID = "holograms"
        const val VERSION = "v2.0.1"
        const val AUTHOR = "RandomByte"

        const val HOLOGRAMS_PERMISSION = "holograms"

        lateinit var PLUGIN_SPAWN_CAUSE: Cause
        lateinit var LOGGER: Logger
    }

    init {
        Companion.LOGGER = logger
    }

    @Listener
    fun onPreInit(event : GamePreInitializationEvent) {
        Sponge.getDataManager().register(HologramData::class.java, HologramData.Immutable::class.java, HologramData.Builder())
        Sponge.getServiceManager().setProvider(this, HologramsService::class.java, HologramsServiceImpl())
    }

    @Listener
    fun onInit(event: GameInitializationEvent) {
        PLUGIN_SPAWN_CAUSE = Cause.source(SpawnCause.builder().type(SpawnTypes.PLUGIN).build()).build()

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .permission(HOLOGRAMS_PERMISSION)
                .executor(ListNearbyHologramsCommand(this))
                .arguments(GenericArguments.optional(GenericArguments.integer("maxDistance".toText())))
                .child(CommandSpec.builder()
                        .permission(HOLOGRAMS_PERMISSION)
                        .arguments(GenericArguments.remainingJoinedStrings("text".toText()))
                        .executor(SpawnTextHologramCommand())
                        .description(Text.of("Creates a Hologram at your feet with the given text which may contain color codes."))
                        .build(), "create")
                .child(CommandSpec.builder()
                        .permission(HOLOGRAMS_PERMISSION)
                        .arguments(GenericArguments.firstParsing(
                                GenericArguments.integer("numberOfLines".toText()),
                                GenericArguments.remainingJoinedStrings("texts".toText())))
                        .executor(SpawnMultiLineTextHologramCommand())
                        .description(Text.of("Creates a Hologram with multiple lines pre-configured in the config file."))
                        .build(), "createMultiLine", "cml")
                .child(CommandSpec.builder()
                        .permission(HOLOGRAMS_PERMISSION)
                        .arguments(GenericArguments.optional(GenericArguments.integer("maxDistance".toText())))
                        .executor(ListNearbyHologramsCommand(this))
                        .description(Text.of("Lists nearby Holograms to delete or move them."))
                        .extendedDescription(Text.of("A number can be added to the command to specify in which " +
                                "radius Holograms will be listed."))
                        .build(), "list")
                .build(), "holograms")

        LOGGER.info("$NAME loaded: $VERSION")
    }
}