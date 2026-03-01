package com.github.azuazu3939.azurite.util

import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class PacketTimer(val uuid: UUID) : BukkitRunnable() {

    override fun run() {
       val entity = Bukkit.getEntity(uuid)
        if (entity != null) {
            val yawRad = Math.toRadians(entity.location.yaw.toDouble())
            val offX = 0.5 * cos(yawRad) - 0.5 * sin(yawRad)
            val offZ = 0.5 * sin(yawRad) + 0.5 * cos(yawRad)

            PacketHandler.basePos[entity.entityId] =
                Vec3(entity.location.x - offX, entity.location.y, entity.location.z - offZ)

            PacketHandler.baseRot[entity.entityId] =
                Pair(entity.location.yaw, entity.location.pitch)
        } else {
            cancel()
        }
    }
}