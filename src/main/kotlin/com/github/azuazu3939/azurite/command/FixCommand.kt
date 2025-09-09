package com.github.azuazu3939.azurite.command

import io.lumine.mythic.bukkit.MythicBukkit
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class FixCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        val item = sender.inventory.itemInMainHand
        val mmid = MythicBukkit.inst().itemManager.getMythicTypeFromItem(item)
        if (item.type.isAir || !item.hasItemMeta() || !MythicBukkit.inst().itemManager.isMythicItem(item) || mmid == null) {
            sender.sendMessage(Component.text("§cそのアイテムはFix出来ません。"))
            return true
        }

        if (item.itemMeta.persistentDataContainer.has(NamespacedKey("mythiccrucible", "bag_inventory"))) {
            sender.sendMessage(Component.text("§cそのアイテムはFix出来ません。"))
            return true
        }

        val newItem = MythicBukkit.inst().itemManager.getItemStack(mmid, item.amount)
        sender.inventory.setItemInMainHand(newItem)
        sender.sendMessage(Component.text("§a操作が完了しました。"))
        return true
    }
}
