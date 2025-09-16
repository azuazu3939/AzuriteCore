package com.github.azuazu3939.azurite.mythic.condition

import com.github.azuazu3939.azurite.util.Util
import com.sk89q.worldedit.math.BlockVector3
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.adapters.AbstractLocation
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.api.skills.conditions.ILocationCondition
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.core.skills.SkillCondition

class ContainRegion(config: MythicLineConfig) : SkillCondition(config.line), IEntityCondition, ILocationCondition {

    private val region = PlaceholderString.of(config.getString(arrayOf("r", "region"), "__GLOBAL__")).get()

    override fun check(p0: AbstractEntity?): Boolean {
        threadSafetyLevel = ThreadSafetyLevel.EITHER
        val entity = p0?.bukkitEntity ?: return false
        val manager = Util.getRegionManager(entity.world) ?: return false
        val loc = entity.location
        val protect = manager.getApplicableRegions(BlockVector3.at(loc.x, loc.y, loc.z)).regions.firstOrNull {
            it.id.contains(region, true)
        }
        return protect != null
    }

    override fun check(p0: AbstractLocation?): Boolean {
        threadSafetyLevel = ThreadSafetyLevel.EITHER
        val loc = BukkitAdapter.adapt(p0)
        val manager = Util.getRegionManager(loc.world) ?: return false
        val protect = manager.getApplicableRegions(BlockVector3.at(loc.x, loc.y, loc.z)).regions.firstOrNull {
            it.id.contains(region, true)
        }
        return protect != null
    }
}