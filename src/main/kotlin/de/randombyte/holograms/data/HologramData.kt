package de.randombyte.holograms.data

import de.randombyte.kosp.extensions.toOptional
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.data.DataView
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData
import org.spongepowered.api.data.merge.MergeFunction
import org.spongepowered.api.data.persistence.AbstractDataBuilder
import org.spongepowered.api.data.persistence.InvalidDataException
import org.spongepowered.api.data.value.immutable.ImmutableValue
import org.spongepowered.api.data.value.mutable.Value
import org.spongepowered.api.text.TextTemplate
import java.time.Duration
import java.util.*

/**
 * Just to distinguish normal ArmorStands and those that are used as a Hologram.
 */
class HologramData internal constructor(
        var isHologram: Boolean = false,
        var textTemplate: TextTemplate = TextTemplate.EMPTY,
        var updateInterval: Duration = Duration.ZERO
) : AbstractData<HologramData, HologramData.Immutable>() {

    val isHologramValue: Value<Boolean>
        get() = Sponge.getRegistry().valueFactory.createValue(HologramKeys.IS_HOLOGRAM, isHologram)

    val textTemplateValue: Value<TextTemplate>
        get() = Sponge.getRegistry().valueFactory.createValue(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, textTemplate)

    val updateIntervalValue: Value<Duration>
        get() = Sponge.getRegistry().valueFactory.createValue(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, updateInterval)

    init {
        registerGettersAndSetters()
    }

    override fun registerGettersAndSetters() {
        registerFieldGetter(HologramKeys.IS_HOLOGRAM, { isHologram })
        registerFieldSetter(HologramKeys.IS_HOLOGRAM, { isHologram = it })
        registerKeyValue(HologramKeys.IS_HOLOGRAM, { isHologramValue })

        registerFieldGetter(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, { textTemplate })
        registerFieldSetter(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, { textTemplate = it })
        registerKeyValue(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, { textTemplateValue })

        registerFieldGetter(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, { updateInterval })
        registerFieldSetter(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, { updateInterval = it })
        registerKeyValue(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, { updateIntervalValue })
    }

    override fun fill(dataHolder: DataHolder, overlap: MergeFunction): Optional<HologramData> {
        dataHolder.get(HologramData::class.java).ifPresent { that ->
            isHologram = overlap.merge(this, that).isHologram
        }
        return toOptional()
    }

    override fun from(container: DataContainer) = from(container as DataView)

    private fun from(container: DataView): Optional<HologramData> {
        container.getBoolean(HologramKeys.IS_HOLOGRAM.query).ifPresent { isHologram = it }
        return toOptional()
    }

    override fun copy() = HologramData(isHologram)

    override fun asImmutable() = Immutable(isHologram)

    override fun getContentVersion() = 2

    override fun toContainer(): DataContainer = super.toContainer().set(HologramKeys.IS_HOLOGRAM.query, isHologram)

    class Immutable internal constructor(
            val isHologram: Boolean = false,
            val textTemplate: TextTemplate = TextTemplate.EMPTY,
            val updateInterval: Duration = Duration.ZERO
    ) : AbstractImmutableData<Immutable, HologramData>() {

        val isHologramValue: ImmutableValue<Boolean>
            get() = Sponge.getRegistry().valueFactory.createValue(HologramKeys.IS_HOLOGRAM, isHologram).asImmutable()

        val textTemplateValue: ImmutableValue<TextTemplate>
            get() = Sponge.getRegistry().valueFactory.createValue(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, textTemplate).asImmutable()

        val updateIntervalValue: ImmutableValue<Duration>
            get() = Sponge.getRegistry().valueFactory.createValue(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, updateInterval).asImmutable()

        init {
            registerGetters()
        }

        override fun registerGetters() {
            registerFieldGetter(HologramKeys.IS_HOLOGRAM, { isHologram })
            registerKeyValue(HologramKeys.IS_HOLOGRAM, { isHologramValue })

            registerFieldGetter(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, { textTemplate })
            registerKeyValue(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, { textTemplateValue })

            registerFieldGetter(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, { updateInterval })
            registerKeyValue(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, { updateIntervalValue })
        }

        override fun asMutable() = HologramData(isHologram, textTemplate, updateInterval)

        override fun getContentVersion() = 2

        override fun toContainer(): DataContainer = super.toContainer()
                .set(HologramKeys.IS_HOLOGRAM.query, isHologram)
                .set(HologramKeys.HOLOGRAM_TEXT_TEMPLATE.query, textTemplate)
                .set(HologramKeys.HOLOGRAM_UPDATE_INTERVAL.query, updateInterval)
    }

    class Builder internal constructor() : AbstractDataBuilder<HologramData>(HologramData::class.java, 1), DataManipulatorBuilder<HologramData, Immutable> {

        override fun create() = HologramData()

        override fun createFrom(dataHolder: DataHolder): Optional<HologramData> = create().fill(dataHolder)

        @Throws(InvalidDataException::class)
        override fun buildContent(container: DataView) = create().from(container)
    }
}