package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.util.DPS
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class DPSListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDamaged(event: MythicDamageEvent) {
        if (event.target.isPlayer) return
        val p = event.caster.entity
        if (!p.isPlayer) return
        if (!DPS.isOn(p.uniqueId)) return
        val d = event.damage * DamageCalculationListener.damageResistance(event.target.bukkitEntity)
        DPS.add(p.uniqueId, event.target.uniqueId, d)
        DPS.show(BukkitAdapter.adapt(p) as Player, DPS.getDamage(p.uniqueId, event.target.uniqueId))
    }
}
