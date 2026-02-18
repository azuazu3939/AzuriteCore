package com.github.azuazu3939.azurite.command

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.database.DBCon
import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class LostBoxCommand(private val plugin: Azurite) : SuspendingCommandExecutor, InventoryHolder {

    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return false
        val inv = DBCon.loadBox(sender.uniqueId, inventory)

        sender.closeInventory()
        sender.openInventory(inv)

        sender.sendMessage(Component.text("ロストボックスを開きました"))
        return true
    }

    override fun getInventory(): Inventory {
        val inv = Bukkit.createInventory(this, 54, Component.text("§5§lロストボックス"))
        return inv
    }
}
