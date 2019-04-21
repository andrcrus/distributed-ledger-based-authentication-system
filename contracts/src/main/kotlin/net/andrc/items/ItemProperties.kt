package net.andrc.items

import net.corda.core.serialization.CordaSerializable

/**
 * @author andrey.makhnov
 */
@CordaSerializable
enum class ItemProperties {
    FRAGILE,
    SLOUGHING,
    FROZEN
}