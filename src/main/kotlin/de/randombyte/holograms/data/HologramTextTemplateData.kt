package de.randombyte.holograms.data

import de.randombyte.kosp.extensions.toOptional
import de.randombyte.kosp.extensions.typeToken
import ninja.leaping.configurate.SimpleConfigurationNode
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.DataContainer
import org.spongepowered.api.data.DataHolder
import org.spongepowered.api.data.DataView
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData
import org.spongepowered.api.data.merge.MergeFunction
import org.spongepowered.api.data.persistence.AbstractDataBuilder
import org.spongepowered.api.data.persistence.DataTranslators
import org.spongepowered.api.data.persistence.InvalidDataException
import org.spongepowered.api.data.value.immutable.ImmutableValue
import org.spongepowered.api.data.value.mutable.Value
import org.spongepowered.api.text.TextTemplate
import java.util.*

/**
 * The [TextTemplate] of a Hologram. It is serialized with hacky stuff: The TextTemplate is serialized
 * by TextTemplateConfigSerializer to a ConfigurationNode. This config node is translated by
 * DataTranslator.CONFIGURATION_NODE to a DataContainer which eventually gets saved the DataHolder.
 */
class HologramTextTemplateData internal constructor(var textTemplate: TextTemplate = TextTemplate.EMPTY) : AbstractData<HologramTextTemplateData, HologramTextTemplateData.Immutable>() {

    val textTemplateValue: Value<TextTemplate>
        get() = Sponge.getRegistry().valueFactory.createValue(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, textTemplate)

    init {
        registerGettersAndSetters()
    }

    override fun registerGettersAndSetters() {
        registerFieldGetter(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, { textTemplate })
        registerFieldSetter(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, { textTemplate = it })
        registerKeyValue(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, { textTemplateValue })
    }

    override fun fill(dataHolder: DataHolder, overlap: MergeFunction): Optional<HologramTextTemplateData> {
        dataHolder.get(HologramTextTemplateData::class.java).ifPresent { that ->
            textTemplate = overlap.merge(this, that).textTemplate
        }
        return toOptional()
    }

    override fun from(container: DataContainer) = from(container as DataView)

    private fun from(container: DataView): Optional<HologramTextTemplateData> {
        container.getView(HologramKeys.HOLOGRAM_TEXT_TEMPLATE.query).ifPresent { dataView ->
            val configNode = DataTranslators.CONFIGURATION_NODE.translate(dataView)
            val textTemplate = configNode.getValue(TextTemplate::class.typeToken)
            this.textTemplate = textTemplate
        }
        return toOptional()
    }

    override fun copy() = HologramTextTemplateData(textTemplate)

    override fun asImmutable() = Immutable(textTemplate)

    override fun getContentVersion() = 1

    override fun toContainer(): DataContainer {
        val configNode = SimpleConfigurationNode.root().setValue(TextTemplate::class.typeToken, textTemplate)
        val container = DataTranslators.CONFIGURATION_NODE.translate(configNode)
        return super.toContainer().set(HologramKeys.HOLOGRAM_TEXT_TEMPLATE.query, container)
    }

    class Immutable(val textTemplate: TextTemplate = TextTemplate.EMPTY) : AbstractImmutableData<Immutable, HologramTextTemplateData>() {

        val textTemplateValue: ImmutableValue<TextTemplate>
            get() = Sponge.getRegistry().valueFactory.createValue(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, textTemplate).asImmutable()

        init {
            registerGetters()
        }

        override fun registerGetters() {
            registerFieldGetter(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, { textTemplate })
            registerKeyValue(HologramKeys.HOLOGRAM_TEXT_TEMPLATE, { textTemplateValue })
        }

        override fun asMutable() = HologramTextTemplateData(textTemplate)

        override fun getContentVersion() = 1

        override fun toContainer(): DataContainer {
            val configNode = SimpleConfigurationNode.root().setValue(TextTemplate::class.typeToken, textTemplate)
            val container = DataTranslators.CONFIGURATION_NODE.translate(configNode)
            return super.toContainer().set(HologramKeys.HOLOGRAM_TEXT_TEMPLATE.query, container)
        }
    }

    class Builder internal constructor() : AbstractDataBuilder<HologramTextTemplateData>(HologramTextTemplateData::class.java, 1), DataManipulatorBuilder<HologramTextTemplateData, Immutable> {

        override fun create() = HologramTextTemplateData()

        override fun createFrom(dataHolder: DataHolder): Optional<HologramTextTemplateData> = create().fill(dataHolder)

        @Throws(InvalidDataException::class)
        override fun buildContent(container: DataView) = create().from(container)
    }
}