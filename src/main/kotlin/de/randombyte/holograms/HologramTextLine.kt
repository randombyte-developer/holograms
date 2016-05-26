package de.randombyte.holograms

import org.spongepowered.api.text.Text
import java.util.*

/**
 * Replaces the old Pair<UUID, Text> which represented the UUID of the ArmorStand and its display text.
 */
data class HologramTextLine(val armorStandUUID: UUID, val displayText: Text)