package com.github.azuazu3939.azurite.listener

import io.lumine.mythic.bukkit.events.MythicDamageEvent
import net.kyori.adventure.text.Component
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.text.NumberFormat
import java.util.*


class CombatLogListener : Listener {

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        combatLog.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onMythicAttack(event: MythicDamageEvent) {
        if (event.target.isPlayer) return
        if (!event.caster.entity.isPlayer) return
        if (!combatLog.contains(event.caster.entity.uniqueId)) return

        val attacker = event.caster.entity.bukkitEntity as Player
        val victim = event.target.bukkitEntity
        message(attacker, "ミシック", event.damage * DamageCalculationListener.damageResistance(victim), true, victim)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onMythicVictim(event: MythicDamageEvent) {
        if (!event.target.isPlayer) return
        if (event.caster.entity.isPlayer) return
        if (!combatLog.contains(event.target.uniqueId)) return

        val victim = event.target.bukkitEntity as Player
        message(victim, "ミシック", event.damage * DamageCalculationListener.damageResistance(victim), false, event.caster.entity.bukkitEntity)
    }

    private fun message(p: Player, type: String, damage: Double, send: Boolean) {
        val prefix = "§8[§cCombatLog§8] "
        val damageType: String
        val damageColor: String
        val num = NumberFormat.getInstance()
        num.maximumFractionDigits = 1

        if (send) {
            damageType = "§a与"
            damageColor = "§a"
        } else {
            damageType = "§c受"
            damageColor = "§c"
        }
        p.sendMessage(Component.text("$prefix$damageColor${num.format(damage)} §8($damageType §7$type§8)"))
    }

    @Suppress("SameParameterValue")
    private fun message(p: Player, type: String, damage: Double, send: Boolean, entity: Entity) {
        val prefix = "§8[§cCombatLog§8] "
        val damageType: String
        val damageColor: String
        val entityName = "§f" + entity.name + " "
        val num = NumberFormat.getInstance()
        num.maximumFractionDigits = 1

        if (send) {
            damageType = "§a与"
            damageColor = "§a"
        } else {
            damageType = "§c受"
            damageColor = "§c"
        }
        p.sendMessage("$prefix$entityName$damageColor${num.format(damage)} §8($damageType §7$type§8)")
    }

    companion object {
        private val combatLog = mutableSetOf<UUID>()

        fun setCombatLog(player: Player) {
            if (combatLog.contains(player.uniqueId)) {
                combatLog.remove(player.uniqueId)
                player.sendMessage(Component.text("§cCombatLogを非表示にしました。"))
            } else {
                combatLog.add(player.uniqueId)
                player.sendMessage(Component.text("§aCombatLogを表示にしました。"))
            }
        }
    }
}
