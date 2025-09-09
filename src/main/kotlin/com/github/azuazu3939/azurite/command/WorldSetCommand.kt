package com.github.azuazu3939.azurite.command

import com.github.azuazu3939.azurite.util.Util
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WorldSetCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        val world = sender.world
        sender.sendMessage(Component.text(world.name + "のgameRuleを更新しました。"))
        Util.worldPreset(world, world.difficulty)
        return true
    }
}
