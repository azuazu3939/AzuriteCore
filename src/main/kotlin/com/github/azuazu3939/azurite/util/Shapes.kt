package com.github.azuazu3939.azurite.util

import org.bukkit.util.Vector
import kotlin.math.*

object Shapes {

    /* =========================================================
       SPHERE
       ========================================================= */

    fun sphere(r: Int, h: Int): List<Vector> {

        val r2 = r * r
        val halfH = h / 2
        val list = ArrayList<Vector>((r * r * r * 0.5).toInt())

        for (x in -r..r) {
            val x2 = x * x
            if (x2 > r2) continue

            for (y in -halfH..halfH) {
                val y2 = y * y
                val xy2 = x2 + y2
                if (xy2 > r2) continue

                val maxZ = sqrt((r2 - xy2).toDouble()).toInt()

                for (z in -maxZ..maxZ) {
                    list += Vector(x, y, z)
                }
            }
        }

        return list
    }

    /* =========================================================
       CYLINDER
       ========================================================= */

    fun cylinder(r: Int, h: Int): List<Vector> {

        val r2 = r * r
        val list = ArrayList<Vector>((r * r * h * 0.8).toInt())

        for (x in -r..r) {
            val x2 = x * x
            if (x2 > r2) continue

            val maxZ = sqrt((r2 - x2).toDouble()).toInt()

            for (z in -maxZ..maxZ)
                for (y in 0 until h)
                    list += Vector(x, y, z)
        }
        return list
    }

    /* =========================================================
       RING
       ========================================================= */

    fun ring(r: Int, ry: Int, t: Int): List<Vector> {

        val outer = r * r
        val inner = (r - t) * (r - t)
        val halfH = ry / 2

        val list = ArrayList<Vector>((r * r * ry * 0.6).toInt())

        for (x in -r..r) {
            val x2 = x * x
            if (x2 > outer) continue

            val maxZ = sqrt((outer - x2).toDouble()).toInt()

            for (z in -maxZ..maxZ) {
                val d = x2 + z * z
                if (d < inner) continue

                for (y in -halfH..halfH)
                    list += Vector(x, y, z)
            }
        }
        return list
    }

    /* =========================================================
       CONE（扇柱）
       ========================================================= */

    fun cone(
        dir: Vector,
        len: Double,
        angle: Double,
        rotationYaw: Double = 0.0,
        height: Int = 1
    ): List<Vector> {

        val il = ceil(len).toInt()
        val lenSq = len * len

        val base = dir.clone().setY(0.0).normalize()

        val rad = Math.toRadians(rotationYaw)
        val sinR = sin(rad)
        val cosR = cos(rad)

        val dirX = base.x * cosR - base.z * sinR
        val dirZ = base.x * sinR + base.z * cosR

        val cosA = cos(Math.toRadians(angle))
        val cosSq = cosA * cosA

        val halfH = height / 2

        val list = ArrayList<Vector>((len * len * height * 0.5).toInt())

        for (x in -il..il) {

            val x2 = x * x
            if (x2 > lenSq) continue

            val maxZ = sqrt((lenSq - x2)).toInt()

            for (z in -maxZ..maxZ) {

                val distSq = x2 + z * z
                if (distSq == 0) continue

                val dot = x * dirX + z * dirZ
                if (dot <= 0) continue
                if (dot * dot < distSq * cosSq) continue

                for (y in -halfH..halfH)
                    list += Vector(x, y, z)
            }
        }
        return list
    }
}