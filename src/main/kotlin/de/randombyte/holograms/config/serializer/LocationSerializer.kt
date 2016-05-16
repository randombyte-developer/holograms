package de.randombyte.holograms.config.serializer

import com.flowpowered.math.vector.Vector3d
import com.google.common.reflect.TypeToken
import de.randombyte.holograms.OptionalExtension.Companion.presence
import de.randombyte.holograms.config.TypeTokens
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.objectmapping.ObjectMappingException
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer
import org.spongepowered.api.Sponge
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

object LocationSerializer : TypeSerializer<Location<World>> {
    override fun deserialize(type: TypeToken<*>?, value: ConfigurationNode): Location<World> {
        val uuid = value.getNode("worldUUID").getValue(TypeTokens.UUID)
        val optWorld = Sponge.getServer().getWorld(uuid)
        return optWorld.presence { world ->
            Location<World>(world, value.getNode("position").getValue(TypeToken.of(Vector3d::class.java)))
        }.absence {
            throw ObjectMappingException("World with UUID $uuid isn't loaded!")
        }
    }

    override fun serialize(type: TypeToken<*>, location: Location<World>, value: ConfigurationNode) {
        value.getNode("worldUUID").setValue(TypeTokens.UUID, location.extent.uniqueId)
        value.getNode("position").setValue(TypeTokens.VECTOR3D, location.position)
    }
}
