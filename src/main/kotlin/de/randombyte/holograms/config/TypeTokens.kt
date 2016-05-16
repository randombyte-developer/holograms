package de.randombyte.holograms.config

import com.flowpowered.math.vector.Vector3d
import com.google.common.reflect.TypeToken
import de.randombyte.holograms.Hologram
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

object TypeTokens {
    val HOLOGRAM = object : TypeToken<Hologram>() {}
    val LOCATION = object : TypeToken<Location<World>>() {}
    val VECTOR3D = object : TypeToken<Vector3d>() {}
    val UUID = object : TypeToken<UUID>() {}
    val TEXT = object : TypeToken<Text>() {}
}