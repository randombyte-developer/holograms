package de.randombyte.holograms.api

import org.spongepowered.api.entity.living.ArmorStand
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import org.spongepowered.api.world.extent.Extent
import java.util.*

interface HologramsService {
    abstract class Hologram(val uuid: UUID, val worldUuid: UUID) {
        abstract fun doesExist(): Boolean

        abstract var location: Location<World>

        abstract var text: Text

        abstract fun remove()

        abstract fun getArmorStand(): ArmorStand
    }

    fun createHologram(location: Location<out Extent>, text: Text): Optional<Hologram>

    /**
     * Spawns multiple Holograms on top of each other. The most bottom one is at [lowestLocation].
     * The following ones stack on top which creates the effect of a multiline Hologram.
     *
     * @return the spawned [Hologram]s, or [Optional.EMPTY] if they couldn't be spawned
     */
    fun createMultilineHologram(lowestLocation: Location<out Extent>, texts: List<Text>, verticalSpace: Double = 0.3): Optional<List<Hologram>> {
        val holograms = texts.asReversed().mapIndexed { i, text -> // from bottom to top
            val pos = lowestLocation.position.add(0.0, i * verticalSpace, 0.0)
            val hologram = createHologram(lowestLocation.setPosition(pos), text).orElse(null) ?: return Optional.empty()
            return@mapIndexed hologram
        }
        return Optional.of(holograms)
    }

    fun getHologram(extent: Extent, hologramUUID: UUID): Optional<out Hologram>

    /**
     * Returns a list of the [Hologram]s in the max [radius] around the [center].
     *
     * @return the list of [Hologram]s and the distance to the [center], sorted by the distance in ascending order
     */
    fun getHolograms(center: Location<out Extent>, radius: Double): List<Pair<Hologram, Double>>
    fun getHolograms(extent: Extent): List<Hologram>
}