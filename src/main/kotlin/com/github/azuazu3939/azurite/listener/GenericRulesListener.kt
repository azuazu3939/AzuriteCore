package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.command.ModeCommand
import com.github.azuazu3939.azurite.database.DBCon
import com.github.azuazu3939.azurite.mythic.MythicTrigger
import com.github.azuazu3939.azurite.util.PacketUtil
import com.github.azuazu3939.azurite.util.Util
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.events.MythicHealMechanicEvent
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.CrafterCraftEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.*
import org.bukkit.persistence.PersistentDataType

class GenericRulesListener(private val plugin: Azurite) : Listener {

    companion object {
        private val DROP_KEY = NamespacedKey("az", "player_dropped")
        private val STEP_KEY = NamespacedKey("az", "default-step-height")
        private const val FIRST_SPAWN_SKILL = "Azuriter_FirstSpawn_1"
    }

    /* ------------------------------------------------ */
    /* Craft Block */
    /* ------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCraft(event: CraftItemEvent) { event.isCancelled = true }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCraft(event: CrafterCraftEvent) { event.isCancelled = true }

    /* ------------------------------------------------ */
    /* Login / Join */
    /* ------------------------------------------------ */

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPreJoin(event: AsyncPlayerPreLoginEvent) {
        DBCon.loadSpawn(event.uniqueId)

        if (MythicListener.RELOADING || MythicListener.QUEUE) {
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        PacketUtil.inject(player, plugin)

        // 直接メインスレッド実行（余計なrunTask削除）
        handleFirstJoin(player)
        applyPlayerDefaults(player)

        if (player.hasPermission("azurite.command.mode")) {
            ModeCommand.switch(player, egod = false, efly = true, ebypass = false)
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        PacketUtil.eject(event.player)
    }

    /* ------------------------------------------------ */
    /* Item Drop / Pickup */
    /* ------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onDrop(event: PlayerDropItemEvent) {
        val meta = event.itemDrop.itemStack.itemMeta ?: return
        meta.persistentDataContainer.set(
            DROP_KEY,
            PersistentDataType.STRING,
            event.player.uniqueId.toString()
        )
        event.itemDrop.itemStack.itemMeta = meta
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPickup(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        val stack = event.item.itemStack
        val meta = stack.itemMeta ?: return
        val container = meta.persistentDataContainer

        val owner = container.get(DROP_KEY, PersistentDataType.STRING) ?: return
        if (owner != player.uniqueId.toString()) {
            event.isCancelled = true
            return
        }

        container.remove(DROP_KEY)
        stack.itemMeta = meta
    }

    /* ------------------------------------------------ */
    /* World Change */
    /* ------------------------------------------------ */

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        applyPlayerDefaults(event.player)
    }

    /* ------------------------------------------------ */
    /* Heal Trigger */
    /* ------------------------------------------------ */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onMythicHeal(event: MythicHealMechanicEvent) {
        triggerHeal(BukkitAdapter.adapt(event.target))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onVanillaHeal(event: EntityRegainHealthEvent) {
        val player = event.entity as? Player ?: return
        triggerHeal(BukkitAdapter.adapt(player))
    }

    private fun triggerHeal(entity: AbstractEntity) {
        if (MythicListener.RELOADING) return

        val caster = plugin.mythic.skillManager.getCaster(entity)
        MythicTrigger(caster).triggerHeal()
    }

    /* ------------------------------------------------ */
    /* Combat / Spawn Rules */
    /* ------------------------------------------------ */

    @EventHandler(ignoreCancelled = true)
    fun onArrow(event: EntityDamageByEntityEvent) {
        if (event.entity is Player && event.damager is Arrow) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onSpawn(event: CreatureSpawnEvent) {
        if (event.spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            event.isCancelled = true
        }
    }

    /* ------------------------------------------------ */
    /* Player Defaults */
    /* ------------------------------------------------ */

    private fun handleFirstJoin(player: Player) {
        if (DBCon.hasSpawn(player.uniqueId)) return

        DBCon.setSpawn(player.uniqueId)
        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            "spawn ${player.name}"
        )

        // 20秒後スキル実行（Coroutine削除）
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            plugin.mythic.apiHelper.castSkill(player, FIRST_SPAWN_SKILL)
        }, 20L * 20)
    }

    private fun applyPlayerDefaults(player: Player) {
        Util.removeAttribute(player, Attribute.STEP_HEIGHT, STEP_KEY)

        Util.addAttribute(
            player,
            Attribute.STEP_HEIGHT,
            AttributeModifier(
                STEP_KEY,
                0.5,
                AttributeModifier.Operation.ADD_NUMBER
            )
        )

        player.getAttribute(Attribute.WAYPOINT_TRANSMIT_RANGE)?.baseValue = 0.0
    }
}