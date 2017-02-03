package de.randombyte.holograms

import de.randombyte.holograms.api.HologramsService
import de.randombyte.holograms.api.HologramsService.Hologram
import de.randombyte.holograms.data.HologramData
import de.randombyte.holograms.data.HologramKeys
import de.randombyte.kosp.extensions.createEntity
import de.randombyte.kosp.extensions.getWorld
import de.randombyte.kosp.extensions.orNull
import de.randombyte.kosp.extensions.toOptional
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.entity.living.ArmorStand
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import org.spongepowered.api.world.extent.Extent
import java.util.*

class HologramsServiceImpl(val spawnCause: Cause) : HologramsService {

    class HologramImpl internal constructor(uuid: UUID, worldUuid: UUID) : Hologram(uuid, worldUuid) {

        init {
            if (!doesExist())
                throw IllegalArgumentException("Hologram can't be found, run Hologram#getArmorStand() to get more information.")
        }

        override var location: Location<World>
            get() = getArmorStand().location
            set(value) { getArmorStand().location = value }

        override var text: Text
            get() = getArmorStand().get(Keys.DISPLAY_NAME).orElse(Text.EMPTY)
            set(value) { getArmorStand().offer(Keys.DISPLAY_NAME, value) }

        override fun doesExist() = worldUuid.getWorld()?.getEntity(uuid)?.orNull()?.isHologram() ?: false

        override fun remove() = getArmorStand().remove()

        override fun getArmorStand(): ArmorStand {
            val world = worldUuid.getWorld() ?: throw RuntimeException("Can't find world '$worldUuid'!")
            val entity = world.getEntity(uuid).orNull() ?: throw RuntimeException("Can't find Entity '$uuid' in world '$worldUuid'!")
            val armorStand = (entity as? ArmorStand) ?: throw RuntimeException("Entity '$uuid' in world '$worldUuid' is not an ArmorStand!")
            if (!armorStand.isHologram()) throw RuntimeException("ArmorStand '$uuid' in world '$worldUuid' is not a Hologram!")
            return armorStand
        }
    }

    override fun createHologram(location: Location<out Extent>, text: Text): Optional<Hologram> {
        val armorStand = location.createEntity(EntityTypes.ARMOR_STAND)
        if (!location.extent.spawnEntity(armorStand, spawnCause)) return Optional.empty()

        armorStand.offer(Keys.DISPLAY_NAME, text)
        armorStand.offer(Keys.CUSTOM_NAME_VISIBLE, true)
        armorStand.offer(Keys.HAS_GRAVITY, false)
        armorStand.offer(Keys.ARMOR_STAND_MARKER, true)
        armorStand.offer(Keys.INVISIBLE, true)
        val data = armorStand.getOrCreate(HologramData::class.java).get().set(HologramKeys.IS_HOLOGRAM, true)
        armorStand.offer(data)

        return HologramImpl(armorStand.uniqueId,location.extent.uniqueId).toOptional()
    }

    override fun getHologram(extent: Extent, hologramUuid: UUID) = getHolograms(extent)
            .firstOrNull { it.uuid == hologramUuid }
            .toOptional()

    override fun getHolograms(center: Location<out Extent>, radius: Double) = getHolograms(center.extent)
            .associateBy(keySelector = { it }, valueTransform = { center.position.distance(it.location.position) })
            .filter { it.value <= radius }
            .toList()
            .sortedBy { it.second }

    override fun getHolograms(extent : Extent) = extent.entities
            .filter(Entity::isHologram)
            .map { HologramImpl(it.uniqueId, extent.uniqueId) }

}

private fun Entity.isHologram() = this is ArmorStand && get(HologramKeys.IS_HOLOGRAM).orElse(false)
