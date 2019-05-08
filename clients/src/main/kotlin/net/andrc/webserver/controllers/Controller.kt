package net.andrc.webserver.controllers

import net.andrc.items.*
import net.andrc.states.*
import net.andrc.utils.generateKeyPair
import net.andrc.utils.signData
import net.andrc.webserver.cordaCommon.NodeRPCConnection
import net.andrc.webserver.cordaCommon.toJson
import net.andrc.webserver.events.Publisher
import net.andrc.webserver.services.CordaDialogService
import net.corda.core.transactions.SignedTransaction
import org.jgroups.util.Base64
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.KeyPair
import java.security.SecureRandom

/**
 * @author andrey.makhnov
 */
@RestController
@RequestMapping("/")
class Controller(rpc: NodeRPCConnection, private val cordaDialogService: CordaDialogService, private val publisher: Publisher) {
    private var counter = 0L

    private val geoData = GeoData("NY", "USA", 40.7878800, -74.0143100)

    private val secureRandom = SecureRandom()

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy


    private fun initContainer(): Container {
        val result = Container(10, "Container#${counter++}", proxy.partiesFromName("GlassContainer", false).first())
        for (i in 1..2) {
            result.putItem(generateItem())
        }
        return result
    }

    private fun generateItem(): Item {
        val certificate = ItemCertificate()
        return Item("Glass", 5, certificate, listOf(ItemProperties.FRAGILE))
    }

    @GetMapping(value = ["/about"], produces = ["text/plain"])
    fun about(): String {
        logger.debug("Request to about...")
        return proxy.nodeInfo().toString()
    }

    @GetMapping(value = ["/peers"], produces = ["application/json"])
    fun peers() = mapOf("peers" to proxy.networkMapSnapshot()
            .filter { it != proxy.nodeInfo() }
            .map { it.toString() }
    )

    @GetMapping(value = ["/containers/register"], produces = ["application/json"])
    fun putContainer(): ResponseEntity<String> {
        lateinit var result: String
        try {
            result = cordaDialogService.registerNewContainer(initContainer(), geoData)
        }
        catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(e.message)
        }
        return ResponseEntity.ok(result)
    }

    @GetMapping(value = ["/containers/registered"], produces = ["application/json"])
    fun registered(): List<String> {
        return proxy.vaultQuery(PutContainerState::class.java).states.map { it.state.data.toString() }
    }

    @GetMapping(value = ["/containers/all"], produces = ["application/json"])
    fun allContainers(): String = cordaDialogService.rootBoxService.getAll().toString()

    @GetMapping(value = ["/containers/delete/{name}"], produces = ["application/json"])
    fun deleteContainer(@PathVariable name: String): ResponseEntity<String> {
        lateinit var result: String
        try {
            val realName = String(Base64.decode(name))
            result = cordaDialogService.deleteContainer(realName, geoData)
        }
        catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(e.message)
        }
        return ResponseEntity.ok(result)
    }

    @GetMapping(value = ["/containers/auth/request"], produces = ["application/json"])
    fun createAuthReq(): String {
        val keyPair = generateKeyPair()
        val data = secureRandom.nextLong().toString()
        val sign = signData(data, keyPair.private)
        return cordaDialogService.createAuthRequest(initOfficerRequest(keyPair), data, sign, geoData)
    }

    @GetMapping(value = ["/containers/auth/requests"], produces = ["application/json"])
    fun authReqs(): List<String> {
        return proxy.vaultQuery(OfficerAuthenticationRequestState::class.java).states.map { it.state.data.toString() }
    }

    @GetMapping(value = ["/containers/auth/responses"], produces = ["application/json"])
    fun authResps(): List<String> {
        return proxy.vaultQuery(OfficerAuthenticationResponseState::class.java).states.map { it.state.data.toString() }
    }

    @GetMapping(value = ["/containers/deleted"], produces = ["application/json"])
    fun deleted(): List<String> {
        return proxy.vaultQuery(DeleteContainerState::class.java).states.map { it.state.data.toString() }
    }

    @GetMapping(value = ["/containers/carriers"], produces = ["application/json"])
    fun carriers(): List<String> {
        return proxy.vaultQuery(ChangeCarrierState::class.java).states.map { it.state.data.toString() }
    }

    private fun initOfficerRequest(keyPair: KeyPair): OfficerCertificate {
        return OfficerCertificate("Andrey Makhnov", "Big Government Org", keyPair.public)
    }

    @GetMapping(value = ["/containers/auth/response/{id}"], produces = ["application/json"])
    fun createAuthResp(@PathVariable id: String): String {
        val keyPair = generateKeyPair()
        val data = secureRandom.nextLong().toString()
        val sign = signData(data, keyPair.private)
        val status = if (secureRandom.nextInt() % 2 == 0)  ResponseStatus.OK else ResponseStatus.FAILED
        return cordaDialogService.createAuthResponse(initOfficerRequest(keyPair), data, sign, id, status, geoData)
    }

    @GetMapping(value = ["/containers/change-carrier"], produces = ["application/json"])
    fun changeCarrier(): String {
        val keyPair = generateKeyPair()
        val carrierCert = CarrierCertificate(keyPair.public)
        val carrier = Carrier( "OOO PEREVOZKA PRO", carrierCert)
        val data = secureRandom.nextLong().toString()
        val sign = signData(data, keyPair.private)
        return cordaDialogService.changeCarrier(carrier, data, sign, geoData)
    }

    @GetMapping(value = ["/event/publish/{name}"], produces = ["application/json"])
    fun publish(@PathVariable name: String): String {
        return publisher.publish(name)
    }

    @GetMapping(value = ["/event/published"], produces = ["application/json"])
    fun published(): List<String> {
        return proxy.vaultQuery(CarrierEventState::class.java).states.map { it.state.data.toString() }
    }
}