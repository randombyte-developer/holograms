package de.randombyte.holograms.config

import de.randombyte.holograms.Hologram
import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable
import org.spongepowered.api.text.Text
import java.util.*

@ConfigSerializable
class Config(
        @Setting val worlds: MutableMap<UUID, World> = mutableMapOf()
) {
    @ConfigSerializable
    class World(
            @Setting val holograms: MutableMap<UUID, Hologram> = mutableMapOf()
    ) {
        @ConfigSerializable
        class Hologram(
                @Setting val armorStandUUID: UUID = UUID(0, 0),
                @Setting val text: Text = Text.EMPTY
        )
    }

    fun addHologram(hologram: Hologram, worldUUID: UUID): Config {
        val world = worlds.getOrPut(worldUUID, { World() })
        val configHologram = World.Hologram(hologram.armorStandUUID, hologram.text)
        world.holograms += (configHologram.armorStandUUID to configHologram)
        return this
    }

    fun deleteHologram(armorStandUUID: UUID, worldUUID: UUID): Config {
        worlds[worldUUID]?.holograms?.remove(armorStandUUID)
        return this
    }
}