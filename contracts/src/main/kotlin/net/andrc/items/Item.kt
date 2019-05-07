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
) {
    fun getItemInfo(): String {
        return """
            |
            |"item" : {
            | "itemName" : "$name",
            | "certificateId" : "${certificate.id}",
            | "creationDate" : "${certificate.creationDate}"
            | "expirationDate" : "${certificate.expirationDate}"
            |}
            |
        """.trimMargin()
    }
}