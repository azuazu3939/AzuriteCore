package com.github.azuazu3939.azurite.mythic.mechanic

import io.lumine.mythic.api.adapters.AbstractLocation
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedLocationSkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.placeholders.PlaceholderBoolean
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.Location
import org.bukkit.Material
import java.io.File

class RemoveMask(manager: SkillExecutor, file: File, line: String, mlc: MythicLineConfig?) : SkillMechanic(manager, file, line, mlc), ITargetedLocationSkill {
    private val radius: PlaceholderInt
    private val noAir: PlaceholderBoolean
    private val shape : PlaceholderString

    init {
        this.threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
        val radius = mlc?.getPlaceholderInteger(arrayOf("r", "radius"), 0)!!
        val shape = mlc.getPlaceholderString(arrayOf("a", "shape"), "SPHERE")

        val noAir = mlc.getPlaceholderBoolean(arrayOf("noair", "na"), true)
        this.radius = radius
        this.shape = shape
        this.noAir = noAir
    }

    private fun playEffect(loc: AbstractLocation, metadata: SkillMetadata) {
        val l = BukkitAdapter.adapt(loc)
        if (radius.get(metadata) == 0) {
            for (p in l.world.players) {
                p.sendBlockChange(l, l.block.blockData)
            }
        } else {
            for (ll in getBlocksInRadius(l, metadata)) {
                for (p in l.world.players) {
                    p.sendBlockChange(ll, ll.block.blockData)
                }
            }
        }
    }

    private fun getBlocksInRadius(l : Location, metadata: SkillMetadata) : List<Location> {
        val array = arrayListOf<Location>()
        val r = radius.get(metadata)
        val sphere = shape.get(metadata).equals("sphere", true)
        val air = noAir.get(metadata)
        val req = r * r
        for (x in -r..r ) {
            for (z in -r..r) {
                for (y in -r..r) {
                    val newLoc = Location(l.world, l.x + x, l.y + y, + l.z + z)
                    if (!sphere || !(l.distanceSquared(newLoc) > req)) {
                        if (air && newLoc.block.type != Material.AIR) {
                            array.add(newLoc)
                        } else if (!air) {
                            array.add(newLoc)
                        }
                    }
                }
            }
        }
        return array
    }

    override fun castAtLocation(p0: SkillMetadata?, p1: AbstractLocation?): SkillResult {
        if (p1 != null && p0 != null) {
            playEffect(p1, p0)
        }
        return SkillResult.SUCCESS
    }
}