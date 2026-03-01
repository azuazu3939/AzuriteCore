package com.github.azuazu3939.azurite.command

import com.github.azuazu3939.azurite.Azurite
import net.kyori.adventure.text.Component
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Hopper
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import kotlin.jvm.optionals.getOrNull

class HopperCommand(private val plugin: Azurite) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false
        if (args.size != 3) {
            sender.sendMessage(Component.text("Hopper見て、/hopper <mob> <mmid> <amount>"))
            return false
        }

        val i: Int
        try {
            i = args[2].toInt()
        } catch (e: Exception) {
            sender.sendMessage(Component.text("amountが不正です。"))
            return false
        }

        val mob = plugin.mythic.mobManager.getMythicMob(args[0]).getOrNull()
        val mmid = plugin.mythic.itemManager.getItemStack(args[1], i)

        if (mmid == null) {
            sender.sendMessage(Component.text("mmidがNullです。"))
            return false
        }
        if (mob == null) {
            sender.sendMessage(Component.text("mobがNullです。"))
            return false
        }

        val ray = sender.rayTraceBlocks(5.0, FluidCollisionMode.NEVER)
        if (ray != null && ray.hitBlock != null && ray.hitBlock!!.type == Material.HOPPER) {
            val hopper = ray.hitBlock!!.state as Hopper
            hopper.persistentDataContainer.set(NamespacedKey("az", "hopper_mob"), PersistentDataType.STRING, args[0])
            hopper.persistentDataContainer.set(
                NamespacedKey("az", "hopper_item"),
                PersistentDataType.STRING,
                args[1] + " " + i
            )
            hopper.update(true)
            sender.sendMessage("完了")
            return true
        }
        return false
    }
}
