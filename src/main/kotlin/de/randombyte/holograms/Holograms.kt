package de.randombyte.holograms

import com.google.inject.Inject
import de.randombyte.holograms.commands.ForceDeleteArmorStandsCommand
import de.randombyte.holograms.commands.ListNearbyHologramsCommand
import de.randombyte.holograms.commands.SpawnMultiLineTextHologramCommand
import de.randombyte.holograms.commands.SpawnTextHologramCommand
import de.randombyte.holograms.config.Config
import de.randombyte.kosp.config.ConfigManager
import de.randombyte.kosp.extensions.orNull
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.DefaultConfig
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text

@Plugin(id = Holograms.ID, name = Holograms.NAME, version = Holograms.VERSION, authors = arrayOf(Holograms.AUTHOR))
class Holograms @Inject constructor(logger: Logger,
            @DefaultConfig(sharedRoot = true) configLoader: ConfigurationLoader<CommentedConfigurationNode>) {

    companion object {
        const val NAME = "Holograms"
        const val ID = "holograms"
        const val VERSION = "v2.0.1"
        const val AUTHOR = "RandomByte"

        const val HOLOGRAMS_PERMISSION = "holograms"

        lateinit var PLUGIN_SPAWN_CAUSE: Cause
        lateinit var LOGGER: Logger
    }

    val configManager = ConfigManager(configLoader, Config::class)

    init {
        Companion.LOGGER = logger
    }

    @Listener
    fun onInit(event: GameInitializationEvent) {
        PLUGIN_SPAWN_CAUSE = Cause.source(SpawnCause.builder().type(SpawnTypes.PLUGIN).build()).build()

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .permission(HOLOGRAMS_PERMISSION)
                .executor(ListNearbyHologramsCommand(configManager))
                .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("maxDistance"))))
                .child(CommandSpec.builder()
                        .permission(HOLOGRAMS_PERMISSION)
                        .arguments(GenericArguments.remainingJoinedStrings(Text.of("text")))
                        .executor(SpawnTextHologramCommand(configManager))
                        .description(Text.of("Creates a Hologram at your feet with the given text which may contain color codes."))
                        .build(), "create")
                .child(CommandSpec.builder()
                        .permission(HOLOGRAMS_PERMISSION)
                        .arguments(GenericArguments.integer(Text.of("numberOfLines")))
                        .executor(SpawnMultiLineTextHologramCommand(configManager))
                        .description(Text.of("Creates a Hologram with multiple lines pre-configured in the config file."))
                        .build(), "createMultiLine", "cml")
                .child(CommandSpec.builder()
                        .permission(HOLOGRAMS_PERMISSION)
                        .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("maxDistance"))))
                        .executor(ListNearbyHologramsCommand(configManager))
                        .description(Text.of("Lists nearby Holograms to delete or move them."))
                        .extendedDescription(Text.of("A number can be added to the command to specify in which " +
                                "radius Holograms will be listed."))
                        .build(), "list")
                .child(CommandSpec.builder()
                        .permission(HOLOGRAMS_PERMISSION)
                        .executor(ForceDeleteArmorStandsCommand(configManager))
                        .description(Text.of("Deletes every ArmorStand(e.g. lost Holograms) in a 2 block radius."))
                        .build(), "force-delete")
                .build(), "holograms")

        updateHologramTexts()

        LOGGER.info("$NAME loaded: $VERSION")
    }

    @Listener
    fun onReload(event: GameReloadEvent) {
        updateHologramTexts()
    }

    fun updateHologramTexts() {
        val config = configManager.get()
        Sponge.getServer().worlds.forEach { world ->
            config.worlds[world.uniqueId]?.holograms?.forEach { hologram ->
                world.getEntity(hologram.key).orNull()?.offer(Keys.DISPLAY_NAME, hologram.value.text)
            }
        }
    }
}