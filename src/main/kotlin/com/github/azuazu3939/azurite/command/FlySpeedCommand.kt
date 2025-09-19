package com.github.azuazu3939.azurite.command

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class FlySpeedCommand : TabExecutor {
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

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.size == 1) {
            if (sender !is Player) {
                sender.sendMessage(Component.text("§a/fs [Float] [<PlayerName>]"))
                return false
            }
            var num: Float
            try {
                num = args[0].toFloat()
            } catch (e: NumberFormatException) {
                sender.sendMessage(Component.text("§a/fs [Float] [<PlayerName>]"))
                return false
            }
            sender.flySpeed = num
            sender.sendMessage(Component.text("§a飛行速度を変更しました。"))
            return true
        }
        if (args.size == 2) {
            val p = Bukkit.getPlayer(args[1])
            if (p == null) {
                sender.sendMessage(Component.text("§a/fs [Float] [<PlayerName>]"))
                return false
            }
            var num: Float
            try {
                num = args[0].toFloat()
            } catch (e: NumberFormatException) {
                sender.sendMessage(Component.text("§a/fs [Float] [<PlayerName>]"))
                return false
            }
            p.flySpeed = num
            sender.sendMessage(Component.text("§a飛行速度を変更しました。"))
        }
        sender.sendMessage(Component.text("§a/fs [Float] [<PlayerName>]"))
        return false
    }
}
