package com.github.azuazu3939.azurite.command

import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CameraCommand : CommandExecutor {

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p0 !is Player) return false
        val attr = p0.getAttribute(Attribute.CAMERA_DISTANCE) ?: return false
        val v = attr.baseValue
        if (v <= 2) {
            attr.baseValue = 4.0
        } else if (v <= 4) {
            attr.baseValue = 6.0
        } else if (v <= 6) {
            attr.baseValue = 8.0
        } else if (v <= 8) {
            attr.baseValue = 2.0
        }
        p0.sendMessage(Component.text("§fカメラディスタンスを 「${attr.baseValue}」に設定しました。"))
        return true
    }
}
