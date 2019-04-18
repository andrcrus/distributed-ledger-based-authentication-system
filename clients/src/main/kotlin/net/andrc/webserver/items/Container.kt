package net.andrc.webserver.items

/**
 * @author andrey.makhnov
 *
 *  Container class. Not thread safe.
 */

open class Container(private val maxCapacity: Long, val name: String) {
    var currentCapacity: Long = 0L

    val containers: MutableMap<String, Container> = HashMap()
    val items: MutableMap<String, Item> = HashMap()

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
        items[item.name] = item
        return true
    }

    fun deleteItem(itemName: String) {
        val item = items[itemName] ?: return
        currentCapacity -= item.capacity
        items.remove(itemName)
    }

    fun deleteContainer(containerName: String) {
        val container = containers[containerName] ?: return
        currentCapacity -= container.maxCapacity
        items.remove(containerName)
    }
}