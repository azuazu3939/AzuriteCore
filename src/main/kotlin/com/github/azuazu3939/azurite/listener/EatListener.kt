package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import dev.aurelium.auraskills.api.AuraSkillsApi
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
        if (countMap.getOrDefault(uuid, 0) >= 15) return

        when (event.regainReason) {
            SATIATED -> {
                val user = AuraSkillsApi.get().getUser(uuid)
                user.mana = (user.maxMana * 0.05 + user.mana).coerceAtMost(user.maxMana)
                event.amount += entity.getAttribute(Attribute.MAX_HEALTH)!!.value * 0.05
                healCounter(uuid)
            }
            else -> return
        }
    }

    private fun healCounter(uuid: UUID) {
        countMap.merge(uuid, 1, Int::plus)
        Azurite.runAsyncLater(runnable = { countMap.merge(uuid, 1, Int::minus) }, 600)
    }
}
