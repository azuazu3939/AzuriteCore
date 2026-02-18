package com.github.azuazu3939.azurite.command

import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class ModeCommand : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        if (args.isEmpty()) {
            switch(sender)
        } else if ("on".equals(args[0], true)) {
            switch(sender, true)
        } else if ("off".equals(args[0], true)) {
            switch(sender, false)
        } else {
            switch(sender)
        }
        return true
    }

    private fun switch(player: Player, bypass: Boolean) {
        var mode = ""
        mode += if (bypass) {
            "enable"
        } else {
            "disable"
        }
        player.performCommand("god $mode")
        player.performCommand("fly $mode")

        var m = ""
        m += if (bypass) {
            "on"
        } else {
            "off"
        }
        player.performCommand("rg bypass $m")
        player.sendMessage(Component.text("§f§l運営モード §b§l$bypass§f§lに切り替えました。"))
    }

    private fun switch(player: Player) {
        player.performCommand("rg bypass")
        player.performCommand("fly " + player.name)
        player.performCommand("god " + player.name)
        player.sendMessage(Component.text("§f§l運営モードを切り替えました。"))
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String>? {
        if (args.size == 1) {
            return mutableListOf("on", "off")
        }
        return null
    }

    companion object {

        fun switch(player: Player, egod: Boolean, efly: Boolean, ebypass: Boolean) {
            var god = ""
            god += if (egod) {
                "enable"
            } else {
                "disable"
            }
            player.performCommand("god $god")

            var fly = ""
            fly += if (efly) {
                "enable"
            } else {
                "disable"
            }
            player.performCommand("fly $fly")

            var bypass = ""
            bypass += if (ebypass) {
                "on"
            } else {
                "off"
            }
            player.performCommand("rg bypass $bypass")
            player.sendMessage(Component.text("§f§l運営モードを切り替えました。"))
        }
    }
}
