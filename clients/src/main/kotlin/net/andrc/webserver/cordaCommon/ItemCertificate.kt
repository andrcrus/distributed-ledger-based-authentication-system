package net.andrc.webserver.cordaCommon

import java.security.PrivateKey
import java.security.PublicKey
import java.util.*

data class ItemCertificate (
        val id: String = UUID.randomUUID().toString(),
        val publicKey: PublicKey,
        val privateKey: PrivateKey
)