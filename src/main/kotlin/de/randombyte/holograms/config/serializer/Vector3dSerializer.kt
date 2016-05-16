package de.randombyte.holograms.config.serializer

import com.flowpowered.math.vector.Vector3d
import com.google.common.reflect.TypeToken
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer

object Vector3dSerializer : TypeSerializer<Vector3d> {
    override fun deserialize(type: TypeToken<*>, value: ConfigurationNode): Vector3d {
        val coordinates = value.getValue(object : TypeToken<List<Double>>() {})
        return Vector3d(coordinates[0], coordinates[1], coordinates[2])
    }

    override fun serialize(type: TypeToken<*>, vector3d: Vector3d, value: ConfigurationNode) {
        value.value = listOf(vector3d.x, vector3d.y, vector3d.z)
    }
}