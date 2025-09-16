package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.command.ModeCommand
import com.github.azuazu3939.azurite.database.DBCon
import com.github.azuazu3939.azurite.mythic.MythicTrigger
import com.github.azuazu3939.azurite.util.PluginDispatchers.runTask
import com.github.azuazu3939.azurite.util.Util
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.events.MythicHealMechanicEvent
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.CrafterCraftEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Suppress("RedundantSuspendModifier")
class GenericRulesListener(private val plugin: Azurite) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCraft(event: CraftItemEvent) {
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCraft(event: CrafterCraftEvent) {
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPreJoin(event: AsyncPlayerPreLoginEvent) {
        DBCon.loadSpawn(event.uniqueId)
        if (MythicListener.isMythicReloading || MythicListener.isReloading) {
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
        }
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onDrop(event: PlayerDropItemEvent) {
        val i = event.itemDrop.itemStack
        val p = event.player
        val meta = i.itemMeta
        if (i.hasItemMeta()) {
            meta.persistentDataContainer.set(NamespacedKey("az", "player_dropped"), PersistentDataType.STRING, p.uniqueId.toString())
            i.setItemMeta(meta)
            event.itemDrop.itemStack = i
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPickup(event: EntityPickupItemEvent) {
        val p = event.entity
        if (p !is Player) return
        val i = event.item.itemStack
        val meta = i.itemMeta
        if (!i.hasItemMeta()) return

        if (meta.persistentDataContainer.has(NamespacedKey("az", "player_dropped"), PersistentDataType.STRING)) {
            val u = meta.persistentDataContainer.get(NamespacedKey("az", "player_dropped"), PersistentDataType.STRING)
            u?.let {
                if (p.uniqueId.toString() != it) {
                    event.isCancelled = true
                    return
                }
            }
        }
        meta.persistentDataContainer.remove(NamespacedKey("az", "player_dropped"))
        i.setItemMeta(meta)
        event.item.itemStack = i

    }

    @EventHandler
    suspend fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        plugin.runTask {
            delayTick(30)
            sync {
                Util.removeAttribute(player, Attribute.STEP_HEIGHT, NamespacedKey("az", "default-step-height"))
                setStep(player)
                setWayPoint(player)
                player.updateInventory()

                val list = arrayListOf<ItemStack?>()
                for (item in player.inventory.contents) {
                    var i = item
                    if (item != null && MythicBukkit.inst().itemManager.isMythicItem(item)) {
                        val mmid = MythicBukkit.inst().itemManager.getMythicTypeFromItem(item)
                        val get = MythicBukkit.inst().itemManager.getItemStack(mmid, item.amount)
                        i = get ?: item
                    }
                    list.add(i)
                }
                player.inventory.contents = list.toTypedArray()
            }
            if (player.hasPermission("azurite.command.mode")) {
                ModeCommand.switch(player, egod = false, efly = true, ebypass = false)
            }
            async {
                if (!DBCon.hasSpawn(player.uniqueId)) {
                    DBCon.loadSpawn(player.uniqueId)
                }
            }
            delayTick(5)
            val has =  async {
                if (!DBCon.hasSpawn(player.uniqueId)) {
                    DBCon.setSpawn(player.uniqueId)
                    true
                } else {
                    false
                }
            }
            if (has) {
                Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "kits start " + player.name
                )
            }
        }
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        val player = event.player
        Util.removeAttribute(player, Attribute.STEP_HEIGHT, NamespacedKey("az", "default-step-height"))
        setStep(player)
        setWayPoint(player)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInteractEntity(evnet: PlayerInteractEntityEvent) {
        checkNullMythicEntity(BukkitAdapter.adapt(evnet.rightClicked))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onHeal(event: MythicHealMechanicEvent) {
        val entity = BukkitAdapter.adapt(event.target)
        if (MythicListener.isMythicReloading) return
        MythicTrigger(MythicBukkit.inst().skillManager.getCaster(entity)).triggerHeal()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onHeal(event: EntityRegainHealthEvent) {
        if (event.entity !is Player) return
        val entity = BukkitAdapter.adapt(event.entity)
        if (MythicListener.isMythicReloading) return
        MythicTrigger(MythicBukkit.inst().skillManager.getCaster(entity)).triggerHeal()
    }

    @EventHandler
    fun onArrow(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        if (event.damager !is Arrow) return
        event.isCancelled = true
    }

    private fun checkNullMythicEntity(entity: AbstractEntity?) {
        if (entity == null || entity.bukkitEntity == null) return
        val mob = MythicBukkit.inst().mobManager
        if (!mob.isActiveMob(entity)) {
            Azurite.runLater(entity::remove, 1)
        }
    }

    private fun setStep(player: Player?) {
        if (player == null) return
        Util.addAttribute(
            player,
            Attribute.STEP_HEIGHT,
            AttributeModifier(NamespacedKey("az", "default-step-height"), 0.5, AttributeModifier.Operation.ADD_NUMBER)
        )
    }

    private fun setWayPoint(player: Player?) {
        if (player == null) return
        player.getAttribute(Attribute.WAYPOINT_TRANSMIT_RANGE)?.baseValue = 0.0
    }
}
