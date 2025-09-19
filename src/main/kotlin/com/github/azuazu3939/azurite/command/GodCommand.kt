package com.github.azuazu3939.azurite.command

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.util.*

class GodCommand : TabExecutor {

    companion object {
        private val GODs = hashSetOf<UUID>()

        fun isGod(uuid: UUID): Boolean {
            return GODs.contains(uuid)
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String?> {
        if (args.size == 2) {
            return Bukkit.getOnlinePlayers().map { it.name }
        }
        if (args.size == 1) {
            return arrayListOf("0", "1", "2", "3")
        }
        return emptyList()
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
       if (args.isEmpty()) {
           if (sender !is Player) return false
           toggle(sender)
           sender.sendMessage(Component.text("§a無敵モードを切り替えました。 ${sender.name}"))
           return true
       }
        if (args.size == 1) {
            val type = args[0]
            val p = Bukkit.getPlayer(type)
            if (p != null) {
                toggle(p)
                sender.sendMessage(Component.text("§a無敵モードを切り替えました。 ${p.name}"))
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
                toggle(sender, b)
                sender.sendMessage(Component.text("§a無敵モードを切り替えました。 ${sender.name}"))
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
                    !GODs.contains(p.uniqueId)
                }

                toggle(p, b)
                sender.sendMessage(Component.text("§a無敵モードを切り替えました。 ${p.name}"))
                return true
            }
        }
        return false
    }

    private fun toggle(element: Player) {
        if (!GODs.add(element.uniqueId)) {
            GODs.remove(element.uniqueId)
        } else {
            heal(element)
        }
    }

    private fun toggle(element: Player, toggle: Boolean) {
        if (toggle) {
            GODs.add(element.uniqueId)
            heal(element)
        } else {
            GODs.remove(element.uniqueId)
        }
    }

    private fun heal(element: Player) {
        element.heal(element.getAttribute(Attribute.MAX_HEALTH)?.value ?: 0.0)
        element.saturation = 20f
        element.maximumAir = element.maximumAir
    }
}
