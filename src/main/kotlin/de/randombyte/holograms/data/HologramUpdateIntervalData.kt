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
import java.time.Duration
import java.util.*

class HologramUpdateIntervalData(var interval: Duration = Duration.ZERO) : AbstractData<HologramUpdateIntervalData, HologramUpdateIntervalData.Immutable>() {

    val intervalValue: Value<Duration>
        get() = Sponge.getRegistry().valueFactory.createValue(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, interval)

    init {
        registerGettersAndSetters()
    }

    override fun registerGettersAndSetters() {
        registerFieldGetter(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, { interval })
        registerFieldSetter(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, { interval = it })
        registerKeyValue(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, { intervalValue })
    }

    override fun fill(dataHolder: DataHolder, overlap: MergeFunction): Optional<HologramUpdateIntervalData> {
        dataHolder.get(HologramUpdateIntervalData::class.java).ifPresent { that ->
            this.interval = overlap.merge(this, that).interval
        }
        return toOptional()
    }

    override fun from(container: DataContainer) = from(container as DataView)

    fun from(container: DataView): Optional<HologramUpdateIntervalData> {
        container.getObject(HologramKeys.HOLOGRAM_UPDATE_INTERVAL.query, Duration::class.java).ifPresent { interval = it }
        return toOptional()
    }

    override fun copy() = HologramUpdateIntervalData(interval)

    override fun asImmutable() = Immutable(interval)

    override fun getContentVersion() = 1

    override fun toContainer(): DataContainer = super.toContainer().set(HologramKeys.HOLOGRAM_UPDATE_INTERVAL.query, interval)

    class Immutable(val interval: Duration = Duration.ZERO) : AbstractImmutableData<Immutable, HologramUpdateIntervalData>() {

        val intervalValue: ImmutableValue<Duration>
            get() = Sponge.getRegistry().valueFactory.createValue(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, interval).asImmutable()

        init {
            registerGetters()
        }

        override fun registerGetters() {
            registerFieldGetter(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, { interval })
            registerKeyValue(HologramKeys.HOLOGRAM_UPDATE_INTERVAL, { intervalValue })
        }

        override fun asMutable() = HologramUpdateIntervalData(interval)

        override fun getContentVersion() = 1

        override fun toContainer(): DataContainer = super.toContainer().set(HologramKeys.HOLOGRAM_UPDATE_INTERVAL.query, interval)
    }

    class Builder internal constructor() : AbstractDataBuilder<HologramUpdateIntervalData>(HologramUpdateIntervalData::class.java, 1), DataManipulatorBuilder<HologramUpdateIntervalData, Immutable> {

        override fun create() = HologramUpdateIntervalData()

        override fun createFrom(dataHolder: DataHolder): Optional<HologramUpdateIntervalData> = create().fill(dataHolder)

        @Throws(InvalidDataException::class)
        override fun buildContent(container: DataView) = create().from(container)
    }
}
