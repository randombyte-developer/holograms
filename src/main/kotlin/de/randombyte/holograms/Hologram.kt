package de.randombyte.holograms

import de.randombyte.holograms.OptionalExtension.Companion.presence
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData
import org.spongepowered.api.effect.potion.PotionEffect
import org.spongepowered.api.effect.potion.PotionEffectTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

/**
 * An invisible [Armorstand] at [location] with a [text] over its head.
 */
class Hologram(val location: Location<World>, val text: Text, var armorStandUUID: UUID? = null) {

    /**
     * Spawns an ArmorStand if no uuid was given in the constructor.
     * @return true if successful, false if not or already spawned
     * @throws IllegalStateException when there is an uuid without an corresponding ArmorStand
     */
    fun spawn(): Boolean {
        val armorStand = if (armorStandUUID == null) {
            //Create new
            location.extent.createEntity(EntityTypes.ARMOR_STAND, location.position).presence { it }.absence { null }
        } else {
            //Search existing
            location.extent.getEntity(armorStandUUID).orElseThrow {
                throw IllegalStateException("UUID $armorStandUUID present without corresponding ArmorStand in world " +
                        "of Hologram location! Please remove this Hologram manually from the config.")
            }
        }
        armorStandUUID = armorStand.uniqueId
        return if (location.extent.spawnEntity(armorStand, Holograms.PLUGIN_SPAWN_CAUSE)) {
            prepare(armorStand, text)
            true
        } else false //Couldn't be spawned
    }

    /**
     * Removes the ArmorStand from the world.
     */
    fun remove() {
        if (armorStandUUID != null) {
            location.extent.getEntity(armorStandUUID).ifPresent { it.remove() }
        }
    }

    companion object {
        fun prepare(entity: Entity, text: Text) {
            entity.offer(Keys.DISPLAY_NAME, text)
            entity.offer(Keys.CUSTOM_NAME_VISIBLE, true)
            setInvisible(entity)
        }

        /**
         * [Keys.INVISIBLE] makes the entity completely gone, even in spectator mode, so I have to use this method to
         * hide the armor stand body. Must be called after spawning the entity.
         */
        fun setInvisible(entity: Entity) {
            Sponge.getCommandManager().process(Sponge.getServer().console, "msg") //https://github.com/SpongePowered/SpongeCommon/issues/665
            Sponge.getCommandManager().process(Sponge.getServer().console, "entitydata ${entity.uniqueId} {Invisible:1b}")
        }
    }
}