package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.TrashInventory
import io.lumine.mythic.bukkit.MythicBukkit
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class TrashListener : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        if (event.inventory.holder is TrashInventory) {
            val clicked = event.currentItem
            if (clicked == null || clicked.type == Material.AIR) return
            clicked.serializeAsBytes()
            if (clicked.persistentDataContainer.has(NamespacedKey("az", "trash"))) {

                event.isCancelled = true
                val p = event.whoClicked as Player
                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)

                if (clicked.type == Material.RED_TERRACOTTA) {
                    val newItem = ItemStack(Material.GREEN_TERRACOTTA, 1)
                    newItem.setItemMeta(clicked.itemMeta)
                    event.currentItem = newItem

                } else if (clicked.type == Material.GREEN_TERRACOTTA) {
                    val newItem = ItemStack(Material.RED_TERRACOTTA, 1)
                    newItem.setItemMeta(clicked.itemMeta)
                    event.currentItem = newItem
                }
            }
        }
    }

    @EventHandler
    fun onDrag(event: InventoryDragEvent) {
        if (event.inventory.holder is TrashInventory) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        val inv = event.inventory
        val player = event.player as Player
        if (inv.holder is TrashInventory) {
            val check = inv.getItem(53)
            if (check != null && check.type != Material.GREEN_TERRACOTTA) {
                inv.setItem(53, null)
                returnItemStacks(player, inv)
                event.player.sendMessage(Component.text("処理の途中だった為、アイテムが返却されました。"))
                return
            }
            val logger = JavaPlugin.getPlugin(Azurite::class.java).logger
            for (item in inv.contents) {
                if (item == null) continue
                if (item.itemMeta.persistentDataContainer.has(NamespacedKey("az", "trash"))) continue
                val mmid = MythicBukkit.inst().itemManager.getMythicTypeFromItem(item)
                if (mmid != null) {
                    logger.info(
                        player.name + "がアイテムを捨てました。 mmid : " + mmid + " ×" + item.amount
                    )
                } else {
                    logger.info(
                        player.name + "がアイテムを捨てました。 type : " + item.type + " ×" + item.amount + " Data : " + item
                    )
                }
            }
        }
    }

    private fun returnItemStacks(player: Player, inv: Inventory) {
        inv.contents.forEach {
            if (it == null) return@forEach
            player.inventory.addItem(it).forEach { drop ->
                val dropStack = drop.value
                val meta = dropStack.itemMeta
                meta.persistentDataContainer.set(NamespacedKey("az", "player_dropped"), PersistentDataType.STRING, player.uniqueId.toString())
                dropStack.itemMeta = meta
                player.world.dropItem(player.location, dropStack)
            }
        }
    }
}
