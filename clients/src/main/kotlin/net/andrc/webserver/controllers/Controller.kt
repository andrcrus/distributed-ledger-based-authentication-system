package net.andrc.webserver.controllers

import net.andrc.items.Container
import net.andrc.items.Item
import net.andrc.items.ItemCertificate
import net.andrc.states.PutContainerState
import net.andrc.webserver.cordaCommon.NodeRPCConnection
import net.andrc.webserver.services.CordaDialogService
import net.corda.core.crypto.SecureHash
import net.corda.core.transactions.SignedTransaction
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.util.*


/**
 * @author andrey.makhnov
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection, val cordaDialogService: CordaDialogService) {
    private var counter = 0L

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy


    fun initContainer(): Container {
        val result = Container(10, "Container#${counter++}", proxy.partiesFromName("RootContainer", false).first())
        for (i in 1..10) {
            result.putItem(generateItem())
        }
        return result
    }

    private fun generateItem(): Item {
        val keyGen = KeyPairGenerator.getInstance("DSA", "SUN")
        val random = SecureRandom.getInstance("SHA1PRNG", "SUN")
        keyGen.initialize(1024, random)
        val pair = keyGen.generateKeyPair()
        val certificate = ItemCertificate(publicKey = pair.public, privateKey = pair.private)
        return Item("Tomatos", 1, certificate, listOf())
    }

    @GetMapping(value = ["/about"], produces = ["text/plain"])
    fun templateendpoint(): String {
        logger.debug("Request to about...")
        return proxy.nodeInfo().toString()
    }

    @GetMapping(value = ["/peers"], produces = ["application/json"])
    fun peers() = mapOf("peers" to proxy.networkMapSnapshot()
            .filter { it != proxy.nodeInfo() }
            .map { it.toString() }
    )

    @GetMapping(value = ["/containers/register"], produces = ["application/json"])
    fun putContainer(): ResponseEntity<Any> {
        lateinit var result: SignedTransaction
        try {
             result = cordaDialogService.registerNewContainer(initContainer())
        }
        catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(e.message)
        }
        return ResponseEntity.ok(result)
    }

    @GetMapping(value = ["/containers/registered"], produces = ["application/json"])
    fun vaccinationRecords(): String = proxy.vaultQuery(PutContainerState::class.java).toString()
}