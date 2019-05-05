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
class CordaDialogService(val rootBoxService: RootBoxService, private val rpc: NodeRPCConnection) {
    private val proxy = rpc.proxy

    private val proxies = HashMap<CordaX500Name, CordaRPCConnection>()

    private fun findRpcConnection(owner: Party): CordaRPCOps {
        if (proxy.nodeInfo().legalIdentities.contains(owner)) {
            return proxy
        }
        if (proxies[owner.name] != null) {
            return proxies[owner.name]!!.proxy
        }
        try {
            val nodeInfo = proxy.networkMapSnapshot().first { it.legalIdentities.contains(owner) }.addresses.first()
            val rpcAddress = NetworkHostAndPort(nodeInfo.host, nodeInfo.port)
            val rpcClient = CordaRPCClient(rpcAddress)
            val rpcConnection = rpcClient.start(rpc.username, rpc.password)
            proxies[owner.name] = rpcConnection
            return rpcConnection.proxy
        }
        catch (e: Exception) {
            throw UnknownOwnerException("${owner.name} does not exists right now. May be you forget to start node?")
        }
    }

    fun registerNewContainer(container: Container): SignedTransaction  {
        if (rootBoxService.putContainer(container)) {
            val proxyCur = findRpcConnection(container.owner)
            val startFlowDynamic = proxyCur.startFlowDynamic(PutContainerFlow::class.java,
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
        val proxyCur = findRpcConnection(stateAndRef.state.data.owner)
        val startFlowDynamic = proxyCur.startFlowDynamic(DeleteContainerFlow::class.java, stateAndRef)
        val result = startFlowDynamic.returnValue.get()
        rootBoxService.deleteContainer(containerName)
        return result
    }

    @PreDestroy
    private fun destroy() {
        proxies.forEach { it.value.notifyServerAndClose() }
    }
}