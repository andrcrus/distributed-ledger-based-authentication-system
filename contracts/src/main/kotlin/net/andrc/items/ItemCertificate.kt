package net.andrc.items

import java.security.PrivateKey
import java.security.PublicKey
import java.util.*

/**
 * @author andrey.makhnov
 */
data class ItemCertificate (
        val id: String = UUID.randomUUID().toString(),
        val publicKey: PublicKey,
        val privateKey: PrivateKey
)