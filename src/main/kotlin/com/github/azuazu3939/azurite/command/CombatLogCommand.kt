package com.github.azuazu3939.azurite.command

import com.github.azuazu3939.azurite.listener.CombatLogListener
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CombatLogCommand : CommandExecutor {

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p0 !is Player) return false
        CombatLogListener.setCombatLog(p0)
        return true
    }
}
