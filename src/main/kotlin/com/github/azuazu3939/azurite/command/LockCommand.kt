package com.github.azuazu3939.azurite.command

import io.lumine.mythic.bukkit.MythicBukkit
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class LockCommand : CommandExecutor {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return false
        val item = sender.inventory.itemInMainHand
        if (item.type == Material.AIR) {
            sender.sendMessage(Component.text("§cメインハンドにアイテムを持つ必要があります。"))
            return true
        }

        if (!MythicBukkit.inst().itemManager.isMythicItem(item)) {
            sender.sendMessage(Component.text("§cロックするアイテムはMMIDが存在する必要があります。"))
            return true
        }

        val key = NamespacedKey("azurite", "lock")

        val meta = item.itemMeta!!
        if (meta.persistentDataContainer.has(key)) {
            meta.persistentDataContainer.remove(key)
            sender.sendMessage(Component.text("§aアイテムのロックを解除しました。"))
        } else {
            meta.persistentDataContainer.set(key, PersistentDataType.BOOLEAN, true)
            sender.sendMessage(Component.text("§aアイテムをロックしました。"))
        }
        item.itemMeta = meta
        sender.sendMessage(Component.text("§aアイテムをロックすると、NPCとのトレードで使用できなくなります。"))
        return true
    }

}
