package de.randombyte.holograms.config.serializer

import com.google.common.reflect.TypeToken
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers

/**
 * Serializer that represents the [Text] with the help of [FormattingCodeTextSerializer].
 */
object FormattingCodeTextSerializer : TypeSerializer<Text> {
    override fun deserialize(type: TypeToken<*>, value: ConfigurationNode) =
            TextSerializers.FORMATTING_CODE.deserialize(value.string)

    override fun serialize(type: TypeToken<*>, text: Text, value: ConfigurationNode) {
        value.value = TextSerializers.FORMATTING_CODE.serialize(text)
    }
}