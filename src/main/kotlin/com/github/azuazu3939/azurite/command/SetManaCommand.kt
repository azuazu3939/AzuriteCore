package com.github.azuazu3939.azurite.command

import com.github.azuazu3939.azurite.mana.Mana
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetManaCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 2) {
            return sendMessage(sender, "§c/setmana <MCID> <mana>")
        }

        val player = Bukkit.getPlayer(args[0]) ?: return sendMessage(sender, "§cプレイヤーが見つかりません。")
        val mana: Double
        try {
            mana = args[1].toDouble()
        } catch (e: NumberFormatException) {
            return sendMessage(sender, "§cマナが無効です。")
        }

        Mana(player).setMana(mana)
        return sendMessage(player, "§b§l操作が完了しました。")
    }

    private fun sendMessage(sender: CommandSender, message: String) : Boolean {
        sender.sendMessage(Component.text(message))
        return true
    }
}
