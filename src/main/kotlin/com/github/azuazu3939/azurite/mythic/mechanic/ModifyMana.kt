package com.github.azuazu3939.azurite.mythic.mechanic

import dev.aurelium.auraskills.api.AuraSkillsApi
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.placeholders.PlaceholderBoolean
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
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

        val user = AuraSkillsApi.get().getUser(p1.uniqueId)
        if (isMultiply.get(p0)) {
            user.mana = (user.maxMana * add.get(p1) + user.mana).coerceAtMost(user.maxMana)
        } else {
            user.mana = (add.get(p0) + user.mana).coerceAtMost(user.maxMana)
        }
        return SkillResult.SUCCESS
    }
}
