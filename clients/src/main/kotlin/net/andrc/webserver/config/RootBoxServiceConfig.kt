package net.andrc.webserver.config

import net.andrc.webserver.cordaCommon.NodeRPCConnection
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

/**
 * @author andrey.makhnov
 */
@Configuration
open class RootBoxServiceConfig(rpc: NodeRPCConnection) {
    val proxy = rpc.proxy

    @Value("\${containers.root.name}")
    lateinit var name: String

    @Value("\${containers.root.maxCapacity}")
    var maxCapacity: Long = 0L

    val owner = proxy.notaryIdentities()[0]

    @PostConstruct
    fun init() {
        if (maxCapacity <= 0) {
            maxCapacity = 100
        }
    }

}