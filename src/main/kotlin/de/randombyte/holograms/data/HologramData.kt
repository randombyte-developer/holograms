package de.randombyte.holograms.data

import com.google.common.reflect.TypeToken
import de.randombyte.kosp.extensions.toOptional
import de.randombyte.kosp.extensions.typeToken
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.DataView
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.key.KeyFactory
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData
import org.spongepowered.api.data.merge.MergeFunction
import org.spongepowered.api.data.persistence.AbstractDataBuilder
import org.spongepowered.api.data.persistence.InvalidDataException
import org.spongepowered.api.data.value.immutable.ImmutableValue
import org.spongepowered.api.data.value.mutable.Value
import java.util.*

/**
 * Just to distinguish normal ArmorStands and those that are used as a Hologram.
 */
class HologramData internal constructor(var isHologram : Boolean = false): AbstractData<HologramData, HologramData.Immutable>() {

    init {
        registerGettersAndSetters()
    }

    override fun registerGettersAndSetters() {
        registerFieldGetter(HologramKeys.IS_HOLOGRAM, { isHologram })
        registerFieldSetter(HologramKeys.IS_HOLOGRAM, { isHologram = it })
        registerKeyValue(HologramKeys.IS_HOLOGRAM, { getIsHologramValue() })
    }

    fun getIsHologramValue(): Value<Boolean> = Sponge.getRegistry().valueFactory.createValue(HologramKeys.IS_HOLOGRAM, isHologram)

    override fun fill(dataHolder: DataHolder, overlap: MergeFunction): Optional<HologramData> {
        dataHolder.get(HologramData::class.java).ifPresent { that ->
            this.isHologram = overlap.merge(this, that).isHologram
        }
        return this.toOptional()
    }

    override fun from(container: DataContainer) = from(container as DataView)

    fun from(container: DataView): Optional<HologramData> {
        container.getBoolean(HologramKeys.IS_HOLOGRAM.query).ifPresent { isHologram = it }
        return this.toOptional()
    }

    override fun copy() = HologramData(isHologram)

    override fun asImmutable() = Immutable(isHologram)

    override fun getContentVersion() = 1

    override fun toContainer(): DataContainer = super.toContainer().set(HologramKeys.IS_HOLOGRAM.query, isHologram)

    class Immutable(val isHologram : Boolean = false) : AbstractImmutableData<Immutable, HologramData>() {

        init {
            registerGetters()
        }

        override fun registerGetters() {
            registerFieldGetter(HologramKeys.IS_HOLOGRAM, { isHologram })
            registerKeyValue(HologramKeys.IS_HOLOGRAM, { getIsHologramValue() })
        }

        fun getIsHologramValue(): ImmutableValue<Boolean> = Sponge.getRegistry().valueFactory.createValue(HologramKeys.IS_HOLOGRAM, isHologram).asImmutable()

        override fun asMutable() = HologramData(isHologram)

        override fun getContentVersion() = 1

        override fun toContainer(): DataContainer = super.toContainer().set(HologramKeys.IS_HOLOGRAM.query, isHologram)

    }

    class Builder internal constructor() : AbstractDataBuilder<HologramData>(HologramData::class.java, 1), DataManipulatorBuilder<HologramData, Immutable> {

        override fun create() = HologramData()

        override fun createFrom(dataHolder: DataHolder): Optional<HologramData> = create().fill(dataHolder)

        @Throws(InvalidDataException::class)
        override fun buildContent(container: DataView) = create().from(container)

    }
}

object HologramKeys {
    val IS_HOLOGRAM: Key<Value<Boolean>> = KeyFactory.makeSingleKey(
            Boolean::class.typeToken,
            object : TypeToken<Value<Boolean>>() {},
            DataQuery.of(".", "is.hologram"), "holograms:is_hologram", "Is Hologram")
}
