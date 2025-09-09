package com.github.azuazu3939.azurite.command

import com.github.azuazu3939.azurite.mana.Mana
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetMaxManaCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 2) {
            return sendMessage(sender, "§c/setmaxmana <MCID> <mana>")
        }

        val player = Bukkit.getPlayer(args[0]) ?: return sendMessage(sender, "§cプレイヤーが見つかりません。")
        val maxMana: Double
        try {
            maxMana = args[1].toDouble()
        } catch (e: NumberFormatException) {
            return sendMessage(sender, "§c最大マナが無効です。")
        }

        Mana(player).setMaxMana(maxMana)
        return sendMessage(player, "§b§l操作が完了しました。")
    }

    private fun sendMessage(sender: CommandSender, message: String) : Boolean {
        sender.sendMessage(Component.text(message))
        return true
    }
}
