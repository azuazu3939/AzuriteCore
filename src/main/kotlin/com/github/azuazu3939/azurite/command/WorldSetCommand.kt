package com.github.azuazu3939.azurite.command

import com.github.azuazu3939.azurite.util.Util
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WorldSetCommand : CommandExecutor {

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p0 !is Player) return false
        val world = p0.world
        p0.sendMessage(Component.text(world.name + "のgameRuleを更新しました。"))
        Util.worldPreset(world, world.difficulty)
        return true
    }
}
