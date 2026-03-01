package com.github.azuazu3939.azurite.command

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class FlyCommand : TabExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.isEmpty()) {
            if (sender !is Player) return false
            sender.allowFlight = !sender.allowFlight
            sender.isFlying = sender.allowFlight
            sender.sendMessage(Component.text("§a飛行モードを切り替えました。 ${sender.name}"))
            return true
        }
        if (args.size == 1) {
            val type = args[0]
            val p = Bukkit.getPlayer(type)
            if (p != null) {
                p.isFlying = !p.isFlying
                sender.sendMessage(Component.text("§a飛行モードを切り替えました。 ${p.name}"))
                return true
            }
            val b = if (type.equals("on", true) || type.equals("enable", true)) {
                true
            } else if (type.equals("off", true) || type.equals("disable", true)) {
                false
            } else {
                return false
            }
            if (sender is Player) {
                sender.allowFlight = b
                sender.isFlying = b
                sender.sendMessage(Component.text("§a飛行モードを切り替えました。 ${sender.name}"))
                return true
            }
        }
        if (args.size == 2) {
            val type = args[0]
            val p = Bukkit.getPlayer(type)
            if (p != null) {

                val m = args[1]
                val b = if (m.equals("on", true) || m.equals("enable", true)) {
                    true
                } else if (m.equals("off", true) || m.equals("disable", true)) {
                    false
                } else {
                    !p.allowFlight
                }

                p.allowFlight = b
                p.isFlying = b
                sender.sendMessage(Component.text("§a飛行モードを切り替えました。 ${p.name}"))
                return true
            }
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String?> {
        if (args.size == 1) {
            return Bukkit.getOnlinePlayers().map { it.name }
        }
        return emptyList()
    }
}
