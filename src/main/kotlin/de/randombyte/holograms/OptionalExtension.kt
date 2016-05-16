package de.randombyte.holograms

import java.util.*

/**
 * Makes living with Optionals easier.
 */
class OptionalExtension {

    companion object {
        /**
         * @property T Type of [Optional]
         * @property R Return type of the present and not present func
         */
        fun <T, R> Optional<T>.presence(func: (T) -> R): OptionalPresence<T, R> = OptionalPresence(this, func)
    }

    class OptionalPresence<T, R>(val optional: Optional<T>, val presentFunc: (T) -> R) {
        fun absence(absentFunc: () -> R): R {
            return if (optional.isPresent) presentFunc.invoke(optional.get()) else absentFunc.invoke()
        }
    }
}