package com.github.azuazu3939.azurite.command

import com.github.azuazu3939.azurite.util.DPS
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class DPSCommand : TabExecutor {

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (args.size == 1) {
            return mutableListOf("on", "off", "clear")
        }
        return null
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        if (args.size != 1) return sendMessage(sender)
        val mode = args[0]
        if (mode.equals("on", true)) {
            DPS.on(sender.uniqueId)
            sender.sendMessage(Component.text("§aDPSの表示を有効にしました。"))
            return true
        } else if (mode.equals("off", true)) {
            DPS.off(sender.uniqueId)
            sender.sendMessage(Component.text("§cDPSの表示を無効にしました。"))
            return true
        } else if (mode.equals("clear", true)) {
            DPS.clear(sender.uniqueId)
            sender.sendMessage(Component.text("§bDPSの履歴を全て消しました。"))
            return true
        }
        return sendMessage(sender)
    }

    private fun sendMessage(p: CommandSender) : Boolean {
        p.sendMessage(Component.text("/dps [on, off, clear]"))
        return true
    }
}
