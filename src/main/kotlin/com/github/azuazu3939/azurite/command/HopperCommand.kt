package com.github.azuazu3939.azurite.command

import io.lumine.mythic.bukkit.MythicBukkit
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

class HopperCommand : CommandExecutor {

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p0 !is Player) return false
        if (p3.size != 3) {
            p0.sendMessage(Component.text("Hopper見て、/hopper <mob> <mmid> <amount>"))
            return false
        }

        val i: Int
        try {
            i = p3[2].toInt()
        } catch (e: Exception) {
            p0.sendMessage(Component.text("amountが不正です。"))
            return false
        }

        val mob = MythicBukkit.inst().mobManager.getMythicMob(p3[0]).getOrNull()
        val mmid = MythicBukkit.inst().itemManager.getItemStack(p3[1], i)

        if (mmid == null) {
            p0.sendMessage(Component.text("mmidがNullです。"))
            return false
        }
        if (mob == null) {
            p0.sendMessage(Component.text("mobがNullです。"))
            return false
        }

        val ray = p0.rayTraceBlocks(5.0, FluidCollisionMode.NEVER)
        if (ray != null && ray.hitBlock != null && ray.hitBlock!!.type == Material.HOPPER) {
            val hopper = ray.hitBlock!!.state as Hopper
            hopper.persistentDataContainer.set(NamespacedKey("az", "hopper_mob"), PersistentDataType.STRING, p3[0])
            hopper.persistentDataContainer.set(NamespacedKey("az", "hopper_item"), PersistentDataType.STRING, p3[1] + " " + i)
            hopper.update(true)
            p0.sendMessage("完了")
            return true
        }
        return false
    }
}
