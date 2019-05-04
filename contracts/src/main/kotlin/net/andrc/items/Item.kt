package net.andrc.items

import net.corda.core.serialization.CordaSerializable

/**
 * @author andrey.makhnov
 */
@CordaSerializable
data class Item (
        val name: String,
        val capacity: Long,
        val certificate: ItemCertificate,
        val properties : List<ItemProperties>
)