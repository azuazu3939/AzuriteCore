package com.github.azuazu3939.azurite.command

import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class ModeCommand : TabExecutor {

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p0 !is Player) return false
        if (p3.isEmpty()) {
            switch(p0)
        } else if ("on".equals(p3[0], true)) {
            switch(p0, true)
        } else if ("off".equals(p3[0], true)) {
            switch(p0, false)
        } else {
            switch(p0)
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
        player.performCommand("egod $mode")
        player.performCommand("efly $mode")

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
        player.performCommand("efly " + player.name)
        player.performCommand("egod " + player.name)
        player.sendMessage(Component.text("§f§l運営モードを切り替えました。"))
    }

    override fun onTabComplete(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): MutableList<String>? {
        if (p3.size == 1) {
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
            player.performCommand("egod $god")

            var fly = ""
            fly += if (efly) {
                "enable"
            } else {
                "disable"
            }
            player.performCommand("efly $fly")

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
