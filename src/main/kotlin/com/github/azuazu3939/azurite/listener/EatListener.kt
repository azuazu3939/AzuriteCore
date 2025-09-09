package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.mana.ManaRegen
import com.github.azuazu3939.azurite.util.Util
import com.google.common.collect.HashMultimap
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityRegainHealthEvent
import java.util.*

class EatListener : Listener {

    companion object {
        private val multimap = HashMultimap.create<Class<*>, UUID>()
        private val countMap = HashMap<UUID, Int>()
    }

    @EventHandler
    fun onRegain(event: EntityRegainHealthEvent) {
        val entity = event.entity
        if ((entity !is Player)) return

        val uuid = entity.uniqueId
        if (countMap.getOrDefault(uuid, 0) >= 10) return

        if (event.regainReason == EntityRegainHealthEvent.RegainReason.SATIATED) {
            ManaRegen(entity).regen(0.05)
            event.amount *= 0.075
            healCounter(uuid)
        }

        if (event.regainReason == EntityRegainHealthEvent.RegainReason.EATING && !Util.isCooldown(
                javaClass,
                uuid,
                multimap
            )
        ) {
            Util.setCooldown(javaClass, uuid, multimap, 60)
            event.amount *= 0.125
            healCounter(uuid)
        }
    }

    private fun healCounter(uuid: UUID) {
        countMap.merge(uuid, 1, Int::plus)
        Azurite.runAsyncLater(runnable = { countMap.merge(uuid, 1, Int::minus) }, 300)
    }
}
