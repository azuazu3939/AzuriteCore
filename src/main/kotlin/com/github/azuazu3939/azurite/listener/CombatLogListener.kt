package com.github.azuazu3939.azurite.listener

import io.lumine.mythic.bukkit.events.MythicDamageEvent
import net.kyori.adventure.text.Component
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.text.NumberFormat
import java.util.*


class CombatLogListener : Listener {

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        combatLog.remove(event.player.uniqueId)
    }

    @Suppress("DEPRECATION")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDamaged(e: EntityDamageEvent) {
        if (e.entity !is Player) return
        val player = e.entity as Player

        if (!combatLog.contains(player.uniqueId)) return

        if (e.cause == EntityDamageEvent.DamageCause.FALL) message(player, "落下", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.FIRE) message(player, "炎上", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.LAVA) message(player, "溶岩", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.POISON) message(player, "毒", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.VOID) message(player, "奈落", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.WITHER) message(player, "衰弱", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.FLY_INTO_WALL) message(player, "激突", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.SUICIDE) message(player, "自害", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.THORNS) message(player, "茨の鎧", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.STARVATION) message(player, "餓死", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.FIRE_TICK) message(player, "延焼", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.DRAGON_BREATH) message(player, "ドラゴンの息", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.HOT_FLOOR) message(player, "マグマブロック", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.DROWNING) message(player, "窒息", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) message(player, "爆発", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.SUFFOCATION) message(player, "窒息", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.CONTACT) message(player, "棘", e.finalDamage, false)
        if (e.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) message(player, "爆発", e.finalDamage, false)
    }

    @Suppress("DEPRECATION")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDamage(e: EntityDamageByEntityEvent) {
        if ((e.entity is Player)) {
            val player = e.entity as Player

            if (!combatLog.contains(player.uniqueId)) return

            if (e.cause == EntityDamageEvent.DamageCause.FALL) message(player, "落下", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.FIRE) message(player, "炎上", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.LAVA) message(player, "溶岩", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.POISON) message(player, "毒", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.VOID) message(player, "奈落", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.WITHER) message(player, "衰弱", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.LIGHTNING) message(player, "雷", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.FALLING_BLOCK) message(player, "落下中のブロック", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.FLY_INTO_WALL) message(player, "激突", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.MAGIC) message(player, "魔法", e.finalDamage, false, e.damager)
            if (e.cause == EntityDamageEvent.DamageCause.SUICIDE) message(player, "自害", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.THORNS) message(player, "茨の鎧", e.finalDamage, false, e.damager)
            if (e.cause == EntityDamageEvent.DamageCause.STARVATION) message(player, "餓死", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.FIRE_TICK) message(player, "延焼", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.DRAGON_BREATH) message(player, "ドラゴンの息", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.HOT_FLOOR) message(player, "マグマブロック", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.DROWNING) message(player, "窒息", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) message(player, "爆発", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.SUFFOCATION) message(player, "窒息", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.CONTACT) message(player, "棘", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) message(player, "爆発", e.finalDamage, false)
            if (e.cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) message(player, "範囲攻撃", e.finalDamage, false, e.damager)
        }

        if ((e.damager is Player)) {
            val player = e.damager as Player

            if (!combatLog.contains(player.uniqueId)) return

            if (e.cause == EntityDamageEvent.DamageCause.POISON) message(player, "毒", e.finalDamage, true)
            if (e.cause == EntityDamageEvent.DamageCause.WITHER) message(player, "衰弱", e.finalDamage, true)
            if (e.cause == EntityDamageEvent.DamageCause.LIGHTNING) message(player, "雷", e.finalDamage, true)
            if (e.cause == EntityDamageEvent.DamageCause.MAGIC) message(player, "魔法", e.finalDamage, true)
            if (e.cause == EntityDamageEvent.DamageCause.SUICIDE) message(player, "自害", e.finalDamage, true)
            if (e.cause == EntityDamageEvent.DamageCause.THORNS) message(player, "茨の鎧", e.finalDamage, true)
            if (e.cause == EntityDamageEvent.DamageCause.FIRE_TICK) message(player, "延焼", e.finalDamage, true)
            if (e.cause == EntityDamageEvent.DamageCause.DRAGON_BREATH) message(player, "ドラゴンの息", e.finalDamage, true)
            if (e.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) message(player, "爆発", e.finalDamage, true)
            if (e.cause == EntityDamageEvent.DamageCause.CONTACT) message(player, "棘", e.finalDamage, true)
            if (e.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) message(player, "爆発", e.finalDamage, true)
            if (e.cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) message(player, "範囲攻撃", e.finalDamage, true, e.entity)
        }
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
