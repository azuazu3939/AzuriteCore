package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent
import io.lumine.mythic.core.mobs.ActiveMob
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.math.pow

class MythicHealthListener(plugin: Azurite) : Listener {

    // 🔥 Bossだけ保持
    private val bossMobs: MutableSet<UUID> = HashSet()

    private val scaledKey = NamespacedKey(plugin, "scaled_players")

    init {
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {

            if (bossMobs.isEmpty()) return@Runnable

            val mobManager = plugin.mythic.mobManager

            val iterator = bossMobs.iterator()
            while (iterator.hasNext()) {
                val uuid = iterator.next()
                val mob = mobManager.getActiveMob(uuid).orElse(null)

                if (mob == null || mob.isDead || !mob.hasThreatTable()) {
                    iterator.remove()
                    continue
                }

                updateHealth(mob)
            }

        }, 20L, 20L)
    }

    // 🔥 Boss登録
    @EventHandler(priority = EventPriority.MONITOR)
    fun onSpawn(event: MythicMobSpawnEvent) {
        val mob = event.mob

        if (!mob.type.internalName.contains("Boss", ignoreCase = true)) return

        bossMobs.add(mob.uniqueId)
    }

    // 🔥 死亡時削除
    @EventHandler(priority = EventPriority.MONITOR)
    fun onDeath(event: MythicMobDeathEvent) {
        bossMobs.remove(event.mob.uniqueId)
    }

    private fun updateHealth(mob: ActiveMob) {

        val ab = mob.entity
        val bukkit = ab.bukkitEntity
        val container = bukkit.persistentDataContainer

        val players = getNearbyPlayers(mob)
        val currentCount = players.size

        val oldCount = container.get(scaledKey, PersistentDataType.INTEGER) ?: -1
        if (oldCount == currentCount) return

        if (currentCount <= 0) {
            container.set(scaledKey, PersistentDataType.INTEGER, 0)
            return
        }

        val baseHealth = mob.type.health.get()
        val newMax = baseHealth * getHealthPerPlayer(currentCount)

        val oldMax = ab.maxHealth
        if (oldMax <= 0) return

        val now = ab.health
        val scale = newMax / oldMax
        val newHealth = (now * scale).coerceAtLeast(1.0).coerceAtMost(newMax)

        ab.maxHealth = newMax
        ab.health = newHealth

        container.set(scaledKey, PersistentDataType.INTEGER, currentCount)
    }

    private fun getNearbyPlayers(mob: ActiveMob): Set<Player> {

        val ab = mob.entity
        val loc = ab.bukkitEntity.location
        val world = loc.world

        return mob.threatTable.allThreatTargets.asSequence()
            .filter { it != null && it.isPlayer }
            .map { BukkitAdapter.adapt(it.asPlayer()) }
            .filter { it.world == world }
            .filter { it.gameMode == GameMode.SURVIVAL || it.gameMode == GameMode.ADVENTURE }
            .filter { loc.distanceSquared(it.location) <= 64 * 64 }
            .toSet()
    }

    private fun getHealthPerPlayer(n: Int): Double {
        if (n <= 1) return 1.0

        val exponent = 1.125 // 🔥 ここを調整可能にしてもOK
        return n.toDouble().pow(exponent)
    }
}