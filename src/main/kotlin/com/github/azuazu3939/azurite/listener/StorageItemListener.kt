package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.util.Util
import com.google.common.collect.HashMultimap
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class StorageItemListener : Listener {

    @EventHandler
    fun onSwap(e: PlayerSwapHandItemsEvent) {
        val item = e.mainHandItem
        if (item.itemMeta?.persistentDataContainer?.has(NamespacedKey.minecraft("no_drop")) == true) {
            e.isCancelled = true
            return
        }
        val off = e.offHandItem
        if (off.itemMeta?.persistentDataContainer?.has(NamespacedKey.minecraft("no_drop")) == true) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        player.inventory.contents
        Azurite.runLater(runnable = {
            player.inventory.filter {
                it != null && it.itemMeta?.persistentDataContainer?.has(NamespacedKey.minecraft("no_drop")) == true
            }.forEach {
                player.inventory.removeItem(it)
            }
            player.inventory.setItem(8, getBoxItemStack())
        }, 40)
    }

    private fun getBoxItemStack(): ItemStack {
        val item = ItemStack(Material.OAK_DOOR, 1)
        val meta = item.itemMeta
        meta.displayName(Component.text("§f§lメニューGUI"))
        meta.persistentDataContainer.set(NamespacedKey.minecraft("no_drop"), PersistentDataType.BOOLEAN, true)
        meta.lore(
            mutableListOf(Component.text("§fクリックで開きます。カーソル移動でキャンセル。"))
        )
        item.itemMeta = meta
        return item
    }

    @EventHandler(ignoreCancelled = true)
    fun onClick(e: InventoryClickEvent) {
        val item = e.currentItem
        if (item?.itemMeta?.persistentDataContainer?.has(NamespacedKey.minecraft("no_drop")) == true) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onInteract(e: PlayerInteractEvent) {
        if (e.item?.itemMeta?.persistentDataContainer?.has(NamespacedKey.minecraft("no_drop")) == true && e.action.isRightClick) {
            e.isCancelled = true
            openWindow(e.player)
        }
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        val chest = e.player.inventory.getItem(8)
        if (chest != null) {
            if (chest.itemMeta?.persistentDataContainer?.has(NamespacedKey.minecraft("no_drop")) == true)
                chest.amount = 0
        }
    }

    @EventHandler
    fun onRespawn(e: PlayerRespawnEvent) {
        e.player.inventory.setItem(8, getBoxItemStack())
    }

    private fun openWindow(e: Player) {
        if (Util.isCooldown(javaClass, e.uniqueId, multimap)) return
        Util.setCooldown(javaClass, e.uniqueId, multimap, 30)

        e.playSound(e, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
        Azurite.runLater(runnable = {
            e.playSound(e, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f)
        }, 5)
        Azurite.runLater(runnable = {
            if (e.inventory.heldItemSlot == 8) {
                e.closeInventory()
                e.playSound(e, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0.5f)
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pa open menu " + e.name)
            } else {
                e.playSound(e, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f)
            }
        }, 10)
    }
     companion object {
         private val multimap = HashMultimap.create<Class<*>, UUID>()
     }
}
