package de.randombyte.holograms

import com.google.inject.Inject
import de.randombyte.holograms.commands.ListNearbyHologramsCommand
import de.randombyte.holograms.commands.SpawnMultiLineTextHologramCommand
import de.randombyte.holograms.commands.SpawnTextHologramCommand
import de.randombyte.holograms.commands.UpdateHologramsCommand
import de.randombyte.holograms.config.ConfigManager
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text

@Plugin(id = Holograms.ID, name = Holograms.NAME, version = Holograms.VERSION, authors = arrayOf(Holograms.AUTHOR))
class Holograms @Inject constructor(val logger: Logger,
            @DefaultConfig(sharedRoot = true) val configLoader: ConfigurationLoader<CommentedConfigurationNode>) {

    companion object {
        const val NAME = "Holograms"
        const val ID = "de.randombyte.holograms"
        const val VERSION = "v0.2"
        const val AUTHOR = "RandomByte"

        lateinit var PLUGIN_SPAWN_CAUSE: Cause
        lateinit var LOGGER: Logger
    }

    init {
        Companion.LOGGER = logger
    }

    @Listener
    fun onInit(event: GameInitializationEvent) {
        PLUGIN_SPAWN_CAUSE = Cause.source(SpawnCause.builder().type(SpawnTypes.PLUGIN).build()).build()
        ConfigManager.configLoader = configLoader

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .child(CommandSpec.builder()
                        .arguments(GenericArguments.remainingJoinedStrings(Text.of("text")))
                        .executor(SpawnTextHologramCommand())
                        .description(Text.of("Creates a Hologram at your feet with the given text which may contain color codes."))
                        .build(), "create")
                .child(CommandSpec.builder()
                        .arguments(GenericArguments.integer(Text.of("numberOfLines")))
                        .executor(SpawnMultiLineTextHologramCommand())
                        .description(Text.of("Creates a Hologram with multiple lines pre-configured in the config file."))
                        .build(), "createMultiLine", "cml")
                .child(CommandSpec.builder()
                        .executor(ListNearbyHologramsCommand())
                        .description(Text.of("Lists nearby Holograms to delete them."))
                        .build(), "list")
                .child(CommandSpec.builder()
                        .executor(UpdateHologramsCommand())
                        .description(Text.of("Updates Holograms in players world to the values of the config file."))
                        .build(), "update")
                .build(), "holograms")

        logger.info("$NAME loaded: $VERSION")
    }
}