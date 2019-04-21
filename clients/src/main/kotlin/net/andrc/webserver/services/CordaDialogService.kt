package net.andrc.webserver.services

import net.andrc.flows.PutContainerFlow
import net.andrc.webserver.cordaCommon.NodeRPCConnection
import net.andrc.webserver.exceptions.OutOfContainerCapacityException
import net.andrc.items.Container
import net.andrc.states.PutContainerState
import net.corda.core.transactions.SignedTransaction
import org.springframework.stereotype.Service

@Service
class CordaDialogService(private val rootBoxService: RootBoxService, rpc: NodeRPCConnection) {
    private val proxy = rpc.proxy

    fun registerNewContainer(container: Container): SignedTransaction  {
        if (rootBoxService.putContainer(container)) {
            val startFlowDynamic = proxy.startFlowDynamic(PutContainerFlow::class.java, PutContainerState(container))
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
}