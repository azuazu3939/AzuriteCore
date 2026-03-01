package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import io.lumine.mythic.api.skills.damage.DamageMetadata
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffectType
import kotlin.math.pow
import kotlin.math.roundToInt

class DamageCalculationListener(private val plugin: Azurite) : Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onCombat(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return

        val victim = event.entity
        if (victim is Player) return
        if (victim !is LivingEntity) return

        val damaged = BukkitAdapter.adapt(victim) ?: return
        val op = damaged.getMetadata("skill-damage")
        val meta = if (op.isPresent) op.get() else return

        if (meta !is DamageMetadata) return
        meta.putBoolean("melee", true)

    }

    @Suppress("DEPRECATION")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onCombat(event: MythicDamageEvent) {
        val victim = event.target
        val attacker = event.caster.entity

        val base = event.damage
        val armor = victim.armor
        val toughness = victim.armorToughness
        val attack = attacker.damage

        val damage = calculation(base, attack, armor, toughness) * damageResistance(victim.bukkitEntity)

        event.damage = damage
    }

    private fun damageResistance(entity: Entity): Double {
        if (entity !is LivingEntity) return 1.0
        if (!entity.hasPotionEffect(PotionEffectType.RESISTANCE)) return 1.0
        var v = entity.getPotionEffect(PotionEffectType.RESISTANCE)?.amplifier?.plus(1)
        if (v != null && v >= 5) v = 5
        return 1 - (0.2 * v!!)
    }

    private fun calculation(value: Double, attack: Double, armor: Double, toughness: Double): Double {
        val fAttack = 0.0.coerceAtLeast(attack)
        val fARMOR = 0.0.coerceAtLeast(armor)
        val fTOUGHNESS = 0.0.coerceAtLeast(toughness)
        val fVALUE = 0.0.coerceAtLeast(value)

        var f = 0.0.coerceAtLeast(fVALUE * fAttack - fARMOR)
        if (f > 0) {
            f*= getValue(fTOUGHNESS)
        }

        return 1.0.coerceAtLeast((f / 20.0).roundToInt() * 1.0)
    }

    private fun math(lim: Double, value: Double): Double {
        val a = value.coerceAtMost(lim)
        return 1 - a / (a + lim)
    }

    private fun getValue(value: Double): Double {
        var resistance = 1.0
        for (depth in 0..9) {
            val lim = 100 * 2.0.pow(depth.toDouble())
            resistance *= math(lim, value)

            if (value <= lim) break
        }
        return resistance
    }
}
