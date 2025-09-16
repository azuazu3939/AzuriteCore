package com.github.azuazu3939.azurite.mythic.condition

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.SkillCondition
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import kotlin.jvm.optionals.getOrNull

class ItemGroup(private val config: MythicLineConfig) : SkillCondition(config.line), IEntityCondition {

    override fun check(p0: AbstractEntity?): Boolean {
        threadSafetyLevel = ThreadSafetyLevel.EITHER
        val entity = p0?.bukkitEntity
        if (entity is Player) {
            val groupID = config.getPlaceholderString(arrayOf("g", "group"), "melee")?.get() ?: "melee"
            val contains = config.getPlaceholderBoolean(arrayOf("c", "contains", "check", "contain"), true)?.get() ?: true
            val slot = config.getPlaceholderString(arrayOf("s", "slot"), "HAND")?.get() ?: "HAND"
            val equip = EquipmentSlot.valueOf(slot.uppercase())

            entity.inventory.getItem(equip).let {
                val mmid = MythicBukkit.inst().itemManager.getMythicTypeFromItem(it) ?: return false
                if (contains) {
                    return MythicBukkit.inst().itemManager.getItem(mmid).getOrNull()?.group?.contains(groupID, true) ?: false
                } else {
                    return MythicBukkit.inst().itemManager.getItem(mmid).getOrNull()?.group?.equals(groupID, true) ?: false
                }
            }
        }
        return false
    }
}