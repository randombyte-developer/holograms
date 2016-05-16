package de.randombyte.holograms

import com.google.inject.Inject
import de.randombyte.holograms.commands.SpawnTextHologramCommand
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
import org.spongepowered.api.event.cause.NamedCause
import org.spongepowered.api.event.entity.DamageEntityEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.world.chunk.LoadChunkEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Chunk
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

@Plugin(id = Holograms.ID, name = Holograms.NAME, version = Holograms.VERSION, authors = arrayOf(Holograms.AUTHOR))
class Holograms @Inject constructor(val logger: Logger,
            @DefaultConfig(sharedRoot = true) val configLoader: ConfigurationLoader<CommentedConfigurationNode>) {

    companion object {
        const val NAME = "Holograms"
        const val ID = "de.randombyte.holograms"
        const val VERSION = "v0.1"
        const val AUTHOR = "RandomByte"

        var PLUGIN_CAUSE = Cause.of(NamedCause.source(Sponge.getPluginManager().fromInstance(this)))
        lateinit var LOGGER: Logger
    }

    init {
        Companion.LOGGER = logger
    }

    @Listener
    fun onInit(event: GameInitializationEvent) {
        ConfigManager.configLoader = configLoader

        Sponge.getCommandManager().register(this, CommandSpec.builder()
            .child(CommandSpec.builder()
                    .arguments(GenericArguments.remainingJoinedStrings(Text.of("text")))
                    .executor(SpawnTextHologramCommand())
                    .build(), "create")
            .build(), "holograms")

        logger.info("$NAME loaded: $VERSION")
    }

    fun Location<World>.inChunk(chunk: Chunk) = inExtent(chunk.world) && chunk.containsBlock(blockPosition)
    @Listener
    fun onLoadChunk(event: LoadChunkEvent) {
        ConfigManager.getItemHolograms().filter { it.location.inChunk(event.targetChunk) }.forEach { it.spawn() }
    }

    //Prevent damage on ArmorStands
    @Listener
    fun onHitArmorStand(event: DamageEntityEvent) {
        if (ConfigManager.getItemHolograms().any {
            it.location.inExtent(event.targetEntity.world) && it.armorStandUUID!!.equals(event.targetEntity.uniqueId)
        }) {
            event.isCancelled = true
        }
    }
}