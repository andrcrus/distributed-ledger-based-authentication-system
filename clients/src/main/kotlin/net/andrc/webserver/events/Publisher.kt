package net.andrc.webserver.events

import net.andrc.items.Events
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class Publisher(private val applicationEventPublisher: ApplicationEventPublisher) {

    fun publish(eventName: String) : String {
        if (eventName.equals(Events.Shaking.toString(), true)) {
            shakingPublish()
            return "Event ${eventName.toUpperCase()} is published!"
        }
        if (eventName.equals(Events.StrongShaking.toString(), true)) {
            strongShakingPublish()
            return "Event ${eventName.toUpperCase()} is published!"
        }
        if (eventName.equals(Events.StopShaking.toString(), true)) {
            stopShakingPublish()
            return "Event ${eventName.toUpperCase()} is published!"
        }
        if (eventName.equals(Events.StopStrongShaking.toString(), true)) {
            stopStrongShakingPublish()
            return "Event ${eventName.toUpperCase()} is published!"
        }
        if (eventName.equals(Events.TurnOffTheFrige.toString(), true)) {
            turnOffTheFridgePublish()
            return "Event ${eventName.toUpperCase()} is published!"
        }
        if (eventName.equals(Events.TurnOnTheFrige.toString(), true)) {
            turnOnTheFridgePublish()
            return "Event ${eventName.toUpperCase()} is published!"
        }
        throw IllegalArgumentException("Event with name: $eventName does not exists.")
    }

    private fun strongShakingPublish() {
        val strongShakingEvent = StrongShaking(this)
        applicationEventPublisher.publishEvent(strongShakingEvent)
    }

    private fun shakingPublish() {
        val shakingEvent = Shaking(this)
        applicationEventPublisher.publishEvent(shakingEvent)
    }

    private fun stopStrongShakingPublish() {
        val stopStrongShakingEvent = StopStrongShaking(this)
        applicationEventPublisher.publishEvent(stopStrongShakingEvent)
    }

    private fun stopShakingPublish() {
        val stopShakingEvent = StopShaking(this)
        applicationEventPublisher.publishEvent(stopShakingEvent)
    }

    private fun turnOffTheFridgePublish() {
        val turnOffTheFrige = TurnOffTheFrige(this)
        applicationEventPublisher.publishEvent(turnOffTheFrige)
    }

    private fun turnOnTheFridgePublish() {
        val turnOnTheFrige = TurnOnTheFrige(this)
        applicationEventPublisher.publishEvent(turnOnTheFrige)
    }
}