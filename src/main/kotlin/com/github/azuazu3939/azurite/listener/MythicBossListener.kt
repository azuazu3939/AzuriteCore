package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.util.PluginDispatchers.runTask
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Suppress("RedundantSuspendModifier")
class MythicBossListener(private val plugin: Azurite) : Listener {

    private val data: ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, Double>> = ConcurrentHashMap()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    suspend fun onAdd(event: MythicMobSpawnEvent) {
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

        //タンク報酬
        val tank = target.target
        if (!tank.isPlayer) return
        val ac = MythicBukkit.inst().apiHelper.getMythicMobInstance(target.bukkitEntity)
        if (!ac.hasThreatTable()) return

        val table = ac.threatTable
        var count = table.allThreatTargets.count { target -> target.isPlayer }
        if (count <= 0) return

        count++
        data.computeIfAbsent(target.uniqueId) { ConcurrentHashMap() }.merge(tank.uniqueId, damage / count, Double::plus)
    }

    @EventHandler
    suspend fun onDeath(event: MythicMobDeathEvent) {
        val target = event.mob.entity
        if (!isRankAble(target)) return

        val playerDamageMap = data.remove(target.uniqueId) ?: return
        val sortedPlayers = playerDamageMap.toList().sortedByDescending { it.second }
        val ps = event.killer.location.getNearbyPlayers(48.0)
        val mmid = event.mob.type.internalName

        plugin.runTask {
            async {
                val messages = mutableListOf<Component>()
                messages.add(Component.text("§f§l[討伐戦] ${event.mob.name}§r §6§lランキング"))

                sortedPlayers.take(10).forEachIndexed { index, (playerUUID, damage) ->
                    val player = Bukkit.getPlayer(playerUUID)
                    if (player != null) {
                        val rank = index + 1
                        val playerName = player.name
                        val message = "§a§l${rank}位 §6§l${playerName} §f§lスコア §b§l${damage.toLong()}"
                        messages.add(Component.text(message))

                        MythicBukkit.inst().apiHelper.castSkill(player, mmid + "_HighDrop")
                    }
                }

                delayTick(20)
                sync {
                    ps.forEach { p ->
                        messages.forEach { msg ->
                            p.sendMessage(msg)
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    fun onDespawn(event: MythicMobDespawnEvent) {
        val target = event.mob.entity
        if (!isRankAble(target)) return
        data.remove(target.uniqueId)
    }

    private fun isRankAble(abstractEntity: AbstractEntity): Boolean {
        val ac = MythicBukkit.inst().mobManager.getActiveMob(abstractEntity.uniqueId).orElse(null)
        return ac != null && ac.hasThreatTable() && abstractEntity.dataContainer.has(NamespacedKey("az", "ranking"), PersistentDataType.STRING)
    }
}
