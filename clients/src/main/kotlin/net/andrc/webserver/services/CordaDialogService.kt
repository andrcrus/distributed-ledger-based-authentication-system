package net.andrc.webserver.services

import net.andrc.flows.DeleteContainerFlow
import net.andrc.flows.OfficerAuthenticationRequestFlow
import net.andrc.flows.PutContainerFlow
import net.andrc.items.Container
import net.andrc.items.OfficerCertificate
import net.andrc.states.DeleteContainerState
import net.andrc.states.OfficerAuthenticationRequestState
import net.andrc.states.PutContainerState
import net.andrc.webserver.cordaCommon.NodeRPCConnection
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

    fun registerNewContainer(container: Container): SignedTransaction  {
        if (rootBoxService.putContainer(container)) {
            val startFlowDynamic = proxy.startFlowDynamic(PutContainerFlow::class.java,
                    PutContainerState(container.name, container.maxCapacity,
                            container.getAllItems(), container.getContainersName(),container.owner,
                            participants = listOf(container.owner, proxy.nodeInfo().legalIdentities.first())))
            try {
                return startFlowDynamic.returnValue.get()
            }catch (e: Exception) {
                rootBoxService.deleteContainer(container.name)
                throw e
            }
        }
        else {
            throw OutOfContainerCapacityException("Oh my god. It's so big.")
        }
    }

    fun deleteContainer(containerName: String): SignedTransaction {
        try {
            val stateAndRef = proxy.vaultQuery(PutContainerState::class.java).states.first { it.state.data.containerName == containerName }
            val startFlowDynamic = proxy.startFlowDynamic(DeleteContainerFlow::class.java, stateAndRef)
            val result = startFlowDynamic.returnValue.get()
            rootBoxService.deleteContainer(containerName)
            return result
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

    fun createAuthRequest(officerCertificate: OfficerCertificate): String {
        val startFlowDynamic = proxy.startFlowDynamic(OfficerAuthenticationRequestFlow::class.java,
                OfficerAuthenticationRequestState(officerCertificate, getParties(), proxy.nodeInfo().legalIdentities))
        val requestId = startFlowDynamic.returnValue.get()
        val deletedContainers = proxy.vaultQuery(DeleteContainerState::class.java).states.map { it.state.data.containerName }.toSet()
        val itemsCertificate = proxy.vaultQuery(PutContainerState::class.java).states
                .filter { !deletedContainers.contains(it.state.data.containerName) }
                .map { it.state.data.items }.flatten().map { it.certificate }
        return """
            |{
            |"id": $requestId,
            |"itemsCertificate": $itemsCertificate,
            |}
        """.trimMargin()
    }
}