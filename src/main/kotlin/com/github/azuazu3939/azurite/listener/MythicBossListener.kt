package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
import java.util.*

class MythicBossListener(plugin: Azurite) : Listener {

    /* ================= キャッシュ ================= */

    private val rankingKey = NamespacedKey(plugin, "ranking")
    private val mobManager = plugin.mythic.mobManager
    private val apiHelper = plugin.mythic.apiHelper

    // ボスUUID -> (プレイヤーUUID -> 累積ダメージ)
    private val damageMap = HashMap<UUID, MutableMap<UUID, Double>>(64)

    /* ================= DAMAGE ================= */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDamage(event: MythicDamageEvent) {

        val target = event.target
        val active = mobManager.getActiveMob(target.uniqueId).orElse(null) ?: return
        if (!active.hasThreatTable()) return
        if (!target.dataContainer.has(rankingKey, PersistentDataType.STRING)) return

        val caster = event.caster.entity
        if (!caster.isPlayer) return

        val playerUUID = caster.uniqueId
        val damage = event.damage
        val bossUUID = target.uniqueId

        val bossMap = damageMap.getOrPut(bossUUID) { HashMap(32) }
        bossMap[playerUUID] = (bossMap[playerUUID] ?: 0.0) + damage

        /* ======== Threat最適化処理 ======== */

        val tank = target.target ?: return
        if (!tank.isPlayer) return

        val mobInstance = apiHelper.getMythicMobInstance(target.bukkitEntity)
        if (!mobInstance.hasThreatTable()) return

        val threatTargets = mobInstance.threatTable.allThreatTargets
        var playerCount = 0

        for (t in threatTargets) {
            if (t?.isPlayer == true) playerCount++
        }

        if (playerCount <= 0) return

        val share = damage / (playerCount + 1)
        val tankUUID = tank.uniqueId
        bossMap[tankUUID] = (bossMap[tankUUID] ?: 0.0) + share
    }

    /* ================= DEATH ================= */

    @EventHandler
    fun onDeath(event: MythicMobDeathEvent) {

        val boss = event.mob.entity
        val active = mobManager.getActiveMob(boss.uniqueId).orElse(null) ?: return
        if (!active.hasThreatTable()) return
        if (!boss.dataContainer.has(rankingKey, PersistentDataType.STRING)) return

        val map = damageMap.remove(boss.uniqueId) ?: return
        if (map.isEmpty()) return

        val top = getTop10Heap(map)
        val viewers = event.killer?.location?.getNearbyPlayers(48.0) ?: return
        val mmid = event.mob.type.internalName

        val messages = ArrayList<Component>(top.size + 1)
        messages.add(Component.text("§f§l[討伐戦] ${event.mob.name} §6§lランキング"))

        var rank = 1
        for ((uuid, dmg) in top) {
            val p = Bukkit.getPlayer(uuid) ?: continue
            messages.add(
                Component.text("§a§l${rank}位 §6§l${p.name} §f§lスコア §b§l${dmg.toLong()}")
            )
            apiHelper.castSkill(p, mmid + "_HighDrop")
            rank++
        }

        for (viewer in viewers) {
            for (msg in messages) {
                viewer.sendMessage(msg)
            }
        }
    }

    /* ================= DESPAWN ================= */

    @EventHandler
    fun onDespawn(event: MythicMobDespawnEvent) {
        val boss = event.mob.entity
        damageMap.remove(boss.uniqueId)
    }

    /* ================= HEAP ================= */

    private fun getTop10Heap(map: Map<UUID, Double>): List<Pair<UUID, Double>> {

        val heap = PriorityQueue<Map.Entry<UUID, Double>>(10) { a, b ->
            a.value.compareTo(b.value)
        }

        for (entry in map.entries) {
            if (heap.size < 10) {
                heap.offer(entry)
            } else if (entry.value > heap.peek().value) {
                heap.poll()
                heap.offer(entry)
            }
        }

        return heap.sortedByDescending { it.value }
            .map { it.key to it.value }
    }
}