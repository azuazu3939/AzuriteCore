package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.mythic.condition.ContainRegion
import com.github.azuazu3939.azurite.mythic.condition.FromSurface
import com.github.azuazu3939.azurite.mythic.condition.HasMana
import com.github.azuazu3939.azurite.mythic.condition.ItemGroup
import com.github.azuazu3939.azurite.mythic.mechanic.*
import io.lumine.mythic.api.skills.ISkillMechanic
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import io.lumine.mythic.bukkit.events.MythicPreReloadEvent
import io.lumine.mythic.bukkit.events.MythicReloadedEvent
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

class MythicListener : Listener {

    @EventHandler
    fun onPreReloadCommand(event: PlayerCommandPreprocessEvent) {
        var command = event.message
        if (command.startsWith("/mythicmobs:")) {
            command = command.removePrefix("/mythicmobs:")
        }

        if (command.startsWith("/mm r") || command.startsWith("mm r")) {
            event.isCancelled = true
            if (isMythicReloading) return

            Bukkit.getOnlinePlayers().forEach {player -> player.sendMessage(Component.text("§e§lMythicMobsのリロードが予約されました。")) }
            reloading()
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onInventory(event: InventoryOpenEvent) {
        if (isMythicReloading || isReloading) {
            event.isCancelled = true
            event.player.sendMessage(Component.text("§cリロード待機中やリロード中はInventoryを開けません。"))
        }
    }

    @EventHandler
    fun onPreReLoad(event: MythicPreReloadEvent) {
        Bukkit.getOnlinePlayers().forEach {player -> player.sendMessage(Component.text("§a§lMythicMobsのリロードが行われます。")) }
        isMythicReloading = true
    }

    @EventHandler
    fun onReloaded(event: MythicReloadedEvent) {
        Bukkit.getOnlinePlayers().forEach {player -> player.sendMessage(Component.text("§6§lMythicMobsのリロードが完了しました。")) }
        isMythicReloading = false
    }

    @EventHandler
    fun onMechanics(event: MythicMechanicLoadEvent) {
        val file = event.container.file
        val skillManager = MythicBukkit.inst().skillManager

        val mechanicRegistry: Map<String, () -> ISkillMechanic> = mapOf(
            "setfall" to { SetFallDistance(skillManager, file, "setfalldistance", event.config) },
            "setfalldistance" to { SetFallDistance(skillManager, file, "setfalldistance", event.config) },

            "raid" to { RaidBoss(skillManager, file, "raidboss", event.config) },
            "raidboss" to { RaidBoss(skillManager, file, "raidboss", event.config) },
            "raids" to { RaidBoss(skillManager, file, "raidboss", event.config) },

            "mana" to { ModifyMana(skillManager, file, "modifymana", event.config) },
            "editmana" to { ModifyMana(skillManager, file, "modifymana", event.config) },
            "modifymana" to { ModifyMana(skillManager, file, "modifymana", event.config) },

            "removemask" to { RemoveMask(skillManager, file, "removemask", event.config) },
            "deletemask" to { RemoveMask(skillManager, file, "removemask", event.config) },
            "clearmask" to { RemoveMask(skillManager, file, "removemask", event.config) },

            "ranking" to { Ranking(skillManager, file, "ranking", event.config) },
            "rank" to { Ranking(skillManager, file, "ranking", event.config) }
        )

        mechanicRegistry[event.mechanicName.lowercase()]?.let { event.register(it()) }
    }


    @EventHandler
    fun onConditions(event: MythicConditionLoadEvent) {
        val conditionRegistry: Map<String, (MythicConditionLoadEvent) -> Unit> = mapOf(
            "hasmana" to { e -> e.register(HasMana(e.config)) },
            "ismana" to { e -> e.register(HasMana(e.config)) },
            "surface" to { e -> e.register(FromSurface(e.config)) },
            "fromsurface" to { e -> e.register(FromSurface(e.config)) },
            "containsregion" to { e -> e.register(ContainRegion(e.config)) },
            "containregion" to { e -> e.register(ContainRegion(e.config)) },
            "containerregion" to { e -> e.register(ContainRegion(e.config)) },
            "itemgroup" to { e -> e.register(ItemGroup(e.config)) },
            "groupitem" to { e -> e.register(ItemGroup(e.config)) }
        )

        conditionRegistry[event.conditionName.lowercase()]?.invoke(event)
    }

    companion object {
        var isMythicReloading = false
        var isReloading = false

        private val reloadSet = ConcurrentHashMap.newKeySet<UUID>()

        fun addRank(uuid: UUID) {
            reloadSet.add(uuid)
        }

        fun removeRank(uuid: UUID) {
            reloadSet.remove(uuid)
        }

        fun reloading() {
            isReloading = true
            Bukkit.getOnlinePlayers().forEach(Consumer { player -> player.closeInventory() }) //MMReload Error防止
            if (reloadSet.isEmpty()) {
                isMythicReloading = true
                isReloading = false
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm re -a")

            } else {
                Azurite.runLater(runnable = {
                    reloading()
                }, 100L)
            }
        }
    }
}
