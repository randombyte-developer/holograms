package de.randombyte.holograms

import com.flowpowered.math.vector.Vector3d
import com.google.inject.Inject
import de.randombyte.holograms.api.HologramsService
import de.randombyte.holograms.api.HologramsService.Hologram
import de.randombyte.holograms.commands.*
import de.randombyte.holograms.data.HologramTextTemplateData
import de.randombyte.holograms.data.HologramUpdateIntervalData
import de.randombyte.holograms.data.HologramData
import de.randombyte.kosp.bstats.BStats
import de.randombyte.kosp.extensions.*
import de.randombyte.kosp.getServiceOrFail
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandException
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.args.GenericArguments.*
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes
import org.spongepowered.api.event.entity.MoveEntityEvent
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GamePreInitializationEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.text.action.TextActions
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Plugin(id = Holograms.ID, name = Holograms.NAME, version = Holograms.VERSION, authors = arrayOf(Holograms.AUTHOR))
class Holograms @Inject constructor(
        val logger: Logger,
        @ConfigDir(sharedRoot = false) val configPath: Path,
        val bStats: BStats) {

    companion object {
        const val NAME = "Holograms"
        const val ID = "holograms"
        const val VERSION = "2.2"
        const val AUTHOR = "RandomByte"

        private val noHologramSelectedErrorText = "You didn't select a Hologram! Do it with ".red() +
                "/holograms list".aqua()
                        .action(TextActions.runCommand("holograms list"))
                        .action(TextActions.showText("Click here".toText()))
    }

    val inputFile: Path = configPath.resolve("input.txt")

    // <PlayerUUID, ArmorStandUUID>
    private val selectedHolograms: MutableMap<UUID, UUID> = mutableMapOf()

    @Listener
    fun onPreInit(event: GamePreInitializationEvent) {
        val spawnCause = Cause.source(SpawnCause.builder().type(SpawnTypes.PLUGIN).build()).build()

        Sponge.getServiceManager().setProvider(this, HologramsService::class.java, HologramsServiceImpl(spawnCause))

        Sponge.getDataManager().run {
            register(HologramData::class.java, HologramData.Immutable::class.java, HologramData.Builder())
            register(HologramTextTemplateData::class.java, HologramTextTemplateData.Immutable::class.java, HologramTextTemplateData.Builder())
            register(HologramUpdateIntervalData::class.java, HologramUpdateIntervalData.Immutable::class.java, HologramUpdateIntervalData.Builder())
        }
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
                .child(CommandSpec.builder()
                        .permission("holograms.show")
                        .executor(ShowHologramCommand(
                                getSelectedHologram = this@Holograms::getSelectedHologram,
                                getRawStringFromFile = { inputFile.toFile().readText() }))
                        .build(), "show")
                .build(), "holograms")

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .executor { src, args ->
                    (src as Player).spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.CRITICAL_HIT)
                            .quantity(20)
                            .offset(Vector3d(0.5, 0.2, 0.5))
                            .velocity(Vector3d(0.0, 0.2, 0.0))
                            .build(), src.location.position.add(5.0, 0.0, 0.0))
                    return@executor CommandResult.success()
                }.build(), "test")

        logger.info("$NAME loaded: $VERSION")
    }

    @Listener
    fun onReload(event: GameReloadEvent) {
        inputFile.safelyCreateFile()
    }

    @Listener
    fun onTeleport(event: MoveEntityEvent.Teleport) {
        // Deselect any Hologram on world teleport
        selectedHolograms.remove(event.targetEntity.uniqueId)
    }

    /**
     * Gets the currently selected [Hologram] by the [Player].
     * @throws [CommandException] and should therefore only be used in commands
     */
    private fun getSelectedHologram(player: Player): Hologram {
        val selectedHologramUuid = selectedHolograms[player.uniqueId] ?:
                throw CommandException(noHologramSelectedErrorText)

        val selectedHologram = getServiceOrFail(HologramsService::class)
                .getHologram(player.world, selectedHologramUuid).orNull()

        if (selectedHologram == null) {
            selectedHolograms.remove(player.uniqueId)
            throw CommandException(noHologramSelectedErrorText)
        }

        return selectedHologram
    }

    private fun Path.safelyCreateFile() {
        if (!Files.exists(this)) {
            Files.createDirectories(this.parent)
            Files.createFile(this)
        }
    }
}