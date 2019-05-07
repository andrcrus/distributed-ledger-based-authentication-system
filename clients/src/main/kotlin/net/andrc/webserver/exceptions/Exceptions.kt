package net.andrc.webserver.exceptions

/**
 * @author andrey.makhnov
 */
class OutOfContainerCapacityException(message: String?) : Exception(message)

class UnknownContainerException(message: String?) : Exception(message)