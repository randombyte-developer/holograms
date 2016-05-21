package de.randombyte.holograms.config

import com.google.common.reflect.TypeToken
import de.randombyte.holograms.Hologram
import de.randombyte.holograms.config.serializer.FormattingCodeTextSerializer
import de.randombyte.holograms.config.serializer.HologramSerializer
import de.randombyte.holograms.config.serializer.LocationSerializer
import de.randombyte.holograms.config.serializer.Vector3dSerializer
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.ConfigurationOptions
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers
import java.util.*

object ConfigManager {

    const val HOLOGRAMS_NODE = "holograms"

    //Initialized in init phase of plugin
    lateinit var configLoader: ConfigurationLoader<CommentedConfigurationNode>

    //Register all (de)serializers
    val configOptions: ConfigurationOptions by lazy {
        ConfigurationOptions.defaults()
                .setSerializers(TypeSerializers.getDefaultSerializers().newChild()
                        .registerType(TypeTokens.HOLOGRAM, HologramSerializer)
                        .registerType(TypeTokens.LOCATION, LocationSerializer)
                        .registerType(TypeTokens.VECTOR3D, Vector3dSerializer)
                        .registerType(TypeTokens.TEXT, FormattingCodeTextSerializer))
    }

    fun load(path: String) = configLoader.load(configOptions).getNode(path)
    fun save(node: ConfigurationNode) = configLoader.save(node)

    fun addHologram(hologram: Hologram) = setHolograms(getHolograms() + hologram)
    fun deleteHologramByArmorStandUUID(uuid: UUID) = setHolograms(getHolograms().filter { !it.armorStandUUID!!.equals(uuid) })
    fun getHolograms() = load(HOLOGRAMS_NODE).getList(TypeToken.of(Hologram::class.java))
    fun setHolograms(holograms: List<Hologram>) =
            save(load(HOLOGRAMS_NODE).setValue(object : TypeToken<List<Hologram>>() {}, holograms).parent)
}