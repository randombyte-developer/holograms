package de.randombyte.holograms.config.serializer

import com.google.common.reflect.TypeToken
import ninja.leaping.configurate.ConfigurationNode
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers
import java.util.*

object HologramSerializer {

    val LIST_LIST_STRING_TYPE = object : TypeToken<List<List<String>>>() {}

    fun deserialize(value: ConfigurationNode): List<Pair<UUID, Text>> {
        val list = value.getValue(LIST_LIST_STRING_TYPE)
        return list.map { strings ->
            UUID.fromString(strings[0]) to TextSerializers.FORMATTING_CODE.deserialize(strings[1])
        }
    }

    fun serialize(lines: List<Pair<UUID, Text>>, value: ConfigurationNode): ConfigurationNode {
        val stringPairs = lines.map { line ->
            listOf(line.first.toString(), TextSerializers.FORMATTING_CODE.serialize(line.second))
        }
        return value.setValue(LIST_LIST_STRING_TYPE, stringPairs)
    }
}