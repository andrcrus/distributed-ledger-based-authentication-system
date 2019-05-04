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
}
