package de.randombyte.holograms

import de.randombyte.holograms.OptionalExtension.Companion.presence
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.entity.living.ArmorStand
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

/**
 * An invisible [ArmorStand] at [location] with a [text] over its head. Optionally an [armorStandUUID] if an ArmorStand
 * already exists.
 */
class Hologram(val text: Text, var armorStandUUID: UUID? = null) {

    /**
     * Spawns an ArmorStand at [location] if no uuid was given in the constructor.
     * @return true if spawned, false if not
     * @throws IllegalStateException when there is an uuid without an corresponding ArmorStand
     */
    fun spawn(location: Location<World>): Boolean {
        val armorStand = if (armorStandUUID == null) {
            //Create new ArmorStand
            location.extent.createEntity(EntityTypes.ARMOR_STAND, location.position)
                    .presence { it }.absence { null } as ArmorStand
        } else return false //Already spawned

        return if (location.extent.spawnEntity(armorStand, Holograms.PLUGIN_SPAWN_CAUSE)) {
            //Spawn created ArmorStand
            armorStandUUID = armorStand.uniqueId
            prepare(armorStand, text)
            true
        } else false //Couldn't be spawned
    }

    companion object {
        fun prepare(armorStand: ArmorStand, text: Text) {
            armorStand.offer(Keys.DISPLAY_NAME, text)
            armorStand.offer(Keys.CUSTOM_NAME_VISIBLE, true)
            armorStand.offer(Keys.ARMOR_STAND_MARKER, true)
            armorStand.offer(Keys.ARMOR_STAND_HAS_GRAVITY, false)
            setInvisible(armorStand)
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