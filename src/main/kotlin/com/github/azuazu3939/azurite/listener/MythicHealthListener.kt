package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import io.lumine.mythic.core.mobs.ActiveMob
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType

class MythicHealthListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onCombat(event: MythicDamageEvent) {
        val ab = event.target
        if (!isHealthScalingAble(ab)) return
        scheduleHealthUpdate(ab)
    }

    private fun isHealthScalingAble(abstractEntity: AbstractEntity): Boolean {
        val ac = MythicBukkit.inst().mobManager.getActiveMob(abstractEntity.uniqueId).orElse(null)
        return ac != null && ac.hasThreatTable() && abstractEntity.dataContainer.has(NamespacedKey("az", "raid_boss"), PersistentDataType.STRING)
    }

    private fun scheduleHealthUpdate(ab: AbstractEntity) {
        Azurite.runLater(runnable = {
            val mob = MythicBukkit.inst().mobManager.getActiveMob(ab.uniqueId).orElse(null)
            if (mob != null) {
                updateHealth(ab, mob)
            }
        }, 1)
    }

    private fun updateHealth(ab: AbstractEntity, mob: ActiveMob) {
        val players = getNearByPlayers(mob.threatTable, ab)

        val max = mob.type.health.get() * getHealthPerPlayer(players.size)
        val now = ab.health

        if (ab.maxHealth == max) return

        val scale = max / ab.maxHealth
        val set = now * scale
        ab.maxHealth = max
        Azurite.runLater(runnable = { ab.health = set }, 1)
    }

    private fun getNearByPlayers(table: ActiveMob.ThreatTable, ab: AbstractEntity) : Set<Player> {
        return table.allThreatTargets.asSequence()
            .filter { it != null && it.isPlayer }
            .map { BukkitAdapter.adapt(it) as Player }
            .filter { it.world.name.equals(ab.world.name, true) }
            .filter { it.gameMode == GameMode.SURVIVAL || it.gameMode == GameMode.ADVENTURE }
            .filter { ab.bukkitEntity.location.distance(it.location) <= 64 }
            .toSet()
    }

    private fun getHealthPerPlayer(n: Int) : Double {
        return n - 0.25 * (n - 1)
    }
}