package de.randombyte.holograms.config

import de.randombyte.holograms.Hologram
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.loader.ConfigurationLoader
import org.spongepowered.api.world.extent.Extent
import java.util.*

object ConfigManager {

    /**
     * Example:
     * worlds {
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

    fun addHologram(extent: Extent, hologram: Hologram) = setHolograms(extent, getHolograms(extent) + hologram)
    fun deleteHologram(extent: Extent, uuid: UUID) = setHolograms(extent, getHolograms(extent).filterNot { it.uuid.equals(uuid) })

    fun getHolograms(extent: Extent): List<Hologram> = getHologramsNode(extent).childrenMap.map { hologramNode ->
        HologramSerializer.deserialize(hologramNode.value)
    }

    fun setHolograms(extent: Extent, holograms: List<Hologram>) {
        val hologramsNode = getHologramsNode(extent)
        hologramsNode.value = null //Clear
        holograms.forEach { hologram ->
            HologramSerializer.serialize(hologram, hologramsNode.getNode(hologram.uuid))
        }
        configLoader.save(hologramsNode.parent.parent)
    }

    private fun getRootNode() = configLoader.load()
    private fun getWorldsNode() = getRootNode().getNode(WORLDS_NODE)
    private fun getHologramsNode(extent: Extent) = getWorldsNode().getNode(extent.uniqueId.toString())
}