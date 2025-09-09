package com.github.azuazu3939.azurite.command

import com.github.azuazu3939.azurite.util.DPS
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class DPSCommand : TabExecutor {

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): MutableList<String>? {
        if (p3.size == 1) {
            return mutableListOf("on", "off", "clear")
        }
        return null
    }

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p0 !is Player) return false
        if (p3.size != 1) return sendMessage(p0)
        val mode = p3[0]
        if (mode.equals("on", true)) {
            DPS.on(p0.uniqueId)
            p0.sendMessage(Component.text("§aDPSの表示を有効にしました。"))
            return true
        } else if (mode.equals("off", true)) {
            DPS.off(p0.uniqueId)
            p0.sendMessage(Component.text("§cDPSの表示を無効にしました。"))
            return true
        } else if (mode.equals("clear", true)) {
            DPS.clear(p0.uniqueId)
            p0.sendMessage(Component.text("§bDPSの履歴を全て消しました。"))
            return true
        }
        return sendMessage(p0)
    }

    private fun sendMessage(p: CommandSender) : Boolean {
        p.sendMessage(Component.text("/dps [on, off, clear]"))
        return true
    }
}
