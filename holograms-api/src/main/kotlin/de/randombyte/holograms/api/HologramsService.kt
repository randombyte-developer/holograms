package de.randombyte.holograms.api

import de.randombyte.kosp.extensions.orNull
import de.randombyte.kosp.extensions.toOptional
import org.spongepowered.api.entity.living.ArmorStand
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.TextTemplate
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import org.spongepowered.api.world.extent.Extent
import java.time.Duration
import java.util.*

// The words 'argument' and 'parameter' of TextTemplates in the SpongeApi are switched
// https://github.com/SpongePowered/SpongeAPI/issues/1452
// I'll keep this error to avoid even more confusion
interface HologramsService {
    abstract class Hologram(val uuid: UUID, val worldUuid: UUID) {

        /**
         * Gets or set the [Location] of the Hologram.
         */
        abstract var location: Location<World>

        /**
         * Getter: The real display Text of the [ArmorStand].
         *      The parameters from [textTemplate] might be old because it was some time ago
         *      the [textTemplate] was reevaluated.
         * Setter: Internally the Text is converted to a [TextTemplate] and then set to [textTemplate].
         */
        abstract var text: Text

        /**
         * Getter: Gets the directly set [TextTemplate] or the one that was converted from [text]
         *      to TextTemplate without [TextTemplate.Arg]s.
         * Setter: Set the [TextTemplate] and reevaluates it(using PlaceholderApi) which sets the [text].
         */
        abstract var textTemplate: TextTemplate

        /**
         * Interval in which the [text] should be updated by reevaluating the [textTemplate] with
         * the parameters provided by PlaceholderApi.
         */
        abstract var updateInterval: Duration

        /**
         * Reevaluates the [textTemplate] (using PlaceholderApi) and sets it as the text.
         * This method is automatically called when setting [textTemplate].
         *
         * @throws RuntimeException if a [textTemplate] with arguments was set and
         *      PlaceholderApi isn't loaded; this can't happen if only a [text] was set
         */
        abstract fun updateText()

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
     * Creates an [Hologram] at the [location].
     *
     * @return the spawned [Hologram], if absent it couldn't be spawned
     */
    fun createHologram(location: Location<out Extent>): Optional<Hologram>

    /**
     * This is just a convenience method.
     * Creates an [Hologram] at the [location] with the [text].
     *
     * @return the spawned [Hologram], if absent it couldn't be spawned
     */
    fun createHologram(location: Location<out Extent>, text: Text): Optional<Hologram> {
        val hologram = createHologram(location).orNull() ?: return Optional.empty()
        hologram.text = text
        return hologram.toOptional()
    }

    /**
     * This is just a convenience method.
     * Spawns multiple Holograms on top of each other. The most bottom one is at [lowestLocation].
     * The following ones stack on top of each other which creates the effect of a multiline Hologram.
     * The first [Text] in [texts] is the most top one.
     * The texts are vertically [verticalSpace] blocks apart(recommended value: 0.3).
     *
     * @return the spawned [Hologram]s, if absent they couldn't be spawned
     */
    fun createMultilineHologram(lowestLocation: Location<out Extent>, texts: List<Text>, verticalSpace: Double): Optional<List<Hologram>> {
        val holograms = texts.asReversed().mapIndexed { i, text -> // from bottom to top
            val pos = lowestLocation.position.add(0.0, i * verticalSpace, 0.0)
            val hologram = createHologram(lowestLocation.setPosition(pos), text).orNull() ?: return Optional.empty()
            return@mapIndexed hologram
        }
        return holograms.toOptional()
    }

    /**
     * Gets all [Hologram]s in the given [extent].
     *
     * @return all found [Hologram]s
     */
    fun getHolograms(extent: Extent): List<Hologram>

    /**
     * This is just a convenience method.
     * Tries to find the [Hologram] with [hologramUuid] in the given [extent].
     *
     * @return the found [Hologram], if absent it couldn't be found
     */
    fun getHologram(extent: Extent, hologramUuid: UUID): Optional<out Hologram> = getHolograms(extent)
            .firstOrNull { it.uuid == hologramUuid }
            .toOptional()

    /**
     * This is just a convenience method.
     * Returns a list of the [Hologram]s in the max [radius] around the [center].
     *
     * @return the list of [Hologram]s and the distance to the [center], sorted by the distance in ascending order
     */
    fun getHolograms(center: Location<out Extent>, radius: Double): List<Pair<Hologram, Double>> = getHolograms(center.extent)
            .map { hologram -> hologram to center.position.distance(hologram.location.position) }
            .filter { (_, distance) -> distance <= radius }
            .sortedBy { (_, distance) -> distance }
}