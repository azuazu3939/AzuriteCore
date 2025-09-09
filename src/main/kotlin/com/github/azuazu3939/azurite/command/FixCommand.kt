package com.github.azuazu3939.azurite.command

import io.lumine.mythic.bukkit.MythicBukkit
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class FixCommand : CommandExecutor {

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p0 !is Player) return false
        val item = p0.inventory.itemInMainHand
        val mmid = MythicBukkit.inst().itemManager.getMythicTypeFromItem(item)
        if (item.type.isAir || !item.hasItemMeta() || !MythicBukkit.inst().itemManager.isMythicItem(item) || mmid == null) {
            p0.sendMessage(Component.text("§cそのアイテムはFix出来ません。"))
            return true
        }

        if (item.itemMeta.persistentDataContainer.has(NamespacedKey("mythiccrucible", "bag_inventory"))) {
            p0.sendMessage(Component.text("§cそのアイテムはFix出来ません。"))
            return true
        }

        val newItem = MythicBukkit.inst().itemManager.getItemStack(mmid, item.amount)
        p0.inventory.setItemInMainHand(newItem)
        p0.sendMessage(Component.text("§a操作が完了しました。"))
        return true
    }
}
