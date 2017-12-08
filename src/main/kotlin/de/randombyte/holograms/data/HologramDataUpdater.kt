package de.randombyte.holograms.data

import org.spongepowered.api.data.DataView
import org.spongepowered.api.data.persistence.DataContentUpdater

/**
 * Created by randombyte on 20.03.17.
 */
class HologramDataUpdater : DataContentUpdater {
    override fun getInputVersion() = 1

    override fun getOutputVersion() = 2

    override fun update(content: DataView?): DataView {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}