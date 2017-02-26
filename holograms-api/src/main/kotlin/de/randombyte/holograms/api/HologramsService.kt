package de.randombyte.holograms.api

import org.spongepowered.api.entity.living.ArmorStand
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import org.spongepowered.api.world.extent.Extent
import java.util.*

interface HologramsService {
    abstract class Hologram(val uuid: UUID, val worldUuid: UUID) {
        abstract var location: Location<World>
        abstract var text: Text

        /**
         * Checks if this Hologram still exists. It may be removed by [remove] or killed otherwise.
         */
        abstract fun exists(): Boolean

        /**
         * Removes the Hologram.
         */
        abstract fun remove()

        /**
         * Gets the actual underlying [ArmorStand] entity. This can be used for further
         * modifications of the Hologram but is normally not needed.
         */
        abstract fun getArmorStand(): ArmorStand
    }

    /**
     * Creates an [Hologram] at the [location] with the [text].
     *
     * @return the spawned [Hologram], if absent it couldn't be spawned
     */
    fun createHologram(location: Location<out Extent>, text: Text): Optional<Hologram>

    /**
     * Spawns multiple Holograms on top of each other. The most bottom one is at [lowestLocation].
     * The following ones stack on top of each other which creates the effect of a multiline Hologram.
     * The first [Text] in [texts] is the most top one.
     * The texts are spaced by [verticalSpace] blocks. The default value is 0.3.
     *
     * @return the spawned [Hologram]s, if absent they couldn't be spawned
     */
    fun createMultilineHologram(lowestLocation: Location<out Extent>, texts: List<Text>, verticalSpace: Double): Optional<List<Hologram>> {
        val holograms = texts.asReversed().mapIndexed { i, text -> // from bottom to top
            val pos = lowestLocation.position.add(0.0, i * verticalSpace, 0.0)
            val hologram = createHologram(lowestLocation.setPosition(pos), text).orElse(null) ?: return Optional.empty()
            return@mapIndexed hologram
        }
        return Optional.of(holograms)
    }

    /**
     * Tries to find the [Hologram] with [hologramUuid] in the given [extent].
     *
     * @return the found [Hologram]s, if absent it couldn't be found
     */
    fun getHologram(extent: Extent, hologramUuid: UUID): Optional<out Hologram>

    /**
     * Returns a list of the [Hologram]s in the max [radius] around the [center].
     *
     * @return the list of [Hologram]s and the distance to the [center], sorted by the distance in ascending order
     */
    fun getHolograms(center: Location<out Extent>, radius: Double): List<Pair<Hologram, Double>>

    /**
     * Gets all [Hologram]s in the given [extent].
     *
     * @return all found [Hologram]s
     */
    fun getHolograms(extent: Extent): List<Hologram>
}