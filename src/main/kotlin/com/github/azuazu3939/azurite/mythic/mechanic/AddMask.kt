package com.github.azuazu3939.azurite.mythic.mechanic

import com.github.azuazu3939.azurite.util.MaskBatcher
import com.github.azuazu3939.azurite.util.Shapes
import io.lumine.mythic.api.adapters.AbstractBlock
import io.lumine.mythic.api.adapters.AbstractLocation
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedLocationSkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.utils.Schedulers
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.util.Vector
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class AddMask(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig?
) : SkillMechanic(manager, file, line, mlc), ITargetedLocationSkill {

    /* =========================================================
       Config
       ========================================================= */

    private val radius = mlc!!.getPlaceholderInteger(arrayOf("r","radius"),3)
    private val height = mlc!!.getPlaceholderInteger(arrayOf("h","height"),15)
    private val thickness = mlc!!.getPlaceholderInteger(arrayOf("t","thickness"),1)
    private val angle = mlc!!.getPlaceholderInteger(arrayOf("angle"),45)
    private val shape = mlc!!.getPlaceholderString(arrayOf("shape"),"sphere")
    private val duration = mlc!!.getPlaceholderInteger(arrayOf("duration","d"),40)
    private val noAir = mlc!!.getPlaceholderBoolean(arrayOf("noair","na"),true)
    private val passthrough = mlc!!.getPlaceholderBoolean(arrayOf("passthrough","pass"),true)
    private val async = mlc!!.getPlaceholderBoolean(arrayOf("async"),true)
    private val audience = mlc!!.getAudience(arrayOf("audience"),"nearby")
    private val rotation = mlc!!.getPlaceholderInteger(arrayOf("rotation","rot"),0)

    /* =========================================================
       Packed block key
       ========================================================= */

    companion object {

        fun pack(loc: Location): Long {
            val w = loc.world.uid.mostSignificantBits.toInt()
            val cx = loc.blockX shr 4
            val cz = loc.blockZ shr 4
            val lx = loc.blockX and 15
            val lz = loc.blockZ and 15
            val y = loc.blockY and 255

            return (w.toLong() shl 48) or
                    ((cx and 0xFFFF).toLong() shl 32) or
                    ((cz and 0xFFFF).toLong() shl 16) or
                    ((y and 0xFF).toLong() shl 8) or
                    ((lx and 0xF).toLong() shl 4) or
                    (lz and 0xF).toLong()
        }

        /** AoE stack count holder */
        val maskHolder = ConcurrentHashMap<Long, Int>()
    }

    /* =========================================================
       AoE stack → color mapping
       ========================================================= */

    private fun blockForCount(count:Int): AbstractBlock {

        val mat = when {
            count <= 0 -> Material.AIR
            count == 1 -> Material.RED_TERRACOTTA
            count == 2 -> Material.ORANGE_TERRACOTTA
            count == 3 -> Material.YELLOW_TERRACOTTA
            count == 4 -> Material.PINK_TERRACOTTA
            count == 5 -> Material.MAGENTA_TERRACOTTA
            else -> Material.PURPLE_TERRACOTTA
        }

        return MythicBukkit.inst().bootstrap.getBlock(mat.name)
    }

    /* =========================================================
       Entry
       ========================================================= */

    override fun castAtLocation(
        metadata: SkillMetadata?,
        location: AbstractLocation?
    ): SkillResult {

        if (metadata==null || location==null) return SkillResult.SUCCESS

        val delayTicks = delay.get(metadata)

        if (delayTicks>0) {
            Schedulers.of(metadata.caster.entity.bukkitEntity).runLater({
                runMask(location, metadata)
            }, delayTicks.toLong())
        } else {
            runMask(location, metadata)
        }

        return SkillResult.SUCCESS
    }

    /* =========================================================
       Main
       ========================================================= */

    private fun runMask(center: AbstractLocation, metadata: SkillMetadata) {

        val audiencePlayers = audience.get(metadata, null)
        val centerBukkit = BukkitAdapter.adapt(center)
        val durationTicks = duration.get(metadata)
        val shapeName = shape.get(metadata).lowercase()

        val r = radius.get(metadata)
        val h = height.get(metadata)
        val t = thickness.get(metadata)
        val ang = angle.get(metadata).toDouble()
        val rot = rotation.get(metadata).toDouble()

        val dir = metadata.caster.entity.bukkitEntity.location.direction

        val generateVectors = {
            when(shapeName) {
                "sphere" -> Shapes.sphere(r, h)
                "cylinder" -> Shapes.cylinder(r, h)
                "ring" -> Shapes.ring(r, h, t)
                "cone" -> Shapes.cone(dir, r.toDouble(), ang, rot, h)
                else -> Shapes.sphere(r, h)
            }
        }

        val applyMask = { vecs: List<Vector> ->

            val updateMap = HashMap<AbstractLocation, AbstractBlock?>(vecs.size)
            val revertKeys = ArrayList<Long>(vecs.size)
            val revertLocs = ArrayList<AbstractLocation>(vecs.size)

            val ox = centerBukkit.blockX
            val oy = centerBukkit.blockY
            val oz = centerBukkit.blockZ
            val world = centerBukkit.world

            for (v in vecs) {

                val bx = ox + v.blockX
                val by = oy + v.blockY
                val bz = oz + v.blockZ

                val loc = Location(world, bx.toDouble(), by.toDouble(), bz.toDouble())
                val block = loc.block

                if ((!block.isSolid && !block.isPassable) || block.isLiquid) continue
                if (noAir.get(metadata) && block.type == Material.AIR) continue

                val key = pack(loc)
                val abs = BukkitAdapter.adapt(loc)

                val newCount = maskHolder.merge(key, 1) { a, b -> a + b }!!

                val newBlock =
                    if (block.isSolid) blockForCount(newCount)
                    else MythicBukkit.inst().bootstrap.getBlock(Material.AIR.name)

                updateMap[abs] = newBlock
                revertKeys += key
                revertLocs += abs
            }

            if (updateMap.isNotEmpty())
                MaskBatcher.queue(audiencePlayers, updateMap)

            if (durationTicks > 0 && revertKeys.isNotEmpty()) {

                Schedulers.of(metadata.caster.entity.bukkitEntity).runLater({

                    if (!centerBukkit.chunk.isLoaded) return@runLater

                    val revertMap = HashMap<AbstractLocation, AbstractBlock?>(revertKeys.size)

                    for (i in revertKeys.indices) {

                        val key = revertKeys[i]
                        val abs = revertLocs[i]

                        val newCount = maskHolder.compute(key) { _, v ->
                            when {
                                v == null -> null
                                v <= 1 -> null
                                else -> v - 1
                            }
                        }

                        val bukkitLoc = BukkitAdapter.adapt(abs)
                        val currentBlock = bukkitLoc.block

                        val newBlock =
                            if (newCount == null) {
                                BukkitAdapter.adapt(currentBlock)
                            } else {
                                if (currentBlock.isSolid)
                                    blockForCount(newCount)
                                else
                                    MythicBukkit.inst().bootstrap.getBlock(Material.AIR.name)
                            }

                        revertMap[abs] = newBlock
                    }

                    if (revertMap.isNotEmpty())
                        MaskBatcher.queue(audiencePlayers, revertMap)

                }, durationTicks.toLong())
            }
        }

        if (async.get(metadata)) {
            Schedulers.async().run {
                val vecs = generateVectors()
                Schedulers.sync().run { applyMask(vecs) }
            }
        } else {
            applyMask(generateVectors())
        }
    }
}