package com.github.azuazu3939.azurite

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class TrashInventory : InventoryHolder {

    override fun getInventory(): Inventory {
        val inv = Bukkit.createInventory(this, 54, Component.text("§b§lゴミ箱"))
        val item = ItemStack(Material.RED_TERRACOTTA, 1)
        val meta = item.itemMeta!!
        meta.displayName(Component.text("§c§lゴミを捨てる"))
        meta.persistentDataContainer.set(NamespacedKey("az", "trash"), PersistentDataType.STRING, "true")
        item.itemMeta = meta
        inv.setItem(53, item)
        return inv
    }
}
