package net.andrc.webserver.events

import net.andrc.items.ItemProperties
import net.andrc.webserver.services.CordaDialogService
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component


@Component
class Listener(private val cordaDialogService: CordaDialogService) : ApplicationListener<CarrierEvent> {

    override fun onApplicationEvent(event: CarrierEvent) {
        when (event) {
            is StrongShaking -> {
                cordaDialogService.carrierEvent(event.event, listOf(ItemProperties.FRAGILE, ItemProperties.BEATING))
            }

            is StopStrongShaking -> {
                cordaDialogService.carrierEvent(event.event, listOf(ItemProperties.FRAGILE, ItemProperties.BEATING))
            }

            is Shaking -> {
                cordaDialogService.carrierEvent(event.event, listOf(ItemProperties.FRAGILE))
            }

            is StopShaking -> {
                cordaDialogService.carrierEvent(event.event, listOf(ItemProperties.FRAGILE))
            }

            is TurnOffTheFrige -> {
                cordaDialogService.carrierEvent(event.event, listOf(ItemProperties.FROZEN))
            }

            is TurnOnTheFrige -> {
                cordaDialogService.carrierEvent(event.event, listOf(ItemProperties.FROZEN))
            }
        }
    }
}