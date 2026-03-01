package com.github.azuazu3939.azurite.mythic.targeter

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.placeholders.PlaceholderAngle
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble
import io.lumine.mythic.api.skills.placeholders.PlaceholderString
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.bukkit.utils.numbers.AngleUnit
import io.lumine.mythic.core.logging.MythicLogger
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.targeters.IEntitySelector
import java.util.function.Predicate
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class AzuriteConeTargeter(manager: SkillExecutor, mlc: MythicLineConfig) : IEntitySelector(manager, mlc) {

    private var angle: PlaceholderAngle? = null
    private var range: PlaceholderDouble? = null
    private var rotation: PlaceholderDouble? = null
    private var yOffset: PlaceholderDouble? = null
    private var height: PlaceholderDouble? = null
    private var livingOnly: Boolean = true

    private var world: PlaceholderString? = null
    private var x: PlaceholderDouble? = null
    private var y: PlaceholderDouble? = null
    private var z: PlaceholderDouble? = null
    private var yaw: PlaceholderDouble? = null
    private var pitch: PlaceholderDouble? = null

    private var mlc: MythicLineConfig? = null

    init {
        this.angle = mlc.getPlaceholderAngle(arrayOf<String>("angle", "a"), AngleUnit.DEGREES, "90")
        this.range = mlc.getPlaceholderDouble(arrayOf<String>("range", "r"), 16.0, *arrayOfNulls<String>(0))
        this.yOffset = mlc.getPlaceholderDouble(arrayOf<String>("yoffset", "yo"), 0.0, *arrayOfNulls<String>(0))
        this.height = mlc.getPlaceholderDouble(arrayOf<String>("height", "h"), 9.0, *arrayOfNulls<String>(0))
        this.rotation = mlc.getPlaceholderDouble(arrayOf<String>("rotation", "rot"), 0.0, *arrayOfNulls<String>(0))
        this.livingOnly = mlc.getBoolean(arrayOf<String>("livingonly", "lo"), true)

        this.world = mlc.getPlaceholderString(arrayOf<String>("world", "w"), null as String?, *arrayOfNulls<String>(0))
        this.mlc = mlc
    }

    private fun initLocation(metadata: SkillMetadata) {
        val coords = mlc!!.getPlaceholderString(arrayOf("location", "loc", "l"), null as String?, *arrayOfNulls<String>(0)).get(metadata)
        if (coords != null) {
            val split: Array<String?> = coords.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            try {
                this.x = PlaceholderDouble.of(split[0])
                this.y = PlaceholderDouble.of(split[1])
                this.z = PlaceholderDouble.of(split[2])
                if (split.size > 3) {
                    this.yaw = PlaceholderDouble.of(split[3])
                } else {
                    this.yaw = PlaceholderDouble.of("0")
                }

                if (split.size > 4) {
                    this.pitch = PlaceholderDouble.of(split[4])
                } else {
                    this.pitch = PlaceholderDouble.of("0")
                }
            } catch (var6: Exception) {
                MythicLogger.errorTargeterConfig(
                    this,
                    mlc,
                    "The 'location' attribute is required and must be in the format l=x,y,z."
                )
            }
        } else {
            this.x = mlc!!.getPlaceholderDouble("x", "0")
            this.y = mlc!!.getPlaceholderDouble("y", "0")
            this.z = mlc!!.getPlaceholderDouble("z", "0")
            this.yaw = mlc!!.getPlaceholderDouble("yaw", "0")
            this.pitch = mlc!!.getPlaceholderDouble("pitch", "0")
        }
    }

    override fun getEntities(metadata: SkillMetadata?): Collection<AbstractEntity> {
        if (metadata == null) return emptyList()
        initLocation(metadata)

        val casterId = metadata.caster.entity.uniqueId

        val angleHalf = angle!!.get(metadata).degrees * 0.5
        val rangeVal = range!!.get(metadata)
        val yOffsetVal = yOffset!!.get(metadata)
        val heightVal = height!!.get(metadata)
        val rotationVal = rotation!!.get(metadata)

        val xVal = x!!.get(metadata)
        val yVal = y!!.get(metadata)
        val zVal = z!!.get(metadata)
        val pitchVal = pitch!!.get(metadata).toFloat()
        val yawVal = yaw!!.get(metadata).toFloat()

        val radiusSq = rangeVal * rangeVal
        val cosA = cos(Math.toRadians(angleHalf))
        val cosSq = cosA * cosA

        val world = if (world == null) metadata.origin.world else MythicBukkit.inst().bootstrap.getWorld(world!!.get(metadata))
        val origin = MythicBukkit.inst().bootstrap.newLocation(world, xVal, yVal + yOffsetVal , zVal, pitchVal, yawVal)

        val finalYaw = yawVal + rotationVal
        val yawRad = Math.toRadians(finalYaw)
        val dirX = -sin(yawRad)
        val dirZ = cos(yawRad)

        val pitchRad = Math.toRadians(pitchVal.toDouble())
        val tanPitch = kotlin.math.tan(pitchRad)

        val halfHeight = heightVal * 0.5

        val filter = Predicate<AbstractEntity> { target ->
            if (target.uniqueId == casterId) return@Predicate false
            if (livingOnly && !target.isLiving) return@Predicate false

            val loc = target.location
            val dx = loc.x - origin.x
            val dz = loc.z - origin.z

            val forward = dx * dirX + dz * dirZ
            if (forward <= 0.0) return@Predicate false

            val distSq = dx * dx + dz * dz
            if (distSq > radiusSq || distSq == 0.0) return@Predicate false

            // ← ここがpitch補正
            val expectedY = origin.y + forward * tanPitch
            val dy = loc.y - expectedY

            if (abs(dy) > halfHeight) return@Predicate false

            (forward * forward) >= (distSq * cosSq)
        }

        return origin.world.getEntitiesNearLocation(origin, rangeVal, filter)
    }
}