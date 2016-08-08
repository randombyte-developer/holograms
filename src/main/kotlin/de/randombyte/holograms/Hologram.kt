package de.randombyte.holograms

import de.randombyte.holograms.Hologram.Companion.spawn
import de.randombyte.holograms.config.ConfigManager
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

/**
 * Floating text[lines]. Spawned with [spawn].
 */
class Hologram(val uuid: UUID, val lines: List<HologramTextLine>) {
    companion object {
        const val MULTI_LINE_SPACE = 0.3

        fun spawn(texts: List<Text>, location: Location<World>): Optional<Hologram> {
            val topLocation = getHologramTopLocation(location, texts.size)
            return Optional.of(Hologram(UUID.randomUUID(), texts.mapIndexed { i, text ->
                val armorStand = location.extent.createEntity(EntityTypes.ARMOR_STAND, topLocation.position.sub(0.0, i * MULTI_LINE_SPACE, 0.0))
                if (!location.extent.spawnEntity(armorStand, Holograms.PLUGIN_SPAWN_CAUSE)) return Optional.empty()
                prepare(armorStand, text)
                return@mapIndexed HologramTextLine(armorStand.uniqueId, text)
            }))
        }

        fun delete(world: World, uuid: UUID) {
            ConfigManager.getHolograms(world).filter { it.uuid.equals(uuid) }.forEach { hologram ->
                hologram.lines.forEach { line ->
                    world.getEntity(line.armorStandUUID).ifPresent { it.remove() }
                }
            }
        }

        private fun prepare(entity: Entity, text: Text) {
            entity.offer(Keys.DISPLAY_NAME, text)
            entity.offer(Keys.CUSTOM_NAME_VISIBLE, true)

            //armorStand.offer(Keys.ARMOR_STAND_HAS_GRAVITY, false)
            setNoGravity(entity) //waiting for bleeding merged into master

            //armorStand.offer(Keys.ARMOR_STAND_MARKER, true)
            setMarker(entity)//waiting for bleeding merged into master

            setInvisible(entity) //https://github.com/SpongePowered/SpongeAPI/issues/1151
        }

        fun getHologramTopLocation(baseLocation: Location<World>, numberOfLines: Int) = baseLocation.add(0.0, numberOfLines * MULTI_LINE_SPACE, 0.0)

        private fun setMarker(entity: Entity) = setBooleanEntitydata(entity, "Marker", true)
        private fun setNoGravity(entity: Entity) = setBooleanEntitydata(entity, "NoGravity", true)

        /**
         * [Keys.INVISIBLE] makes the entity completely gone, even in spectator mode, so I have to use this method to
         * hide the armor stand body. Must be called after spawning the entity.
         * https://github.com/SpongePowered/SpongeAPI/issues/1151
         */
        private fun setInvisible(entity: Entity) = setBooleanEntitydata(entity, "Invisible", true)

        private fun setBooleanEntitydata(entity: Entity, dataTag: String, active: Boolean) = setEntitydata(entity, "{$dataTag:${if (active) 1 else 0}b}")
        private fun setEntitydata(entity: Entity, data: String) = executeCommand("entitydata ${entity.uniqueId} $data")

        /**
         * Executes a [command] as the server console. [pingBefore] defaults to true, which means that a command is
         * executed before the actual one(In this case "msg").
         * The bug report is [here](https://github.com/SpongePowered/SpongeCommon/issues/665).
         */
        private fun executeCommand(command: String, pingBefore: Boolean = true): CommandResult {
            if (pingBefore) executeCommand("msg", false) //https://github.com/SpongePowered/SpongeCommon/issues/665
            return Sponge.getCommandManager().process(Sponge.getServer().console, command)
        }
    }
}