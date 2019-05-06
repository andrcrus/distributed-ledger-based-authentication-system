package net.andrc.webserver.services

import net.andrc.flows.DeleteContainerFlow
import net.andrc.flows.PutContainerFlow
import net.andrc.items.Container
import net.andrc.states.PutContainerState
import net.andrc.webserver.cordaCommon.NodeRPCConnection
import net.andrc.webserver.exceptions.OutOfContainerCapacityException
import net.andrc.webserver.exceptions.UnknownOwnerException
import net.corda.client.rpc.CordaRPCClient
import net.corda.client.rpc.CordaRPCConnection
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.NetworkHostAndPort
import org.springframework.stereotype.Service
import javax.annotation.PreDestroy

@Service
class CordaDialogService(val rootBoxService: RootBoxService, rpc: NodeRPCConnection) {
    private val proxy = rpc.proxy

    fun registerNewContainer(container: Container): SignedTransaction  {
        if (rootBoxService.putContainer(container)) {
            val startFlowDynamic = proxy.startFlowDynamic(PutContainerFlow::class.java,
                    PutContainerState(container.name, container.maxCapacity, container.getAllItems(), container.owner))
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
        val stateAndRef = proxy.vaultQuery(PutContainerState::class.java).states.first { it.state.data.containerName == containerName }
        val startFlowDynamic = proxy.startFlowDynamic(DeleteContainerFlow::class.java, stateAndRef)
        val result = startFlowDynamic.returnValue.get()
        rootBoxService.deleteContainer(containerName)
        return result
    }

}