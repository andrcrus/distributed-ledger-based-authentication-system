package net.andrc.items

import net.corda.core.identity.Party
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

/**
 * @author andrey.makhnov
 */
class RootContainer(maxCapacity: Long, name: String, owner: Party) : Container(maxCapacity, name, owner) {
    override val containers: MutableMap<String, Container> = ConcurrentHashMap()
    override val items: MutableMap<String, Item> = ConcurrentHashMap()

    fun getAllItems(): Map<String, Item> {
        val containerQueue: Queue<Container> = LinkedList<Container>()
        val result: MutableMap<String, Item> = HashMap()
        result.putAll(getImmutableItems())
        getImmutableContainers().values.forEach { containerQueue.add(it) }
        while (containerQueue.size != 0) {
            val current = containerQueue.poll()
            result.putAll(current.getImmutableItems())
            current.getImmutableContainers().values.forEach { containerQueue.add(it) }
        }
        return result.toMap()
    }

}
