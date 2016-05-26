package de.randombyte.holograms.config

import com.google.common.reflect.TypeToken
import de.randombyte.holograms.Hologram
import de.randombyte.holograms.HologramTextLine
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.text.serializer.TextSerializers
import java.util.*

object HologramSerializer {

    val LIST_LIST_STRING_TYPE = object : TypeToken<List<List<String>>>() {}

    fun deserialize(value: ConfigurationNode): Hologram =
            Hologram(UUID.fromString(value.key as String), value.getValue(LIST_LIST_STRING_TYPE).map { strings ->
                HologramTextLine(UUID.fromString(strings[0]), TextSerializers.FORMATTING_CODE.deserialize(strings[1]))
            })

    fun serialize(hologram: Hologram, value: ConfigurationNode): ConfigurationNode =
            value.setValue(LIST_LIST_STRING_TYPE, hologram.lines.map { line ->
                listOf(line.armorStandUUID.toString(), TextSerializers.FORMATTING_CODE.serialize(line.displayText))
            })
}