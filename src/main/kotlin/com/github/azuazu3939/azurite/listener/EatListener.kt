package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.mana.ManaRegen
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.*
import java.util.*

class EatListener : Listener {
    companion object {
        private val countMap = HashMap<UUID, Int>()
    }

    @EventHandler
    fun onRegain(event: EntityRegainHealthEvent) {
        val entity = event.entity
        if (entity !is Player) return

        val uuid = entity.uniqueId
        if (countMap.getOrDefault(uuid, 0) >= 10) return

        when (event.regainReason) {
            SATIATED -> {
                ManaRegen(entity).regen(0.05)
                event.amount += entity.getAttribute(Attribute.MAX_HEALTH)!!.value * 0.05
                healCounter(uuid, 1)
            }
            EATING -> {
                event.amount *= 1.25
                healCounter(uuid, 1)
            }
            MAGIC_REGEN -> {
                event.amount *= 1.5
                healCounter(uuid, 1)
            }
            MAGIC -> {
                event.amount *= 1.5
                healCounter(uuid, 2)
            }
            CUSTOM -> {
                event.amount *= 1.5
                healCounter(uuid, 2)
            }
            else -> return
        }
    }

    private fun healCounter(uuid: UUID, value: Int) {
        countMap.merge(uuid, value, Int::plus)
        Azurite.runAsyncLater(runnable = { countMap.merge(uuid, 1, Int::minus) }, 600)
    }
}
