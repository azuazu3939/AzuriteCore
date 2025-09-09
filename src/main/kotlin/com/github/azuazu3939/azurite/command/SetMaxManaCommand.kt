package com.github.azuazu3939.azurite.command

import com.github.azuazu3939.azurite.mana.Mana
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetMaxManaCommand : CommandExecutor {

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p3.size != 2) {
            return sendMessage(p0, "§c/setmaxmana <MCID> <mana>")
        }

        val player = Bukkit.getPlayer(p3[0]) ?: return sendMessage(p0, "§cプレイヤーが見つかりません。")
        val maxMana: Double
        try {
            maxMana = p3[1].toDouble()
        } catch (e: NumberFormatException) {
            return sendMessage(p0, "§c最大マナが無効です。")
        }

        Mana(player).setMaxMana(maxMana)
        return sendMessage(player, "§b§l操作が完了しました。")
    }

    private fun sendMessage(sender: CommandSender, message: String) : Boolean {
        sender.sendMessage(Component.text(message))
        return true
    }
}
