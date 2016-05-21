package de.randombyte.holograms.config.serializer

import com.google.common.reflect.TypeToken
import de.randombyte.holograms.Hologram
import de.randombyte.holograms.config.TypeTokens
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer

/**
 * @param logger For reporting errors
 */
object HologramSerializer : TypeSerializer<Hologram> {

    const val ARMORSTAND_UUID_NODE = "armorStandUUID"
    const val TEXT_NODE = "text"

    /**
     * @return null if Hologram can't be created
     */
    override fun deserialize(type: TypeToken<*>, value: ConfigurationNode): Hologram? {
        val uuid = value.getNode(ARMORSTAND_UUID_NODE).getValue(TypeTokens.UUID)
        val text = value.getNode(TEXT_NODE).getValue(TypeTokens.TEXT)
        return Hologram(text, uuid)
    }

    override fun serialize(type: TypeToken<*>, hologram: Hologram, value: ConfigurationNode) {
        value.getNode(ARMORSTAND_UUID_NODE).setValue(TypeTokens.UUID, hologram.armorStandUUID)
        value.getNode(TEXT_NODE).setValue(TypeTokens.TEXT, hologram.text)
    }
}