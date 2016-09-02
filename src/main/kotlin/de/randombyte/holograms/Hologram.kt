package de.randombyte.holograms

import de.randombyte.holograms.Hologram.Companion.spawn
import de.randombyte.holograms.config.ConfigManager
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
                val optArmorStand = location.extent.createEntity(EntityTypes.ARMOR_STAND, topLocation.position.sub(0.0, i * MULTI_LINE_SPACE, 0.0))
                if (!optArmorStand.isPresent) return Optional.empty()
                val armorStand = optArmorStand.get()
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

        private fun prepare(armorStand: Entity, text: Text) {
            armorStand.offer(Keys.DISPLAY_NAME, text)
            armorStand.offer(Keys.CUSTOM_NAME_VISIBLE, true)
            armorStand.offer(Keys.ARMOR_STAND_HAS_GRAVITY, false)
            armorStand.offer(Keys.ARMOR_STAND_MARKER, true)
            armorStand.offer(Keys.INVISIBLE, true)
        }

        fun getHologramTopLocation(baseLocation: Location<World>, numberOfLines: Int): Location<World> =
                baseLocation.add(0.0, numberOfLines * MULTI_LINE_SPACE, 0.0)
    }
}