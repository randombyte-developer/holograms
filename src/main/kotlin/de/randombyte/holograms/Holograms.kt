package de.randombyte.holograms

import com.google.inject.Inject
import de.randombyte.holograms.api.HologramsService
import de.randombyte.holograms.commands.ListNearbyHologramsCommand
import de.randombyte.holograms.commands.SetNearestHologramText
import de.randombyte.holograms.commands.SpawnMultiLineTextHologramCommand
import de.randombyte.holograms.commands.SpawnTextHologramCommand
import de.randombyte.holograms.data.HologramData
import de.randombyte.holograms.data.HologramKeys
import de.randombyte.kosp.extensions.toText
import org.bstats.sponge.MetricsLite
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments.*
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.data.DataRegistration
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import java.nio.file.Files
import java.nio.file.Path

@Plugin(id = Holograms.ID, name = Holograms.NAME, version = Holograms.VERSION, authors = arrayOf(Holograms.AUTHOR))
class Holograms @Inject constructor(
        private val logger: Logger,
        @ConfigDir(sharedRoot = false) private val configPath: Path,
        private val pluginContainer: PluginContainer,
        val bStats: MetricsLite) {


    companion object {
        const val NAME = "Holograms"
        const val ID = "holograms"
        const val VERSION = "3.1"
        const val AUTHOR = "RandomByte"
    }

    val inputFile: Path = configPath.resolve("input.txt")

    @Listener
    fun onPreInit(event: GamePreInitializationEvent) {
        Sponge.getServiceManager().setProvider(this, HologramsService::class.java, HologramsServiceImpl())

        HologramKeys.buildKeys()

        Sponge.getDataManager().registerLegacyManipulatorIds("de.randombyte.holograms.data.HologramData", DataRegistration.builder()
                .dataClass(HologramData::class.java)
                .immutableClass(HologramData.Immutable::class.java)
                .builder(HologramData.Builder())
                .manipulatorId("holograms-data")
                .dataName("Holograms Data")
                .buildAndRegister(pluginContainer))

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

