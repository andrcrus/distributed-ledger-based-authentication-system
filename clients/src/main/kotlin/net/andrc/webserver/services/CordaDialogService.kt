package net.andrc.webserver.services

import net.andrc.flows.*
import net.andrc.items.Carrier
import net.andrc.items.Container
import net.andrc.items.GeoData
import net.andrc.items.OfficerCertificate
import net.andrc.states.*
import net.andrc.webserver.cordaCommon.NodeRPCConnection
import net.andrc.webserver.cordaCommon.toJson
import net.andrc.webserver.exceptions.OutOfContainerCapacityException
import net.andrc.webserver.exceptions.UnknownContainerException
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import org.springframework.stereotype.Service

/**
 * @author andrey.makhnov
 */
@Service
class CordaDialogService(val rootBoxService: RootBoxService, rpc: NodeRPCConnection) {
    private val proxy = rpc.proxy

    fun registerNewContainer(container: Container, geoData: GeoData): String  {
        if (rootBoxService.putContainer(container)) {
            val startFlowDynamic = proxy.startFlowDynamic(PutContainerFlow::class.java,
                    PutContainerState(container.name, container.maxCapacity,
                            container.getAllItems(), container.getContainersName(),container.owner,
                            participants = listOf(container.owner, proxy.nodeInfo().legalIdentities.first()), geoData = geoData))
            try {
                val signedTransaction =  startFlowDynamic.returnValue.get()
                return """
                    |
                    |{
                    | "txId" : "${signedTransaction.tx.id}",
                    | "containerName" : "${container.name}",
                    | "items" : ${container.getImmutableItems().map { it.value.getItemInfo() }},
                    | "containers" : ${container.getContainersName()},
                    | "geo" : $geoData
                    |}
                    |
                """.trimMargin()
            }catch (e: Exception) {
                rootBoxService.deleteContainer(container.name)
                throw e
            }
        }
        else {
            throw OutOfContainerCapacityException("Oh my god. It's so big.")
        }
    }

    fun deleteContainer(containerName: String, geoData: GeoData): String {
        try {
            val stateAndRef = proxy.vaultQuery(PutContainerState::class.java).states.first { it.state.data.containerName == containerName }
            val startFlowDynamic = proxy.startFlowDynamic(DeleteContainerFlow::class.java, stateAndRef, geoData)
            val result = startFlowDynamic.returnValue.get()
            rootBoxService.deleteContainer(containerName)
            return """
                |{
                | "txId" : "${result.tx.id}",
                | "containerName" : "$containerName",
                | "geo" : $geoData
                |}
                |
            """.trimMargin()
        }
        catch (e: NoSuchElementException) {
            throw UnknownContainerException("$containerName does not exists in ledger.")
        }
        catch (e: Exception) {
            throw e
        }
    }

    private fun getParties(): List<Party> {
        return proxy.networkMapSnapshot().filter { it != proxy.nodeInfo() }.map { it.legalIdentities.first() }
    }

    fun createAuthRequest(officerCertificate: OfficerCertificate, data: String, sign: String, geoData: GeoData): String {
        val startFlowDynamic = proxy.startFlowDynamic(OfficerAuthenticationRequestFlow::class.java,
                OfficerAuthenticationRequestState(officerCertificate, data, sign, getParties(), geoData, proxy.nodeInfo().legalIdentities))
        val requestId = startFlowDynamic.returnValue.get()
        val deletedContainers = proxy.vaultQuery(DeleteContainerState::class.java).states.map { it.state.data.containerName }.toSet()
        val itemsCertificate = proxy.vaultQuery(PutContainerState::class.java).states
                .filter { !deletedContainers.contains(it.state.data.containerName) }
                .map { it.state.data.items }.flatten().map { it.certificate }
        return """
            |
            |{
            |"id": "$requestId",
            |"itemsCertificate": "$itemsCertificate",
            |"geo" : $geoData
            |}
            |
        """.trimMargin()
    }

    fun createAuthResponse(officerCertificate: OfficerCertificate, data: String, sign: String, requestId: String, responseStatus: ResponseStatus, geoData: GeoData): String {
        val startFlowDynamic = proxy.startFlowDynamic(OfficerAuthenticationResponseFlow::class.java,
                OfficerAuthenticationResponseState(officerCertificate, data, sign, getParties(),
                        responseStatus, requestId, geoData, proxy.nodeInfo().legalIdentities))
        val signedTransaction = startFlowDynamic.returnValue.get()
        return """
            |
            |{
            | "txId" : "${signedTransaction.tx.id}",
            | "requestId" : "$requestId",
            | "status" : "$responseStatus",
            | "geo" : $geoData
            |}
            |
        """.trimMargin()
    }

    fun changeCarrier(carrier: Carrier, data: String, signature: String, geoData: GeoData): String  {
        val changeCarrierState = ChangeCarrierState(carrier, data, signature, geoData, getParties())
        val startFlowDynamic = proxy.startFlowDynamic(ChangeCarrierFlow::class.java,
                changeCarrierState)
        val signedTransaction = startFlowDynamic.returnValue.get()
        return """
            |
            |{
            | "txId" : "${signedTransaction.tx.id}",
            | "organizationName" : "${carrier.organizationName}",
            | "date" : "${changeCarrierState.date}",
            | "geo" : $geoData
            |}
            |
        """.trimMargin()
    }
}