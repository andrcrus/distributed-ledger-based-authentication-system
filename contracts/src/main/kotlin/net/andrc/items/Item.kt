package net.andrc.items

/**
 * @author andrey.makhnov
 */
data class Item (
        val name: String,
        val capacity: Long,
        val certificate: ItemCertificate,
        val properties : List<ItemProperties>
)