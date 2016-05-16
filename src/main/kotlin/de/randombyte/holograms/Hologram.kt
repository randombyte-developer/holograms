package de.randombyte.holograms

import de.randombyte.holograms.OptionalExtension.Companion.presence
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
     * Spawns an ArmorStand if none was given in the constructor.
     * @return true if successful, false if not or already spawned
     * @throws IllegalStateException when there is an uuid without an corresponding ArmorStand
     */
    fun spawn(): Boolean {
        return if (armorStandUUID == null) {
            //Create new
            location.extent.createEntity(EntityTypes.ARMOR_STAND, location.position).presence { armorStand ->
                armorStandUUID = armorStand.uniqueId
                prepare(armorStand, text)
                return@presence location.extent.spawnEntity(armorStand, Holograms.PLUGIN_CAUSE)
            }.absence {
                return@absence false //Couldn't be created
            }
        } else {
            val armorStand = getArmorStand(armorStandUUID as UUID) //casting manually because smart cast can't handle this situation
            if (armorStand != null) {
                prepare(armorStand, text) //Refresh invisibility
            } else {
                throw IllegalStateException("UUID $armorStandUUID present without corresponding ArmorStand in world" +
                        "of Hologram location! Please remove this Hologram manually from the config.")
            }
            return false
        }
    }

    /**
     * Removes the ArmorStand from the world.
     */
    fun remove() {
        if (armorStandUUID != null) {
            location.extent.getEntity(armorStandUUID).ifPresent { it.remove() }
        }
    }

    /**
     * @return The found Entity or null
     */
    fun getArmorStand(uuid: UUID) = location.extent.getEntity(uuid).presence { it }.absence { null }

    companion object {
        fun prepare(entity: Entity, text: Text) {
            setInvisible(entity)
            entity.offer(Keys.INVISIBLE, true)
            entity.offer(Keys.DISPLAY_NAME, text)
            entity.offer(Keys.CUSTOM_NAME_VISIBLE, true)
        }

        /**
         * Gives the effect Invisibility without particles 68 years([Integer.MAX_VALUE]) to the [entity].
         */
        fun setInvisible(entity: Entity) {
            entity.getOrCreate(PotionEffectData::class.java).ifPresent { potionsData ->
                entity.offer(potionsData.addElement(PotionEffect.builder()
                        .potionType(PotionEffectTypes.INVISIBILITY)
                        .particles(false)
                        .duration(Integer.MAX_VALUE)
                        .build()))
            }
        }
    }
}