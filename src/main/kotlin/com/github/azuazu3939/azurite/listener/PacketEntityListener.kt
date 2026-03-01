package com.github.azuazu3939.azurite.listener

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent
import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.util.PacketHandler.Companion.basePos
import com.github.azuazu3939.azurite.util.PacketHandler.Companion.baseRot
import com.github.azuazu3939.azurite.util.PacketHandler.Companion.entityIdToRandomId
import com.github.azuazu3939.azurite.util.PacketHandler.Companion.entityIdToUUID
import com.github.azuazu3939.azurite.util.PacketTimer
import com.github.azuazu3939.azurite.util.PacketUtil
import com.github.azuazu3939.azurite.util.PacketUtil.getDisguise
import com.github.azuazu3939.azurite.util.PacketUtil.removeEntity
import com.github.azuazu3939.azurite.util.PacketUtil.sendPacket
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent
import net.minecraft.world.entity.EntityType
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityRemoveEvent
import java.util.*

class PacketEntityListener(val azurite: Azurite) : Listener {

    @EventHandler
    fun onRemove(event: EntityRemoveEvent) {
        remove(event.entity)
    }

    @EventHandler
    fun onRemoveEntity(event: EntityRemoveFromWorldEvent) {
        remove(event.entity)
    }

    @EventHandler
    fun onDeath(event: EntityDeathEvent) {
        remove(event.entity)
    }

    @EventHandler
    fun onDespawn(event: MythicMobDespawnEvent) {
        remove(event.entity)
    }

    @EventHandler
    fun onDeath(event: MythicMobDeathEvent) {
        remove(event.entity)
    }

    @EventHandler
    fun onSpawn(event: MythicMobSpawnEvent) {
        val config = event.mobType.config
        val type = config.getString("Packet.Type", null) ?: return
        val entity = event.entity

        if (type.equals("BlockDisplay", true)) {

            entity.world.players.forEach { player -> player.removeEntity(entity.entityId) } //元モブ消す
            val id = Random().nextInt(100000) + 1

            entityIdToRandomId[entity.entityId] = id
            entityIdToUUID[entity.entityId] = entity.uniqueId

            PacketTimer(entity.uniqueId).runTaskTimer(azurite, 1, 1)

            val spawn = entity.getDisguise(EntityType.BLOCK_DISPLAY, id)
            entity.world.players.forEach { player -> player.sendPacket(spawn) } //Display出す

            val matId = config.getString("Packet.Material", "STONE")!!
            val blockData = try {
                Material.valueOf(matId.uppercase()).createBlockData()
            } catch (_: Exception) {
                Material.STONE.createBlockData()
            }

            var scale = Triple(1.0f, 1.0f, 1.0f)
            if (config.getString("Packet.Scale") != null) {
                try {
                    val s = config.getString("Packet.Scale").split(",")
                    if (s.size == 3) {
                        scale = Triple(s[0].toFloat(), s[1].toFloat(), s[1].toFloat())
                    }
                } catch (_: Exception) {}
            }

            val meta = PacketUtil.getBlockDisplayMeta(id, blockData, scale)
            entity.world.players.forEach { player -> player.sendPacket(meta) } //Display設定適応
            return
        }

        // ==========================
        // ItemDisplay 置き換え
        // ==========================
        if (type.equals("ItemDisplay", true)) {

            entity.world.players.forEach { player -> player.removeEntity(entity.entityId) } //元モブ消す
            val id = Random().nextInt(100000) + 1

            val spawn = entity.getDisguise(EntityType.ITEM_DISPLAY, id)
            entity.world.players.forEach { player -> player.sendPacket(spawn) } //Display出す
            return
        }
    }

    private fun remove(entity: Entity) {
        val randomId = entityIdToRandomId[entity.entityId] ?: return
        entity.world.players.forEach { it.removeEntity(randomId) }
        entityIdToRandomId.remove(entity.entityId)
        entityIdToUUID.remove(entity.entityId)
        basePos.remove(randomId)
        baseRot.remove(randomId)
    }
}
