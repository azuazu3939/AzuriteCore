package com.github.azuazu3939.azurite.listener

import dev.aurelium.auraskills.api.AuraSkillsApi
import dev.aurelium.auraskills.api.skill.Skills
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class MythicEXPListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDamaged(event: MythicDamageEvent) {
        if (event.target.isPlayer) return
        val mm = MythicBukkit.inst().mobManager.getMythicMobInstance(event.target) ?: return
        if (mm.type.internalName.contains("admin", true)) return

        val cast = event.caster.entity
        if (!cast.isPlayer) return

        val user = AuraSkillsApi.get().getUser(cast.uniqueId)
        val cause = event.damageMetadata.damageCause

        val dmg = if (event.target.health - event.damage <= 0) {
            event.target.health
        } else {
            event.damage
        }

        if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            user.addSkillXp(Skills.FIGHTING, dmg)

        } else if (cause == EntityDamageEvent.DamageCause.PROJECTILE) {
            user.addSkillXp(Skills.ARCHERY, dmg)
        }
    }
}
