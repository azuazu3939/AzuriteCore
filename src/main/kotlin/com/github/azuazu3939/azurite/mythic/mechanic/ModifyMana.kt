package com.github.azuazu3939.azurite.mythic.mechanic

import com.github.azuazu3939.azurite.mana.ManaRegen
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.*
import io.lumine.mythic.api.skills.placeholders.PlaceholderBoolean
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.entity.Player
import java.io.File

class ModifyMana(manager: SkillExecutor, file: File, line: String, mlc: MythicLineConfig?) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill {
    private var add : PlaceholderDouble
    private val isMultiply : PlaceholderBoolean

    init {
        this.threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
        add = mlc?.getPlaceholderDouble(arrayOf("a", "amount"), 1.0)!!
        isMultiply = mlc.getPlaceholderBoolean(arrayOf("m", "multiple"), false)
    }

    override fun castAtEntity(p0: SkillMetadata?, p1: AbstractEntity?): SkillResult {
        if (p1 == null) return SkillResult.INVALID_TARGET
        if (!p1.isPlayer) return SkillResult.INVALID_TARGET

        val player = BukkitAdapter.adapt(p1) as Player
        if (isMultiply.get(p0)) {
            ManaRegen(player).regen(add.get(p0))
        } else {
            val mana = ManaRegen(player)
            mana.setMana((add.get(p0) + mana.getMana()).coerceAtMost(mana.getMaxMana()))
        }
        return SkillResult.SUCCESS
    }
}
