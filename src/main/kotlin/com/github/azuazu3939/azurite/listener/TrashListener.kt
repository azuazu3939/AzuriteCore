package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.TrashInventory
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

class TrashListener(private val plugin: Azurite) : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        if (event.inventory.holder is TrashInventory) {
            val clicked = event.currentItem
            if (clicked == null || clicked.type == Material.AIR) return
            if (clicked.persistentDataContainer.has(NamespacedKey("az", "trash"))) {

                event.isCancelled = true
                val p = event.whoClicked as Player
                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)

                if (clicked.type == Material.BONE_MEAL) {
                    event.currentItem = TRASH_FIRE

                } else if (clicked.type == Material.CAMPFIRE) {
                    event.currentItem = TRASH_BOX
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
            if (check != null && check.type != Material.CAMPFIRE) {
                inv.setItem(53, null)
                returnItemStacks(player, inv)
                event.player.sendMessage(Component.text("処理の途中だった為、アイテムが返却されました。"))
                return
            }
            val logger = JavaPlugin.getPlugin(Azurite::class.java).logger
            for (item in inv.contents) {
                if (item == null) continue
                if (item.itemMeta.persistentDataContainer.has(NamespacedKey("az", "trash"))) continue
                val mmid = plugin.mythic.itemManager.getMythicTypeFromItem(item)
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

    companion object {
        private fun getTrashBox(): ItemStack {
            val item = ItemStack(Material.BONE_MEAL, 1)
            val meta = item.itemMeta!!
            meta.displayName(Component.text("§c§lゴミを捨てる"))
            meta.persistentDataContainer.set(NamespacedKey("az", "trash"), PersistentDataType.STRING, "true")
            item.itemMeta = meta

            return item
        }

        private fun getTrashFire(): ItemStack {
            val item = ItemStack(Material.CAMPFIRE, 1)
            val meta = item.itemMeta!!
            meta.displayName(Component.text("§c§lゴミを捨てる"))
            meta.persistentDataContainer.set(NamespacedKey("az", "trash"), PersistentDataType.STRING, "true")
            item.itemMeta = meta

            return item
        }

        val TRASH_BOX = getTrashBox()

        val TRASH_FIRE = getTrashFire()
    }
}
