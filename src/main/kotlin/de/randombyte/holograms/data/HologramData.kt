package de.randombyte.holograms.data

import de.randombyte.kosp.extensions.toOptional
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.DataView
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData
import org.spongepowered.api.data.merge.MergeFunction
import org.spongepowered.api.data.persistence.AbstractDataBuilder
import org.spongepowered.api.data.persistence.InvalidDataException
import org.spongepowered.api.data.value.mutable.Value
import org.spongepowered.api.util.TypeTokens
import java.util.*

/**
 * Just to distinguish normal ArmorStands and those that are used as a Hologram.
 */
class HologramData internal constructor(var isHologram: Boolean = false) : AbstractData<HologramData, HologramData.Immutable>() {

    init {
        registerGettersAndSetters()
    }

    override fun registerGettersAndSetters() {
        registerFieldGetter(HologramKeys.IS_HOLOGRAM, { isHologram })
        registerFieldSetter(HologramKeys.IS_HOLOGRAM, { isHologram = it })
        registerKeyValue(HologramKeys.IS_HOLOGRAM, {
            Sponge.getRegistry().valueFactory.createValue(HologramKeys.IS_HOLOGRAM, isHologram)
        })
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

    override fun getContentVersion() = 1

    override fun toContainer(): DataContainer = super.toContainer().set(HologramKeys.IS_HOLOGRAM.query, isHologram)

    class Immutable(val isHologram: Boolean = false) : AbstractImmutableData<Immutable, HologramData>() {

        init {
            registerGetters()
        }

        override fun registerGetters() {
            registerFieldGetter(HologramKeys.IS_HOLOGRAM, { isHologram })
            registerKeyValue(HologramKeys.IS_HOLOGRAM, {
                Sponge.getRegistry().valueFactory.createValue(HologramKeys.IS_HOLOGRAM, isHologram).asImmutable()
            })
        }

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
    lateinit var IS_HOLOGRAM: Key<Value<Boolean>>

    fun buildKeys() {
        IS_HOLOGRAM = Key.builder()
                .type(TypeTokens.BOOLEAN_VALUE_TOKEN)
                .id("holograms:is-hologram")
                .name("Is Hologram")
                .query(DataQuery.of("IsHologram"))
                .build()
    }
}