package de.randombyte.holograms.data

import com.google.common.reflect.TypeToken
import de.randombyte.kosp.extensions.typeToken
import org.spongepowered.api.data.DataQuery
import org.spongepowered.api.data.key.Key
import org.spongepowered.api.data.key.KeyFactory
import org.spongepowered.api.data.value.mutable.Value
import org.spongepowered.api.text.TextTemplate
import java.time.Duration

object HologramKeys {
    val IS_HOLOGRAM: Key<Value<Boolean>> = KeyFactory.makeSingleKey(
            Boolean::class.typeToken,
            object : TypeToken<Value<Boolean>>() {},
            DataQuery.of("IsHologram"), "holograms:is_hologram", "Is Hologram")

    val HOLOGRAM_TEXT_TEMPLATE: Key<Value<TextTemplate>> = KeyFactory.makeSingleKey(
            TextTemplate::class.typeToken,
            object : TypeToken<Value<TextTemplate>>() {},
            DataQuery.of("HologramTextTemplate"), "holograms:hologram_texttemplate", "Hologram TextTemplate")

    val HOLOGRAM_UPDATE_INTERVAL: Key<Value<Duration>> = KeyFactory.makeSingleKey(
            Duration::class.typeToken,
            object : TypeToken<Value<Duration>>() {},
            DataQuery.of("HologramUpdateInterval"), "holograms:hologram_update_interval", "Hologram Update Interval")
}