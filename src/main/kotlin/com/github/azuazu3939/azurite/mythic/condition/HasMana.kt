package com.github.azuazu3939.azurite.mythic.condition

import com.github.azuazu3939.azurite.mana.Mana
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.api.skills.conditions.ISkillCondition
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.utils.numbers.RangedDouble

class HasMana(private val config: MythicLineConfig?) : ISkillCondition, IEntityCondition {

    override fun check(p0: AbstractEntity?): Boolean {
        val range = RangedDouble(config?.getPlaceholderString(arrayOf("a", "amount"), "10")?.get() ?: "10")
        if (p0 == null) return false
        if (!p0.isPlayer) return false
        val player = BukkitAdapter.adapt(p0.asPlayer())
        return range.equals(Mana(player).getMana())
    }
}