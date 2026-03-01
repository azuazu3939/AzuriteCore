package com.github.azuazu3939.azurite.mythic

import io.lumine.mythic.api.skills.placeholders.PlaceholderManager
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.core.skills.placeholders.Placeholder
import org.bukkit.attribute.Attribute

class MythicHolder(private val mgr: PlaceholderManager) {

    fun init() {
        attackSpeed()
        attackSpeedDouble()
    }

    private fun attackSpeed() {
        mgr.register("az.attack_speed", Placeholder.entity { t, u ->
            if (!t.isPlayer) return@entity "0"
            val p = BukkitAdapter.adapt(t.asPlayer())
            return@entity p.getAttribute(Attribute.ATTACK_SPEED)?.value?.toInt()?.toString() ?: return@entity "0"
        })
    }

    private fun attackSpeedDouble() {
        mgr.register("az.attack_speed.double", Placeholder.entity { t, u ->
            if (!t.isPlayer) return@entity "0.0"
            val p = BukkitAdapter.adapt(t.asPlayer())
            return@entity p.getAttribute(Attribute.ATTACK_SPEED)?.value?.toString() ?: return@entity "0.0"
        })
    }
}