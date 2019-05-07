package net.andrc.utils

import java.security.*
import java.util.*

/**
 * @author andrey.makhnov
 */

fun generateKeyPair(): KeyPair {
    val generator = KeyPairGenerator.getInstance("RSA")

    generator.initialize(2048, SecureRandom())

    return generator.generateKeyPair()
}

fun signData(plainText: String, privateKey: PrivateKey): String {
    val privateSignature = Signature.getInstance("SHA256withRSA")
    privateSignature.initSign(privateKey)
    privateSignature.update(plainText.toByteArray())

    val signature = privateSignature.sign()

    return Base64.getEncoder().encodeToString(signature)
}

fun verifySign(plainText: String, signature: String, publicKey: PublicKey): Boolean {
    val publicSignature = Signature.getInstance("SHA256withRSA")
    publicSignature.initVerify(publicKey)
    publicSignature.update(plainText.toByteArray())

    val signatureBytes = Base64.getDecoder().decode(signature)

    return publicSignature.verify(signatureBytes)
}
