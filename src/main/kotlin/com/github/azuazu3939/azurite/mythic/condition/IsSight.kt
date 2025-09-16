package com.github.azuazu3939.azurite.mythic.condition

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.conditions.IEntityComparisonCondition
import io.lumine.mythic.api.skills.conditions.ISkillCondition
import io.lumine.mythic.bukkit.BukkitAdapter
import org.bukkit.FluidCollisionMode
import org.bukkit.entity.LivingEntity

class IsSight(config: MythicLineConfig) : ISkillCondition, IEntityComparisonCondition {

    override fun check(caster: AbstractEntity?, target: AbstractEntity?): Boolean {
        val entity = caster?.bukkitEntity as? LivingEntity ?: return false

        val hitLoc = entity.rayTraceBlocks(64.0, FluidCollisionMode.ALWAYS)
            ?.hitBlock
            ?.location
            ?.let { BukkitAdapter.adapt(it) }
            ?: return false

        val casterLoc = caster.location ?: return false
        val targetLoc = target?.location ?: return false

        return casterLoc.distance(hitLoc) >= targetLoc.distance(hitLoc)
    }
}

