package com.github.azuazu3939.azurite.mythic.mechanic

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ISkillMechanic
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.placeholders.PlaceholderFloat
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import java.io.File

class SetFallDistance(manager: SkillExecutor, file: File, line: String, mlc: MythicLineConfig?) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill {
    private val fallDistance: PlaceholderFloat

    init {
        val fallDistance = mlc?.getPlaceholderFloat(arrayOf("v", "value", "a", "amount"), -1F)!!
        this.fallDistance = fallDistance
    }

    override fun castAtEntity(p0: SkillMetadata?, p1: AbstractEntity?): SkillResult {
        val f = fallDistance.get(p0)
        if (f == -1F) return SkillResult.CONDITION_FAILED
        if (p1 != null) {
            p1.bukkitEntity.fallDistance = f
        }
        return SkillResult.SUCCESS
    }
}