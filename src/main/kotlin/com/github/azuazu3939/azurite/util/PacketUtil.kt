package com.github.azuazu3939.azurite.util

import com.github.azuazu3939.azurite.Azurite
import io.netty.channel.Channel
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID

object PacketUtil {
    private const val NAME = "azurite"

    fun inject(player: Player, azurite: Azurite) {
        val pipe = getChannel(player).pipeline()
        if (pipe.get(NAME) != null) return

        pipe.addBefore("packet_handler", NAME, PacketHandler(azurite))
    }

    fun eject(player: Player) {
        val pipe = getChannel(player).pipeline()
        if (pipe.get(NAME) != null) {
            pipe.remove(NAME)
        }
    }

    fun getChannel(player: Player): Channel {
        return (player as CraftPlayer).handle.connection.connection.channel
    }

    fun Player.sendPacket(packet: Packet<*>) {
        (this as CraftPlayer).handle.connection.connection.send(packet)
    }

    fun Entity.getDisguise(type: EntityType<*>, id: Int): ClientboundAddEntityPacket {
        val l = location

        return ClientboundAddEntityPacket(
            id,
            uniqueId,
            l.x,
            l.y,
            l.z,
            0f,      // yaw 無効化
            0f,      // pitch 無効化
            type,
            0,       // dataは0でOK（ただし後でmetadata必須）
            Vec3.ZERO,
            0.0
        )
    }

    fun Player.spawnDisplay(type: EntityType<*>, vector: Vector, id: Int) {
        val packet = ClientboundAddEntityPacket(id, UUID.randomUUID(), vector.x, vector.y, vector.z, 0F, 0F, type, 0, Vec3.ZERO, 0.0)
        this.sendPacket(packet)
    }

    fun Player.removeEntity(id: Int){
        this.sendPacket(ClientboundRemoveEntitiesPacket(id))
    }

    fun Player.displayMeta(id: Int, text: Component) {
        val net = PaperAdventure.asVanilla(text)
        val list = arrayListOf<SynchedEntityData.DataValue<*>>()
        list += (SynchedEntityData.DataValue.create(EntityDataSerializers.COMPONENT.createAccessor(23), net))
        list += (SynchedEntityData.DataValue.create(EntityDataSerializers.BYTE.createAccessor(15), 3.toByte()))
        list += (SynchedEntityData.DataValue.create(EntityDataSerializers.BYTE.createAccessor(26), 255.toByte()))

        this.sendPacket(ClientboundSetEntityDataPacket(id, list))
    }


    fun getBlockDisplayMeta(entityId: Int, data: BlockData, scale: Triple<Float, Float, Float>): ClientboundSetEntityDataPacket {

        val values = mutableListOf<SynchedEntityData.DataValue<*>>()

        // Transformation interpolation duration
        values += SynchedEntityData.DataValue(
            9,
            EntityDataSerializers.INT,
            5
        )

        // Position/Rotation interpolation duration
        values += SynchedEntityData.DataValue(
            10,
            EntityDataSerializers.INT,
            5
        )

        // index 11 translation
        values += SynchedEntityData.DataValue(
            11,
            EntityDataSerializers.VECTOR3,
            Vector3f(0f,0f,0f)
        )

        // index 12 scale
        values += SynchedEntityData.DataValue(
            12,
            EntityDataSerializers.VECTOR3,
            Vector3f(scale.first,scale.second,scale.third)
        )

        // index 13 left rotation
        values += SynchedEntityData.DataValue(
            13,
            EntityDataSerializers.QUATERNION,
            Quaternionf(0f,0f,0f,1f)
        )

        // index 14 right rotation
        values += SynchedEntityData.DataValue(
            14,
            EntityDataSerializers.QUATERNION,
            Quaternionf(0f,0f,0f,1f)
        )

        /*
        (15 << 4) | (15 << 20)
        = 240 | 15728640
        = 15728880
        */

        values += SynchedEntityData.DataValue(
            16,
            EntityDataSerializers.INT,
            15728880
        )

        // index 16 block state (BlockDisplay固有)
        values += SynchedEntityData.DataValue(
            23,
            EntityDataSerializers.BLOCK_STATE,
            (data as CraftBlockData).state
        )

        return ClientboundSetEntityDataPacket(entityId, values)
    }
}