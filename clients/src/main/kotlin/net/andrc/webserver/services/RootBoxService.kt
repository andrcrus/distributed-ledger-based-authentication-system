package net.andrc.webserver.services

import net.andrc.webserver.config.RootBoxServiceConfig
import net.andrc.items.Container
import net.andrc.items.Item
import net.andrc.items.RootContainer
import org.springframework.stereotype.Service

/**
 * @author andrey.makhnov
 */
@Service
class RootBoxService(rootBoxConfig: RootBoxServiceConfig) {
    private val lock = Object()
    private val rootContainer = RootContainer(rootBoxConfig.maxCapacity, rootBoxConfig.name, rootBoxConfig.owner)

    fun putContainer(container: Container): Boolean {
        synchronized(lock) {
            return rootContainer.putContainer(container)
        }
    }

    fun putItem(item: Item): Boolean {
        synchronized(lock) {
            return rootContainer.putItem(item)
        }
    }

    fun deleteItem(itemName: String) {
        synchronized(lock) {
            rootContainer.deleteItem(itemName)
        }
    }

    fun deleteContainer(containerName: String) {
        synchronized(lock) {
            rootContainer.deleteContainer(containerName)
        }
    }

    fun getAllContainers(): List<Container> {
        return rootContainer.getImmutableContainers().map { it.value }
    }

    fun getAll(): Any {
        val result = HashMap<String, Map<String, Any>>()
        result["items"] = rootContainer.getImmutableItems()
        result["containers"] = rootContainer.getImmutableContainers()
        return result
    }
}
