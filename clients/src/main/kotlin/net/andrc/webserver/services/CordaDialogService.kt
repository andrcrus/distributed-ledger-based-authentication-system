package net.andrc.webserver.services

import net.andrc.flows.DeleteContainerFlow
import net.andrc.flows.OfficerAuthenticationRequestFlow
import net.andrc.flows.OfficerAuthenticationResponseFlow
import net.andrc.flows.PutContainerFlow
import net.andrc.items.Container
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

    fun registerNewContainer(container: Container): String  {
        if (rootBoxService.putContainer(container)) {
            val startFlowDynamic = proxy.startFlowDynamic(PutContainerFlow::class.java,
                    PutContainerState(container.name, container.maxCapacity,
                            container.getAllItems(), container.getContainersName(),container.owner,
                            participants = listOf(container.owner, proxy.nodeInfo().legalIdentities.first())))
            try {
                val signedTransaction =  startFlowDynamic.returnValue.get()
                return """
                    |{
                    | "txId" : "${signedTransaction.tx.id}",
                    | "containerName" : "${container.name}",
                    | "items" : ${container.getImmutableItems().map { it.value.getItemInfo() }},
                    | "containers" : ${container.getContainersName()}
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

    fun deleteContainer(containerName: String): String {
        try {
            val stateAndRef = proxy.vaultQuery(PutContainerState::class.java).states.first { it.state.data.containerName == containerName }
            val startFlowDynamic = proxy.startFlowDynamic(DeleteContainerFlow::class.java, stateAndRef)
            val result = startFlowDynamic.returnValue.get()
            rootBoxService.deleteContainer(containerName)
            return """
                |{
                | "txId" : "${result.tx.id}",
                | "containerName" : "$containerName"
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

    fun createAuthRequest(officerCertificate: OfficerCertificate, data: String, sign: String): String {
        val startFlowDynamic = proxy.startFlowDynamic(OfficerAuthenticationRequestFlow::class.java,
                OfficerAuthenticationRequestState(officerCertificate, data, sign, getParties(), proxy.nodeInfo().legalIdentities))
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
            |}
            |
        """.trimMargin()
    }

    fun createAuthResponse(officerCertificate: OfficerCertificate, data: String, sign: String, requestId: String, responseStatus: ResponseStatus): String {
        val startFlowDynamic = proxy.startFlowDynamic(OfficerAuthenticationResponseFlow::class.java,
                OfficerAuthenticationResponseState(officerCertificate, data, sign, getParties(),
                        responseStatus, requestId,proxy.nodeInfo().legalIdentities))
        val signedTransaction = startFlowDynamic.returnValue.get()
        return """
            |
            |{
            | "txId" : "${signedTransaction.tx.id}",
            | "requestId" : "$requestId"
            | "status" : "$responseStatus"
            |}
            |
        """.trimMargin()
    }
}