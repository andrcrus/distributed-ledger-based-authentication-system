package net.andrc.webserver.services

import net.andrc.flows.PutContainerFlow
import net.andrc.webserver.cordaCommon.NodeRPCConnection
import net.andrc.webserver.exceptions.OutOfContainerCapacityException
import net.andrc.items.Container
import net.andrc.states.PutContainerState
import org.springframework.stereotype.Service

@Service
class CordaDialogService(private val rootBoxService: RootBoxService, rpc: NodeRPCConnection) {
    private val proxy = rpc.proxy

    fun registerNewContainer(container: Container)  {
        if (rootBoxService.putContainer(container)) {
            proxy.startFlowDynamic(PutContainerFlow::class.java, PutContainerState(container))
        }
        else {
            throw OutOfContainerCapacityException("Oh my god. It's so big.")
        }
    }
}