package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent
import io.lumine.mythic.core.mobs.ActiveMob
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MythicBossListener : Listener {

    private val data: ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, Double>> = ConcurrentHashMap()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAdd(event: MythicMobSpawnEvent) {
        val mob = event.mob ?: return
        Azurite.runLater(runnable = {
            if (!isRankAble(mob.entity)) return@runLater
            MythicListener.addRank(mob.uniqueId)
        }, 5)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onRemove(event: MythicMobDespawnEvent) {
        val mob = event.mob ?: return
        if (!isRankAble(mob.entity)) return
        MythicListener.removeRank(mob.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onRemove(event: MythicMobDeathEvent) {
        val mob = event.mob ?: return
        if (!isRankAble(mob.entity)) return
        MythicListener.removeRank(mob.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onRanking(event: MythicDamageEvent) {
        val target = event.target
        if (!isRankAble(target)) return
        val caster = event.caster.entity
        if (!caster.isPlayer) return

        val player = caster.bukkitEntity as Player
        val damage = event.damage * DamageCalculationListener.damageResistance(target.bukkitEntity)
        data.computeIfAbsent(target.uniqueId) { ConcurrentHashMap() }.merge(player.uniqueId, damage, Double::plus)
    }

    @EventHandler
    fun onDeath(event: MythicMobDeathEvent) {
        val target = event.mob.entity
        if (!isRankAble(target)) return

        val playerDamageMap = data.remove(target.uniqueId) ?: return
        val sortedPlayers = playerDamageMap.toList().sortedByDescending { it.second }
        val ps = event.killer.location.getNearbyPlayers(48.0)
        val mmid = event.mob.type.internalName

        val messages = mutableListOf<Component>()
        messages.add(Component.text("§f§l[討伐戦] ${event.mob.name}§r §6§lランキング"))

        sortedPlayers.take(10).forEachIndexed { index, (playerUUID, damage) ->
            val player = Bukkit.getPlayer(playerUUID)
            if (player != null) {
                val rank = index + 1
                val playerName = player.name
                val message = "§a§l${rank}位 §6§l${playerName} §f§lスコア §b§l${damage.toLong()}"
                messages.add(Component.text(message))

                for (repeat in 0..10 - rank) {
                    MythicBukkit.inst().apiHelper.castSkill(player, mmid + "_HighDrop")
                }
            }
        }

        Azurite.runLater(runnable = {
            ps.forEach { p ->
                messages.forEach { msg ->
                    p.sendMessage(msg)
                }
            }
        }, 20)
    }

    @EventHandler
    fun onDespawn(event: MythicMobDespawnEvent) {
        val target = event.mob.entity
        if (!isRankAble(target)) return
        data.remove(target.uniqueId)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onCombat(event: MythicDamageEvent) {
        val ab = event.target
        if (!isRaidAble(ab)) return
        scheduleBossHealthUpdate(ab)
    }

    private fun isRaidAble(abstractEntity: AbstractEntity): Boolean {
        val ac = MythicBukkit.inst().mobManager.getActiveMob(abstractEntity.uniqueId).orElse(null)
        return ac != null && ac.hasThreatTable() && abstractEntity.dataContainer.has(NamespacedKey("az", "raid_boss"), PersistentDataType.STRING)
    }

    private fun isRankAble(abstractEntity: AbstractEntity): Boolean {
        val ac = MythicBukkit.inst().mobManager.getActiveMob(abstractEntity.uniqueId).orElse(null)
        return ac != null && ac.hasThreatTable() && abstractEntity.dataContainer.has(NamespacedKey("az", "ranking"), PersistentDataType.STRING)
    }

    private fun scheduleBossHealthUpdate(ab: AbstractEntity) {
        Azurite.runLater(runnable = {
            val mob = MythicBukkit.inst().mobManager.getActiveMob(ab.uniqueId).orElse(null)
            if (mob != null) {
                updateBossHealth(ab, mob)
            }
        }, 1)
    }

    private fun updateBossHealth(ab: AbstractEntity, mob: ActiveMob) {
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
        return n - 0.4 * (n - 1)
    }
}
