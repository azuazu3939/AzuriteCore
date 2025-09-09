package com.github.azuazu3939.azurite.command

import com.github.azuazu3939.azurite.TrashInventory
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TrashCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        sender.closeInventory()
        sender.openInventory(TrashInventory().getInventory())
        return true
    }
}
