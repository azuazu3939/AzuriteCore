package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.command.GodCommand
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class GodListener : Listener {

    @EventHandler
    fun onDamaged(event: EntityDamageEvent) {
        if (GodCommand.isGod(event.entity.uniqueId)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onDamaged(event: EntityDamageByEntityEvent) {
        if (GodCommand.isGod(event.entity.uniqueId)) {
            event.isCancelled = true
        }
    }
}
