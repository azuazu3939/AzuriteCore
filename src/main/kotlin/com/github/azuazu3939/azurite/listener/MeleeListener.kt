package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.util.Util
import com.google.common.collect.HashMultimap
import io.lumine.mythic.bukkit.MythicBukkit
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.*

class MeleeListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun omAttack(event: PrePlayerAttackEntityEvent) {
        event.isCancelled = true
        attack(event.player)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action.isRightClick || event.hand == null || event.hand == EquipmentSlot.OFF_HAND) return
        attack(event.player)
    }

    private fun attack(player: Player) {
        val ct = cooldown(player)
        val hand = player.inventory.itemInMainHand
        val mmid = MythicBukkit.inst().itemManager.getMythicTypeFromItem(hand) ?: return

        val mythic = MythicBukkit.inst().itemManager.getItem(mmid).orElse(null)
        if (mythic == null || !mythic.group.contains("Melee-Weapon")) return

        if (Util.isCooldown(javaClass, player.uniqueId, multimap)) return
        Util.setCooldown(javaClass, player.uniqueId, multimap, 4)

        if (mythic.group.contains("Multi") && ct != 0) {
            MythicBukkit.inst().apiHelper.castSkill(player, "Azuriter_Multi_Melee_Weapon_$ct")
        } else {
            MythicBukkit.inst().apiHelper.castSkill(player, "Azuriter_Melee_Weapon_$ct")
        }
    }

    private fun cooldown(player: Player) : Int {
        return (player.attackCooldown * 10).toInt()
    }

    companion object {
        private val multimap = HashMultimap.create<Class<*>, UUID>()
    }
}
