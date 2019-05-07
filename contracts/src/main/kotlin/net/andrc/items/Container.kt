package net.andrc.items

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author andrey.makhnov
 *  Container class. Not thread safe.
 */
@CordaSerializable
open class Container(val maxCapacity: Long, val name: String, val owner: Party) {
    private var currentCapacity: Long = 0L

    protected open val containers: MutableMap<String, Container> = HashMap()
    protected open val items: MutableMap<String, Item> = HashMap()

    fun putContainer(container: Container): Boolean {
        if (currentCapacity + container.maxCapacity > maxCapacity) {
            return false
        }

        currentCapacity += container.maxCapacity
        containers[container.name] = container
        return true
    }

    fun putItem(item: Item): Boolean {
        if (currentCapacity + item.capacity > maxCapacity) {
            return false
        }

        currentCapacity += item.capacity
        items[item.certificate.id] = item
        return true
    }

    fun deleteItem(certificateId: String) {
        val item = items[certificateId] ?: return
        currentCapacity -= item.capacity
        items.remove(certificateId)
    }

    fun deleteContainer(containerName: String) {
        val container = containers[containerName] ?: return
        currentCapacity -= container.maxCapacity
        items.remove(containerName)
    }

    fun getImmutableContainers(): Map<String, Container> {
        return containers.toMap()
    }

    fun getImmutableItems(): Map<String, Item> {
        return items.toMap()
    }

    fun getContainersName(): List<String> {
        val containerQueue: Queue<Container> = LinkedList<Container>()
        val result: MutableList<Container> = ArrayList()
        result.addAll(getImmutableContainers().values)
        getImmutableContainers().values.forEach { containerQueue.add(it) }
        while (containerQueue.size != 0) {
            val current = containerQueue.poll()
            result.addAll(current.getImmutableContainers().values)
            current.getImmutableContainers().values.forEach { containerQueue.add(it) }
        }
        return result.map { it.name }
    }

    fun isEmpty(): Boolean {
        return items.isEmpty() && containers.isEmpty()
    }

    fun getAllItems(): List<Item> {
        val containerQueue: Queue<Container> = LinkedList<Container>()
        val result: MutableMap<String, Item> = HashMap()
        result.putAll(getImmutableItems())
        getImmutableContainers().values.forEach { containerQueue.add(it) }
        while (containerQueue.size != 0) {
            val current = containerQueue.poll()
            result.putAll(current.getImmutableItems())
            current.getImmutableContainers().values.forEach { containerQueue.add(it) }
        }
        return result.values.toList()
    }

    override fun toString(): String {
        return """
            |{
            |"maxCapacity" : "$maxCapacity",
            |"name"="$name",
            |"owner" : "$owner",
            |"containers" : "$containers",
            |"items" : ${items.map { it.value.getItemInfo() }}
            |}
            |
        """.trimMargin()
    }
}