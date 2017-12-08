package de.randombyte.holograms

import com.google.inject.Inject
import de.randombyte.holograms.api.HologramsService
import de.randombyte.holograms.commands.ListNearbyHologramsCommand
import de.randombyte.holograms.commands.SetNearestHologramText
import de.randombyte.holograms.commands.SpawnMultiLineTextHologramCommand
import de.randombyte.holograms.commands.SpawnTextHologramCommand
import de.randombyte.holograms.data.HologramData
import de.randombyte.kosp.bstats.BStats
import de.randombyte.kosp.extensions.toText
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments.*
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.data.DataRegistration
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.EventContext
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.plugin.PluginManager
import java.nio.file.Files
import java.nio.file.Path

@Plugin(id = Holograms.ID, name = Holograms.NAME, version = Holograms.VERSION, authors = arrayOf(Holograms.AUTHOR))
class Holograms @Inject constructor(
        val logger: Logger,
        @ConfigDir(sharedRoot = false) val configPath: Path,
        val bStats: BStats) {


    companion object {
        const val NAME = "Holograms"
        const val ID = "holograms"
        const val VERSION = "2.3"
        const val AUTHOR = "RandomByte"
    }

    val inputFile: Path = configPath.resolve("input.txt")
    @Listener
    fun onPreInit(event: GamePreInitializationEvent) {

        val spawnCause = Cause.builder().append(SpawnTypes.PLUGIN).build(EventContext.empty())
        Sponge.getServiceManager().setProvider(this, HologramsService::class.java, HologramsServiceImpl(spawnCause))
        /*Sponge.getDataManager().register(,*//* ,*//* )*/
        DataRegistration.builder()
                .dataClass(HologramData::class.java)
                .immutableClass(HologramData.Immutable::class.java)
                .builder(HologramData.Builder())
                .manipulatorId(Holograms.AUTHOR+"-"+Holograms.ID)
                .dataName(Holograms.AUTHOR+" "+Holograms.NAME)
                .buildAndRegister(Sponge.getPluginManager().getPlugin(Holograms.ID).get())

    }

    @Listener
    fun onInit(event: GameInitializationEvent) {
        inputFile.safelyCreateFile()

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .permission("holograms.list")
                .executor(ListNearbyHologramsCommand(this))
                .arguments(optional(integer("maxDistance".toText())))
                .child(CommandSpec.builder()
                        .permission("holograms.create")
                        .arguments(remainingJoinedStrings("text".toText()))
                        .executor(SpawnTextHologramCommand())
                        .build(), "create")
                .child(CommandSpec.builder()
                        .permission("holograms.createMultiLine")
                        .arguments(seq(
                                doubleNum("verticalSpace".toText()),
                                firstParsing(
                                        integer("numberOfLines".toText()),
                                        remainingJoinedStrings("texts".toText())
                                )
                        ))
                        .executor(SpawnMultiLineTextHologramCommand())
                        .build(), "createMultiLine", "cml")
                .child(CommandSpec.builder()
                        .permission("holograms.list")
                        .arguments(optional(integer("maxDistance".toText())))
                        .executor(ListNearbyHologramsCommand(this))
                        .build(), "list")
                .child(CommandSpec.builder()
                        .permission("holograms.setText")
                        .arguments(remainingJoinedStrings("text".toText()))
                        .executor(SetNearestHologramText())
                        .build(), "setText")
                .build(), "holograms")

        logger.info("$NAME loaded: $VERSION")
    }

    @Listener
    fun onReload(event: GameReloadEvent) {
        inputFile.safelyCreateFile()
    }

    private fun Path.safelyCreateFile() {
        if (!Files.exists(this)) {
            Files.createDirectories(this.parent)
            Files.createFile(this)
        }
    }
}

