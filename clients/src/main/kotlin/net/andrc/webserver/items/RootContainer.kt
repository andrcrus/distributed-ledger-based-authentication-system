package net.andrc.webserver.items

import java.util.*
import kotlin.collections.HashMap

class RootContainer(maxCapacity: Long, name: String) : Container(maxCapacity, name) {

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
