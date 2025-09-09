package com.github.azuazu3939.azurite.listener

import com.github.azuazu3939.azurite.Azurite
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.block.Hopper
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

class SummonListener : Listener {

    companion object {
        private val bossHopper = hashMapOf<Location, UUID>()
        private val eventNow = hashSetOf<Location>()
    }

    @EventHandler
    fun onHopper(event: InventoryPickupItemEvent) {
        val hopper = event.inventory.holder as? Hopper ?: return
        val dataContainer = hopper.persistentDataContainer

        val mobKey = NamespacedKey("az", "hopper_mob")
        val itemKey = NamespacedKey("az", "hopper_item")

        val mobId = dataContainer.get(mobKey, PersistentDataType.STRING)
        val itemData = dataContainer.get(itemKey, PersistentDataType.STRING)

        if (mobId == null || itemData == null) {
            event.isCancelled = true
            return
        }

        val stack = event.item.itemStack
        val mythicItemId = MythicBukkit.inst().itemManager.getMythicTypeFromItem(stack)
            ?: run {
                event.isCancelled = true
                return
            }

        val (expectedItemId, requiredAmount) = itemData.split(" ").let {
            it[0] to it.getOrNull(1)?.toIntOrNull()
        }

        if (expectedItemId != mythicItemId || requiredAmount == null) {
            event.isCancelled = true
            return
        }

        // MythicMob が存在しない場合
        val mobOpt = MythicBukkit.inst().mobManager.getMythicMob(mobId)
        if (mobOpt.isEmpty) {
            event.isCancelled = true
            return
        }

        // Boss 存在や処理中チェック
        bossHopper[hopper.location]?.let { uuid ->
            if (Bukkit.getEntity(uuid) != null) {
                event.isCancelled = true
                return
            } else {
                bossHopper.remove(hopper.location)
            }
        }
        if (eventNow.contains(hopper.location)) {
            event.isCancelled = true
            return
        }

        // Hopper内の合計を直接計算（同じ Material のみ）
        val totalAmount = hopper.inventory
            .filterNotNull()
            .filter { expectedItemId == MythicBukkit.inst().itemManager.getMythicTypeFromItem(it) }
            .sumOf { it.amount }
            .plus(stack.amount)

        if (totalAmount >= requiredAmount &&
            !MythicListener.isReloading &&
            !MythicListener.isMythicReloading
        ) {
            eventNow.add(hopper.location)
            hopper.inventory.clear()
            event.isCancelled = true

            val remainder = totalAmount - requiredAmount
            if (remainder == 0) {
                event.item.remove()
            } else {
                stack.amount = remainder
                event.item.itemStack = stack.clone()
            }

            hopper.world.playSound(hopper.location, Sound.BLOCK_END_GATEWAY_SPAWN, 1f, 1f)

            Azurite.runLater({
                hopper.world.playSound(hopper.location, Sound.BLOCK_PORTAL_TRAVEL, 1f, 1f)
            }, 15)

            Azurite.runLater({
                val bossId = MythicBukkit.inst().apiHelper.spawnMythicMob(
                    mobId,
                    hopper.location.add(0.0, 1.0, 0.0)
                ).uniqueId
                bossHopper[hopper.location] = bossId
                eventNow.remove(hopper.location)
            }, 100)
        }

        // ドロップ主処理
        val dropperKey = NamespacedKey("az", "player_dropped")
        if (stack.persistentDataContainer.has(dropperKey, PersistentDataType.STRING)) {
            val uuidStr = stack.persistentDataContainer.get(dropperKey, PersistentDataType.STRING)
            val player = uuidStr?.let { Bukkit.getPlayer(UUID.fromString(it)) } ?: return

            repeat(stack.amount) {
                MythicBukkit.inst().apiHelper.castSkill(player, "${mobId}_SpawnDrop")
            }

            stack.itemMeta = stack.itemMeta.apply {
                persistentDataContainer.remove(dropperKey)
            }
        }
    }

}
