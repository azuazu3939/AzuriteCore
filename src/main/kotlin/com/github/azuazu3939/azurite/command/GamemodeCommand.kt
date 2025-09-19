package com.github.azuazu3939.azurite.command

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player


class GamemodeCommand : TabExecutor {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String?> {
        if (args.size == 2) {
            return Bukkit.getOnlinePlayers().map { it.name }
        }
        return emptyList()
    }

    @Suppress("UnstableApiUsage")
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.size == 1) {
            if (sender !is Player) return false
            try {
                val num = args[0].toInt()
                if (num !in 0..3) {
                    sender.sendMessage(Component.text("/gm <0-3> [<PlayerName>]"))
                    return false
                }
                sender.gameMode = GameMode.getByValue(num)!!
                return true
            } catch (e: NumberFormatException) {
                sender.sendMessage(Component.text("/gm <0-3> [<PlayerName>]"))
                return false
            }
        }
        if (args.size == 2) {
            val p = Bukkit.getPlayer(args[1])
            if (p == null) {
                sender.sendMessage(Component.text("/gm <0-3> [<PlayerName>]"))
                return false
            }
            try {
                val num = args[0].toInt()
                if (num !in 0..3) {
                    sender.sendMessage(Component.text("/gm <0-3> [<PlayerName>]"))
                    return false
                }
                p.gameMode = GameMode.getByValue(num)!!
                return true
            } catch (e: NumberFormatException) {
                sender.sendMessage(Component.text("/gm <0-3> [<PlayerName>]"))
                return false
            }
        }
        sender.sendMessage(Component.text("/gm <0-3> [<PlayerName>]"))
        return false
    }
}
