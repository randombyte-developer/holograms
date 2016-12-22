package de.randombyte.holograms

import de.randombyte.holograms.Hologram.Companion.spawn
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
class Hologram(val armorStandUUID: UUID, val text: Text) {
    companion object {
        const val MULTI_LINE_SPACE = 0.3

        /**
         * Spawns one Hologram at [location] with [text].
         *
         * @return the [Hologram], or null if it couldn't be spawned
         */
        fun spawn(text: Text, location: Location<World>): Hologram? {
            val armorStand = location.extent.createEntity(EntityTypes.ARMOR_STAND, location.position)
            if (!location.extent.spawnEntity(armorStand, Holograms.PLUGIN_SPAWN_CAUSE)) return null
            prepare(armorStand, text)
            return Hologram(armorStand.uniqueId, text)
        }

        /**
         * Spawns multiple Holograms on top of each other. The most bottom one is at [bottomLocation].
         * The following ones stack on top which creates the effect of a multiline Hologram.
         *
         * @return the spawned [Hologram]s, or null if they couldn't be spawned
         */
        fun spawn(texts: List<Text>, bottomLocation: Location<World>): List<Hologram>? {
            return texts.asReversed().mapIndexed { i, text -> // from bottom to top
                val pos = bottomLocation.position.add(0.0, i * MULTI_LINE_SPACE, 0.0)
                val hologram = spawn(text, bottomLocation.setPosition(pos)) ?: return null
                return@mapIndexed hologram
            }
        }

        fun fromArmorStand(armorStand: Entity): Hologram? = if (armorStand.type == EntityTypes.ARMOR_STAND) {
            Hologram(armorStand.uniqueId, armorStand.get(Keys.DISPLAY_NAME).orElse(Text.EMPTY))
        } else null

        private fun prepare(armorStand: Entity, text: Text) {
            armorStand.offer(Keys.DISPLAY_NAME, text)
            armorStand.offer(Keys.CUSTOM_NAME_VISIBLE, true)
            armorStand.offer(Keys.HAS_GRAVITY, false)
            armorStand.offer(Keys.ARMOR_STAND_MARKER, true)
            armorStand.offer(Keys.INVISIBLE, true)
        }
    }
}