package net.andrc.webserver.items

import net.andrc.webserver.cordaCommon.ItemCertificate

data class Item (
        val name: String,
        val capacity: Long,
        val certificate: ItemCertificate,
        val properties : List<ItemProperties>
)