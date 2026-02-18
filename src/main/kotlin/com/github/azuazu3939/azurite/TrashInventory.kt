package com.github.azuazu3939.azurite

import com.github.azuazu3939.azurite.listener.TrashListener
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class TrashInventory : InventoryHolder {

    override fun getInventory(): Inventory {
        val inv = Bukkit.createInventory(this, 54, Component.text("§b§lゴミ箱"))
        inv.setItem(53, TrashListener.TRASH_BOX)
        return inv
    }
}
