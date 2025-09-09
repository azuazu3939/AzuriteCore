package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.event.ManaModifiedEvent
import com.github.azuazu3939.azurite.event.ManaModifyEvent
import com.github.azuazu3939.azurite.mana.Mana
import com.github.azuazu3939.azurite.mana.ManaRegen
import com.github.azuazu3939.azurite.util.PluginDispatchers.runTask
import org.bukkit.Bukkit.createBossBar
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.KeyedBossBar
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Suppress("RedundantSuspendModifier")
class ManaListener(private val plugin: Azurite) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        removeBossBar(player.uniqueId)

        Azurite.runLater(runnable = {
            removeBossBar(player.uniqueId)
            ManaRegen(player).start()
        }, 20L)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        Azurite.runLater(runnable = {
            ManaRegen(player).stop()
            removeLastUsedMana(player.uniqueId)
        }, 20L)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onManaModified(event: ManaModifiedEvent) {
        val player = event.getPlayer()
        val uuid = player.uniqueId
        if (bossBarMap.containsKey(uuid)) {
            updateBossBar(player)
        } else {
            createBossBar(player)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onManaModify(event: ManaModifyEvent) {
        val player = event.getPlayer()
        val uuid = player.uniqueId
        if (event.getAdd() - event.getBefore() < 0) {
            removeLastUsedMana(uuid)
            return
        }

        if (lastUsedManaMap.containsKey(uuid)) return
        lastUsedManaMap[uuid] = 0
        cancelManaRegenSet.add(uuid)
        lastUsedManaCheck(player, uuid)
    }

    private suspend fun lastUsedManaCheck(player: Player, uuid: UUID) {
        plugin.runTask {
            val b = async {
                if (!lastUsedManaMap.containsKey(uuid) || !cancelManaRegenSet.contains(uuid)) {
                    false
                } else if (!player.isOnline) {
                    removeLastUsedMana(uuid)
                    false
                } else {
                    lastUsedManaMap.merge(uuid, 1, Int::plus)
                    lastUsedManaRegen(player, uuid)
                    true
                }
            }
            if (b) {
                lastUsedManaCheck(player, uuid)
            }
        }
    }

    private fun lastUsedManaRegen(player: Player, uuid: UUID) {
        if (!lastUsedManaMap.containsKey(uuid)) return
        if (lastUsedManaMap[uuid]!! < 40) return

        ManaRegen(player).regen(0.05)
    }

    private fun updateBossBar(player: Player) {
        val uuid = player.uniqueId
        val bar = bossBarMap[uuid]
        if (bar != null) {
            bar.setTitle(createBossBarTitle(player))
            bar.progress = getProgress(player)
            bar.isVisible = true
            visibleCountMap.merge(uuid, 3, Int::plus)
        }
    }

    private fun getProgress(player: Player): Double {
        val mana = Mana(player)
        return mana.getMana() / mana.getMaxMana()
    }

    private fun createBossBar(player: Player) {
        val uuid = player.uniqueId
        val key = NamespacedKey("az", uuid.toString().lowercase())
        val bossBar = createBossBar(key, createBossBarTitle(player), BarColor.BLUE, BarStyle.SEGMENTED_10)
        barFlags(bossBar, player)
        bossBarMap[uuid] = bossBar
        visibleCountMap[uuid] = 3

    }

    private fun barFlags(keyed: KeyedBossBar, player: Player) {
        keyed.progress = getProgress(player)
        keyed.isVisible = true
        keyed.addPlayer(player)
    }

    private fun createBossBarTitle(player: Player): String {
        val mana = ManaRegen(player)
        val num = NumberFormat.getInstance()
        num.maximumFractionDigits = 2
        return "§b§lマナ §f§l" + num.format(mana.getMana()) + "§f /§f§l " + num.format(mana.getMaxMana())
    }

    private fun subtractVisibleCount(uuid: UUID) {
        Azurite.runAsyncLater(runnable = {
            decrementVisibleCount(uuid)
            ensureVisibleCountWithinLimits(uuid)
            handleBossBarVisibility(uuid)
        }, INITIAL_DELAY)
    }

    private fun decrementVisibleCount(uuid: UUID) {
        visibleCountMap.merge(uuid, 1, Int::minus)
    }

    private fun ensureVisibleCountWithinLimits(uuid: UUID) {
        visibleCountMap.computeIfPresent(uuid) { _: UUID, count: Int -> count.coerceAtMost(MAX_VISIBLE_THRESHOLD) }
    }

    private fun handleBossBarVisibility(uuid: UUID) {
        if (getVisibleCount(uuid) <= 0) {
            removeBossBar(uuid)
        } else {
            rescheduleVisibilityCheck(uuid)
        }
    }


    private fun getVisibleCount(uuid: UUID): Int {
        return visibleCountMap.getOrDefault(uuid, 0)
    }

    private fun rescheduleVisibilityCheck(uuid: UUID) {
        if (getVisibleCount(uuid) > MAX_VISIBLE_THRESHOLD) {
            visibleCountMap[uuid] = MAX_VISIBLE_THRESHOLD
        }
        subtractVisibleCount(uuid)
    }

    companion object {
        private val bossBarMap = ConcurrentHashMap<UUID, KeyedBossBar>()
        private val visibleCountMap = ConcurrentHashMap<UUID, Int>()
        private val lastUsedManaMap = ConcurrentHashMap<UUID, Int>()
        private val cancelManaRegenSet = ConcurrentHashMap.newKeySet<UUID>()
        private const val MAX_VISIBLE_THRESHOLD = 5
        private const val INITIAL_DELAY = 20L

        fun removeLastUsedMana(uuid: UUID) {
            lastUsedManaMap.remove(uuid)
            cancelManaRegenSet.remove(uuid)
        }

        fun removeBossBar(uuid: UUID) {
            visibleCountMap.remove(uuid)
            if (bossBarMap.containsKey(uuid)) {
                val bar = bossBarMap[uuid]
                if (bar != null) {
                    bar.isVisible = false
                    bar.removeAll()
                }
                bossBarMap.remove(uuid)
            }
        }

        fun removeAll() {
            bossBarMap.values.forEach { bar ->
                bar.isVisible = false
                bar.removeAll()
            }
            bossBarMap.clear()
            visibleCountMap.clear()
        }
    }
}
