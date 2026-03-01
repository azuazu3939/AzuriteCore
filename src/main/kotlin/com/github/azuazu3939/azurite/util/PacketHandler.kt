package com.github.azuazu3939.azurite.util

import com.github.azuazu3939.azurite.Azurite
import com.github.azuazu3939.azurite.util.PacketUtil.getDisguise
import io.lumine.mythic.bukkit.MythicBukkit
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.phys.Vec3
import org.bukkit.Material
import java.util.*
import kotlin.jvm.optionals.getOrNull


class PacketHandler(val azurite: Azurite) : ChannelDuplexHandler() {

    private val entityIdField =
        ClientboundMoveEntityPacket::class.java
            .getDeclaredField("entityId")
            .apply { isAccessible = true }

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg is ServerboundInteractPacket) {
            try {
                if (msg.isAttack) return
            } catch (e: Throwable) {
                if (e is VirtualMachineError) {
                    throw e
                }
                super.channelRead(ctx, msg)
            }
        }

        super.channelRead(ctx, msg)
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {

        if (msg is ClientboundBundlePacket) {

            val newPackets = mutableListOf<Packet<in ClientGamePacketListener>>()

            for (p in msg.subPackets()) {

                if (p is ClientboundAddEntityPacket) {
                    val uuid = p.uuid
                    val op = MythicBukkit.inst().mobManager.getActiveMob(uuid).getOrNull()

                    if (op == null) {
                        newPackets.add(p)
                        continue
                    }

                    val entity = op.entity.bukkitEntity
                    val config = op.type.config
                    val type = config.getString("Packet.Type", null)

                    if (type == null) {
                        newPackets.add(p)
                        continue
                    }

                    val id = entityIdToRandomId.computeIfAbsent(entity.entityId) {
                        val randomId = Random().nextInt(100000) + 1
                        entityIdToUUID[entity.entityId] = entity.uniqueId
                        PacketTimer(entity.uniqueId).runTaskTimer(azurite, 1, 1)
                        randomId
                    }

                    if (type.equals("BlockDisplay", true)) {

                        val spawn = entity.getDisguise(EntityType.BLOCK_DISPLAY, id)
                        newPackets.add(spawn)
                        continue
                    }
                    newPackets.add(p)
                }

                if (p is ClientboundSetEntityDataPacket) {

                    val uuid = entityIdToUUID[p.id]
                    if (uuid == null) {
                        newPackets.add(p)
                        continue
                    }

                    val op = MythicBukkit.inst().mobManager.getActiveMob(uuid).getOrNull()
                    if (op == null) {
                        newPackets.add(p)
                        continue
                    }

                    val entity = op.entity.bukkitEntity
                    val config = op.type.config
                    val type = config.getString("Packet.Type", null)

                    if (type == null) {
                        newPackets.add(p)
                        continue
                    }

                    val id = entityIdToRandomId.computeIfAbsent(p.id) {
                        val randomId = Random().nextInt(100000) + 1
                        entityIdToUUID[p.id] = entity.uniqueId
                        PacketTimer(entity.uniqueId).runTaskTimer(azurite, 1, 1)
                        randomId
                    }

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

                    newPackets.add(PacketUtil.getBlockDisplayMeta(id, blockData, scale))
                    continue
                }
                newPackets.add(p)
            }

            return super.write(ctx, ClientboundBundlePacket(newPackets), promise)
        }

        if (msg is ClientboundMoveEntityPacket) {
            val id = entityIdField.getInt(msg)
            val randomId = entityIdToRandomId[id] ?: return super.write(ctx, msg, promise)

            val base = basePos[id] ?: return super.write(ctx, msg, promise)
            val (yaw, pitch) = baseRot[id] ?: (0f to 0f)

            val change = PositionMoveRotation(
                base,
                Vec3.ZERO,
                yaw,
                pitch
            )

            val teleport = ClientboundTeleportEntityPacket(
                randomId,
                change,
                emptySet(),
                msg.isOnGround
            )
            super.write(ctx, teleport, promise)
        }


        if (msg is ClientboundTeleportEntityPacket) {
            val entityId = msg.id
            val randomId = entityIdToRandomId[entityId]
                ?: return super.write(ctx, msg, promise)

            val pos = basePos[entityId]
                ?: return super.write(ctx, msg, promise)

            val (yaw, pitch) = baseRot[entityId]
                ?: (0f to 0f)

            val change = PositionMoveRotation(
                pos,
                Vec3.ZERO,
                yaw,
                pitch
            )

            val teleport = ClientboundTeleportEntityPacket(
                randomId,
                change,
                emptySet(),
                msg.onGround
            )
            super.write(ctx, teleport, promise)
        }

        return super.write(ctx, msg, promise)
    }

    companion object {

        val entityIdToRandomId = mutableMapOf<Int, Int>()
        val entityIdToUUID = mutableMapOf<Int, UUID>()
        val basePos = mutableMapOf<Int, Vec3>()
        val baseRot = mutableMapOf<Int, Pair<Float, Float>>()

    }
}
