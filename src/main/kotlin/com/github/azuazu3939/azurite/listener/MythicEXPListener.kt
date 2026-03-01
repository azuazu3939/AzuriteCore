package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class MythicEXPListener(private val plugin: Azurite) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDamaged(event: MythicDamageEvent) {
        if (event.target.isPlayer) return
        val mm = plugin.mythic.mobManager.getMythicMobInstance(event.target) ?: return
        if (mm.type.internalName.contains("admin", true)) return

        val cast = event.caster.entity
        if (!cast.isPlayer) return
    }
}
