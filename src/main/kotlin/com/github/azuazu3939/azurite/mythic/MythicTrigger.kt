package com.github.azuazu3939.azurite.mythic

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.skills.SkillCaster
import io.lumine.mythic.api.skills.SkillTrigger
import io.lumine.mythic.bukkit.MythicBukkit

class MythicTrigger(private val caster: SkillCaster) {

    fun triggerHeal() {
        if (!caster.entity.isPlayer) return
        val meta = MythicBukkit.inst().skillManager.eventBus.buildSkillMetadata(HEAL, caster, caster.entity, null, false)
        MythicBukkit.inst().skillManager.eventBus.processTriggerMechanics(meta)
    }

    fun triggerAzu(triggerEntity: AbstractEntity) {
        val meta = MythicBukkit.inst().skillManager.eventBus.buildSkillMetadata(AZU, caster, triggerEntity, triggerEntity.location, false)
        MythicBukkit.inst().skillManager.eventBus.processTriggerMechanics(meta)
    }

    companion object {
        private val HEAL = SkillTrigger.create("HEALED", "HEAL")
        private val AZU = SkillTrigger.create("AZURITE", "AZU", "AZ")

         init {
             HEAL.register()
             AZU.register()
         }
    }
}