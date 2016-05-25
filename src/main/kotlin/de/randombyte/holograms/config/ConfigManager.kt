package de.randombyte.holograms.config

import de.randombyte.holograms.config.serializer.HologramSerializer
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.extent.Extent
import java.util.*

object ConfigManager {

    /**
     * holograms {
     *     <worldUUID1> {
     *         <hologramsUUID1> [
     *             [<armorStandUUID1>, <text1>],
     *             [<armorStandUUID2>, <text2>],
     *             [<armorStandUUID3>, <text3>]
     *         ]
     *     }
     * }
     */

    const val WORLDS_NODE = "worlds"

    //Initialized in init phase of plugin
    lateinit var configLoader: ConfigurationLoader<CommentedConfigurationNode>

    fun addHologram(extent: Extent, lines: List<Pair<UUID, Text>>) {
        setHolograms(extent, getHolograms(extent).toMutableList() + Pair(UUID.randomUUID(), lines))
    }

    //List<Pair<hologramUUID, List<Pair<armorStandUUID, armorStandText>>>>
    fun getHolograms(extent: Extent): List<Pair<UUID, List<Pair<UUID, Text>>>> {
        return getHologramsNode(extent).childrenMap.map { hologramNode ->
            UUID.fromString(hologramNode.key as String) to HologramSerializer.deserialize(hologramNode.value)
        }
    }

    fun setHolograms(extent: Extent, holograms: List<Pair<UUID, List<Pair<UUID, Text>>>>) {
        val hologramsNode = getHologramsNode(extent)
        holograms.forEach { hologram ->
            HologramSerializer.serialize(hologram.second, hologramsNode.getNode(hologram.first))
        }
        configLoader.save(hologramsNode.parent.parent)
    }

    private fun getRootNode() = configLoader.load()
    private fun getExtentsNode() = getRootNode().getNode(WORLDS_NODE)
    private fun getHologramsNode(extent: Extent) = getExtentsNode().getNode(extent.uniqueId.toString())
}