package de.randombyte.holograms

import de.randombyte.holograms.api.HologramsService
import de.randombyte.holograms.api.HologramsService.Hologram
import de.randombyte.holograms.data.HologramData
import de.randombyte.holograms.data.HologramKeys
import de.randombyte.holograms.data.HologramTextTemplateData
import de.randombyte.holograms.data.HologramUpdateIntervalData
import de.randombyte.kosp.extensions.getWorld
import de.randombyte.kosp.extensions.orNull
import de.randombyte.kosp.extensions.toOptional
import de.randombyte.kosp.fixedTextTemplateOf
import de.randombyte.kosp.getServiceOrFail
import me.rojo8399.placeholderapi.PlaceholderService
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.entity.living.ArmorStand
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.TextTemplate
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import org.spongepowered.api.world.extent.Extent
import java.time.Duration
import java.util.*

class HologramsServiceImpl(val spawnCause: Cause) : HologramsService {

    class HologramImpl internal constructor(uuid: UUID, worldUuid: UUID) : Hologram(uuid, worldUuid) {

        init {
            // Check for existence by trying to get the ArmorStand
            // If it can't be found a detailed exception is thrown
            getArmorStand()
        }

        override var location: Location<World>
            get() = getArmorStand().location
            set(value) { getArmorStand().location = value }

        override var text: Text
            get() = getArmorStand().get(Keys.DISPLAY_NAME).orElse(Text.EMPTY)
            set(value) { textTemplate = fixedTextTemplateOf(value) }

        override var textTemplate: TextTemplate
            get() = getArmorStand().get(HologramKeys.HOLOGRAM_TEXT_TEMPLATE).orElse(TextTemplate.EMPTY)
            set(value) {
                if (!getArmorStand().offer(HologramTextTemplateData(value)).isSuccessful)
                    throw RuntimeException("Couldn't offer TextTemplate to ArmorStand!")
                updateText()
            }

        override var updateInterval: Duration
            get() = getArmorStand().get(HologramKeys.HOLOGRAM_UPDATE_INTERVAL).orElse(Duration.ZERO)
            set(value) {
                if (!getArmorStand().offer(HologramUpdateIntervalData(value)).isSuccessful)
                    throw RuntimeException("Couldn't offer update interval to ArmorStand!")
            }

        override fun updateText() {
            setRawText(evaluateTextTemplate())
        }

        override fun exists() = worldUuid.getWorld()?.getEntity(uuid)?.orNull()?.isHologram() ?: false

        override fun remove() = getArmorStand().remove()

        override fun getArmorStand(): ArmorStand {
            val world = worldUuid.getWorld() ?: throw RuntimeException("Can't find world '$worldUuid'!")
            val entity = world.getEntity(uuid).orNull() ?: throw RuntimeException("Can't find Entity '$uuid' in world '$worldUuid'!")
            val armorStand = (entity as? ArmorStand) ?: throw RuntimeException("Entity '$uuid' in world '$worldUuid' is not an ArmorStand!")
            if (!armorStand.isHologram()) throw RuntimeException("ArmorStand '$uuid' in world '$worldUuid' is not a Hologram!")
            return armorStand
        }

        private fun evaluateTextTemplate(): Text {
            // toText() is the same as apply() when no arguments are present
            // toText() just returns the already constructed Text object
            if (textTemplate.arguments.isEmpty()) return textTemplate.toText()
            val placeholderService = getServiceOrFail(PlaceholderService::class,
                    failMessage = "PlaceholderAPI is not available! Please install it or don't use placeholders in your texts.")
            return placeholderService.replacePlaceholders(textTemplate, null)
        }

        private fun setRawText(text: Text) {
            if (!getArmorStand().offer(Keys.DISPLAY_NAME, text).isSuccessful)
                throw RuntimeException("Couldn't offer display text to ArmorStand!")
        }
    }

    override fun createHologram(location: Location<out Extent>): Optional<Hologram> {
        val armorStand = location.createEntity(EntityTypes.ARMOR_STAND)
        if (!location.extent.spawnEntity(armorStand, spawnCause)) return Optional.empty()

        armorStand.offer(Keys.CUSTOM_NAME_VISIBLE, true)
        armorStand.offer(Keys.HAS_GRAVITY, false)
        armorStand.offer(Keys.ARMOR_STAND_MARKER, true)
        armorStand.offer(Keys.INVISIBLE, true)

        armorStand.offer(HologramData(isHologram = true))
        armorStand.offer(HologramUpdateIntervalData())

        return HologramImpl(armorStand.uniqueId, location.extent.uniqueId).toOptional()
    }

    override fun getHolograms(extent: Extent) = extent.entities
            .filter(Entity::isHologram)
            .map { HologramImpl(it.uniqueId, extent.uniqueId) }
}

private fun Entity.isHologram() = this is ArmorStand && get(HologramKeys.IS_HOLOGRAM).orElse(false)