package com.template.webserver

import com.template.flows.Initiator
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = ["/about"], produces = ["text/plain"])
    fun templateendpoint(): String {
        logger.debug("Request to about...")
        proxy.startFlowDynamic(Initiator::class.java)
        return proxy.nodeInfo().toString()
    }

    @GetMapping(value = ["/peers"], produces = ["application/json"])
    fun peers() = mapOf("peers" to proxy.networkMapSnapshot()
            .filter { it != proxy.nodeInfo() }
            .map { it.toString() }
    )
}