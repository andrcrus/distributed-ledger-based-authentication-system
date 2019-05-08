package net.andrc.webserver.events

import net.andrc.items.Events
import org.springframework.context.ApplicationEvent


abstract class CarrierEvent(source : Any) : ApplicationEvent(source) {
    abstract val event: Events
}

class StrongShaking(source : Any) : CarrierEvent(source) {
    override val event: Events
        get() = Events.StrongShaking
}

class Shaking(source : Any) : CarrierEvent(source)  {
    override val event: Events
        get() = Events.Shaking
}

class StopStrongShaking(source : Any) : CarrierEvent(source)  {
    override val event: Events
        get() = Events.StopStrongShaking
}

class StopShaking(source : Any) : CarrierEvent(source)  {
    override val event: Events
        get() = Events.StopShaking
}

class TurnOffTheFrige(source : Any) : CarrierEvent(source)  {
    override val event: Events
        get() = Events.TurnOffTheFrige
}

class TurnOnTheFrige(source : Any) : CarrierEvent(source)  {
    override val event: Events
        get() = Events.TurnOnTheFrige
}





