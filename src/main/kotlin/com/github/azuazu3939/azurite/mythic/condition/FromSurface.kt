package com.github.azuazu3939.azurite.mythic.condition

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.utils.numbers.RangedInt
import io.lumine.mythic.core.skills.SkillCondition
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

class FromSurface(private val config: MythicLineConfig) : SkillCondition(config.line), IEntityCondition {

    override fun check(p0: AbstractEntity?): Boolean {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
        val entity = BukkitAdapter.adapt(p0)
        var b = entity.location.block
        var i = 0
        while (check(b)) {
            b = b.getRelative(BlockFace.DOWN)
            i++
            if (b.y <= -62) break
        }
        return RangedInt(config.getPlaceholderString(arrayOf("r", "range"), "1").get()).equals(i)
    }

    private fun check(block: Block) : Boolean {
        val m = block.type
        return m.isAir || (!m.isSolid && m != Material.WATER && m != Material.LAVA)
    }
}