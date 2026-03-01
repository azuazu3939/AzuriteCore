package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.mythic.MythicHolder
import com.github.azuazu3939.azurite.mythic.condition.ContainRegion
import com.github.azuazu3939.azurite.mythic.condition.FromSurface
import com.github.azuazu3939.azurite.mythic.condition.IsSight
import com.github.azuazu3939.azurite.mythic.condition.ItemGroup
import com.github.azuazu3939.azurite.mythic.mechanic.*
import com.github.azuazu3939.azurite.mythic.targeter.AzuriteConeTargeter
import com.github.azuazu3939.azurite.mythic.targeter.AzuriteRadiusTargeter
import com.github.azuazu3939.azurite.mythic.targeter.AzuriteRingTargeter
import io.lumine.mythic.api.skills.ISkillMechanic
import io.lumine.mythic.api.skills.conditions.ISkillCondition
import io.lumine.mythic.api.skills.targeters.ISkillTargeter
import io.lumine.mythic.bukkit.events.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

class MythicListener(private val plugin: Azurite) : Listener {

    @EventHandler
    fun onPreReloadCommand(event: PlayerCommandPreprocessEvent) {
        var command = event.message
        if (command.startsWith("/mythicmobs:")) {
            command = command.removePrefix("/mythicmobs:")
        }

        if (command.startsWith("/mm r") || command.startsWith("mm r")) {
            event.isCancelled = true
            if (RELOADING) return

            Bukkit.getOnlinePlayers().forEach {player -> player.sendMessage(Component.text("§e§lMythicMobsのリロードの予約がされました。")) }
            reloading()
            Bukkit.getOnlinePlayers().forEach(Consumer { player -> player.closeInventory() })
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onInventory(event: InventoryOpenEvent) {
        if (RELOADING || QUEUE) {
            event.isCancelled = true
            event.player.sendMessage(Component.text("§cリロード待機中やリロード中はInventoryを開けません。"))
        }
    }

    @EventHandler
    fun onPreReLoad(event: MythicPreReloadEvent) {
        Bukkit.getOnlinePlayers().forEach {player -> player.sendMessage(Component.text("§a§lMythicMobsのリロードが行われます。")) }
        RELOADING = true
    }

    @EventHandler
    fun onReloaded(event: MythicReloadedEvent) {
        MythicHolder(event.instance.placeholderManager).init()
        Bukkit.getOnlinePlayers().forEach {player -> player.sendMessage(Component.text("§6§lMythicMobsのリロードが完了しました。")) }
        RELOADING = false
        statSet.forEach {
            val player = Bukkit.getPlayer(it) ?: return@forEach
             plugin.mythic.playerManager.getProfile(player).statRegistry.updateDirtyStats()
        }
        statSet.clear()
    }

    @EventHandler
    fun onStat(event: MythicStatChangeEvent) {
        if (!RELOADING) return
        val caster = event.caster.entity
        if (!caster.isPlayer) return
        statSet.add(caster.uniqueId)
    }

    @EventHandler
    fun onMechanics(event: MythicMechanicLoadEvent) {
        val file = event.container.file
        val skillManager = plugin.mythic.skillManager

        val mechanicRegistry: Map<String, () -> ISkillMechanic> = mapOf(
            "setfall" to { SetFallDistance(skillManager, file, "setfalldistance", event.config) },
            "setfalldistance" to { SetFallDistance(skillManager, file, "setfalldistance", event.config) },

            "raid" to { RaidBoss(skillManager, file, "raidboss", event.config) },
            "raidboss" to { RaidBoss(skillManager, file, "raidboss", event.config) },
            "raids" to { RaidBoss(skillManager, file, "raidboss", event.config) },

            "addmask" to { AddMask(skillManager, file, "addmask", event.config) },
            "setmask" to { AddMask(skillManager, file, "setmask", event.config) },
            "addblockmask" to { AddMask(skillManager, file, "addblockmask", event.config) },
            "setblockmask" to { AddMask(skillManager, file, "setblockmask", event.config) },

            "ranking" to { Ranking(skillManager, file, "ranking", event.config) },
            "rank" to { Ranking(skillManager, file, "ranking", event.config) }
        )

        mechanicRegistry[event.mechanicName.lowercase()]?.let { event.register(it()) }
    }


    @EventHandler
    fun onConditions(event: MythicConditionLoadEvent) {
        val conditionRegistry: Map<String, () -> ISkillCondition> = mapOf(
            "surface" to { FromSurface(event.config) },
            "fromsurface" to { FromSurface(event.config) },
            "containsregion" to { ContainRegion(event.config) },
            "containregion" to { ContainRegion(event.config) },
            "containerregion" to { ContainRegion(event.config) },
            "itemgroup" to { ItemGroup(event.config) },
            "groupitem" to { ItemGroup(event.config) },
            "insight" to { IsSight(event.config) },
            "issight" to { IsSight(event.config) }
        )

        conditionRegistry[event.conditionName.lowercase()]?.let { event.register(it()) }
    }

    @EventHandler
    fun onTargeter(event: MythicTargeterLoadEvent) {
        val mlc = event.config
        val skill = event.container.manager

        val targeterRegistry: Map<String, () -> ISkillTargeter> = mapOf(
            "azuritecone" to { AzuriteConeTargeter(skill, mlc) },
            "azucone" to { AzuriteConeTargeter(skill, mlc) },
            "azcone" to { AzuriteConeTargeter(skill, mlc) },
            "acone" to { AzuriteConeTargeter(skill, mlc) },

            "azuriteradius" to { AzuriteRadiusTargeter(skill, mlc) },
            "azuradius" to { AzuriteRadiusTargeter(skill, mlc) },
            "azradius" to { AzuriteRadiusTargeter(skill, mlc) },
            "aradius" to { AzuriteRadiusTargeter(skill, mlc) },

            "azuritering" to { AzuriteRingTargeter(skill, mlc) },
            "azuring" to { AzuriteRingTargeter(skill, mlc) },
            "azring" to { AzuriteRingTargeter(skill, mlc) },
            "aring" to { AzuriteRingTargeter(skill, mlc) },
        )

        targeterRegistry[event.targeterName.lowercase()]?.let { event.register(it()) }
    }

    companion object {
        var RELOADING = false
        var QUEUE = false

        private val reloadSet = ConcurrentHashMap.newKeySet<UUID>()
        private val statSet = hashSetOf<UUID>()

        fun addRank(uuid: UUID) {
            reloadSet.add(uuid)
        }

        fun removeRank(uuid: UUID) {
            reloadSet.remove(uuid)
        }

        fun reloading() {
            QUEUE = true //MMReload Error防止
            if (reloadSet.isEmpty()) {
                RELOADING = true
                QUEUE = false
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm re -a")

            } else {
                Azurite.runLater(runnable = {
                    reloading()
                }, 100L)
            }
        }
    }
}
