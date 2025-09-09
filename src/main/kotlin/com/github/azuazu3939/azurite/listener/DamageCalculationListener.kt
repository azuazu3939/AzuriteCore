package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.mythic.MythicTrigger
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.potion.PotionEffectType
import kotlin.math.pow
import kotlin.math.roundToInt

class DamageCalculationListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onDamaged(event: MythicDamageEvent) {
        val attacker = event.caster.entity
        val victim = event.target

        val base = event.damage
        val armor = victim.armor
        val toughness = victim.armorToughness
        val attack = attacker.damage

        event.damage = calculation(base, attack, armor, toughness)
        MythicTrigger(event.caster).triggerAzu(victim)
    }

    companion object {
        fun damageResistance(entity: Entity) : Double {
            if (entity !is LivingEntity) return 1.0
            if (!entity.hasPotionEffect(PotionEffectType.RESISTANCE)) return 1.0
            var v = entity.getPotionEffect(PotionEffectType.RESISTANCE)?.amplifier?.plus(1)
            if (v != null && v >= 5) v = 5
            return 1 - (0.2 * v!!)
        }
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
