package de.randombyte.holograms.config

import de.randombyte.holograms.HologramTextLine
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

    fun addHologram(extent: Extent, lines: List<HologramTextLine>) = setHolograms(extent, getHolograms(extent) + Pair(UUID.randomUUID(), lines))
    fun deleteHologram(extent: Extent, uuid: UUID) = setHolograms(extent, getHolograms(extent).filterNot { it.first.equals(uuid) })

    //List<Pair<hologramUUID, List<HologramTextLine>>>
    fun getHolograms(extent: Extent): List<Pair<UUID, List<HologramTextLine>>> =
            getHologramsNode(extent).childrenMap.map { hologramNode ->
                UUID.fromString(hologramNode.key as String) to HologramSerializer.deserialize(hologramNode.value)
            }

    fun setHolograms(extent: Extent, holograms: List<Pair<UUID, List<HologramTextLine>>>) {
        val hologramsNode = getHologramsNode(extent)
        hologramsNode.value = null //Clear
        holograms.forEach { hologram ->
            HologramSerializer.serialize(hologram.second, hologramsNode.getNode(hologram.first))
        }
        configLoader.save(hologramsNode.parent.parent)
    }

    private fun getRootNode() = configLoader.load()
    private fun getWorldsNode() = getRootNode().getNode(WORLDS_NODE)
    private fun getHologramsNode(extent: Extent) = getWorldsNode().getNode(extent.uniqueId.toString())
}