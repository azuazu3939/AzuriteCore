package com.github.azuazu3939.azurite.mythic.mechanic

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import java.io.File

class Ranking(skillManager: SkillExecutor?, file: File?, s: String, config: MythicLineConfig?) : SkillMechanic(skillManager, file, s, config), ITargetedEntitySkill {

    override fun castAtEntity(p0: SkillMetadata?, p1: AbstractEntity?): SkillResult {
        if (p1 != null) {
            if (p1.isPlayer) return SkillResult.CONDITION_FAILED
            if (!p1.isLiving) return SkillResult.CONDITION_FAILED

            p1.dataContainer.set(NamespacedKey("az", "ranking"), PersistentDataType.STRING, "true")
            SkillResult.SUCCESS
        }
        return SkillResult.INVALID_TARGET
    }
}
