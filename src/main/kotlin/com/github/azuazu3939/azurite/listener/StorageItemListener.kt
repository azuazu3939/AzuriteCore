package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.util.Util
import com.google.common.collect.HashMultimap
import net.kyori.adventure.text.Component
import net.minecraft.world.item.component.CustomModelData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.craftbukkit.inventory.components.CraftCustomModelDataComponent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

@Suppress("UnstableApiUsage")
class StorageItemListener(private val plugin: Azurite) : Listener {

    companion object {
        private val KEY_NO_DROP = NamespacedKey.minecraft("no_drop")
        private val COOLDOWN_MAP = HashMultimap.create<Class<*>, UUID>()

        // 生成コスト削減のためキャッシュ
        private val MENU_ITEM: ItemStack = ItemStack(Material.OAK_DOOR).apply {
            val meta = itemMeta!!
            meta.displayName(Component.text("§f§lメニューGUI"))
            meta.lore(listOf(Component.text("§fクリックで開きます。カーソル移動でキャンセル。")))
            meta.persistentDataContainer.set(KEY_NO_DROP, PersistentDataType.BOOLEAN, true)
            meta.setCustomModelDataComponent(CraftCustomModelDataComponent(CustomModelData(listOf(100000.toFloat()), emptyList(), emptyList(), emptyList())))
            itemMeta = meta
        }
    }

    /* -------------------------
       Utility
     ------------------------- */

    private fun ItemStack?.isMenuItem(): Boolean {
        return this?.itemMeta
            ?.persistentDataContainer
            ?.has(KEY_NO_DROP) == true
    }

    /* -------------------------
       Events
     ------------------------- */

    @EventHandler
    fun onSwap(e: PlayerSwapHandItemsEvent) {
        if (e.mainHandItem.isMenuItem() || e.offHandItem.isMenuItem()) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {

            // ストリーム生成を避ける
            val inv = player.inventory
            for (i in 0 until inv.size) {
                if (inv.getItem(i).isMenuItem()) {
                    inv.setItem(i, null)
                }
            }

            inv.setItem(8, MENU_ITEM.clone()) // cloneで安全に

        }, 30L)
    }

    @EventHandler(ignoreCancelled = true)
    fun onClick(e: InventoryClickEvent) {
        if (e.currentItem.isMenuItem()) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        val item = e.item
        if (!item.isMenuItem() || !e.action.isRightClick) return

        e.isCancelled = true
        openWindow(e.player)
    }

    @EventHandler
    fun onDrop(e: PlayerDropItemEvent) {
        if (e.itemDrop.itemStack.isMenuItem()) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val inv = e.player.inventory
        if (inv.getItem(8).isMenuItem()) {
            inv.setItem(8, null)
        }
    }

    @EventHandler
    fun onRespawn(e: PlayerRespawnEvent) {
        e.player.inventory.setItem(8, MENU_ITEM.clone())
    }

    /* -------------------------
       Window Logic
     ------------------------- */

    private fun openWindow(player: Player) {

        // クールダウン
        if (Util.isCooldown(javaClass, player.uniqueId, COOLDOWN_MAP)) return
        Util.setCooldown(javaClass, player.uniqueId, COOLDOWN_MAP, 30)

        // 先にスロット確認（無駄な処理削減）
        if (player.inventory.heldItemSlot != 8) {
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f)
            return
        }

        // 軽量サウンド処理
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
        }, 5L)

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            player.closeInventory()
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0.5f)
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                "pa open menu ${player.name}"
            )
        }, 10L)
    }
}