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
import kotlin.math.sqrt
import kotlin.text.isEmpty
import kotlin.text.split

class AzuriteRingTargeter(manager: SkillExecutor, mlc: MythicLineConfig) : IEntitySelector(manager, mlc) {

    private var angle: PlaceholderAngle? = null
    private var range: PlaceholderDouble? = null
    private var thickness: PlaceholderDouble? = null
    private var rotation: PlaceholderDouble? = null
    private var yOffset: PlaceholderDouble? = null
    private var height: PlaceholderDouble? = null
    private var livingOnly: Boolean = true

    private var world: PlaceholderString? = null
    private var x: PlaceholderDouble? = null
    private var y: PlaceholderDouble? = null
    private var z: PlaceholderDouble? = null
    private var yaw: PlaceholderDouble? = null

    private var mlc: MythicLineConfig? = null

    init {
        this.angle = mlc.getPlaceholderAngle(arrayOf("angle", "a"), AngleUnit.DEGREES, "360")
        this.range = mlc.getPlaceholderDouble(arrayOf("range", "r"), 16.0)
        this.thickness = mlc.getPlaceholderDouble(arrayOf("thickness", "t"), 3.0)
        this.rotation = mlc.getPlaceholderDouble(arrayOf("rotation", "rot"), 0.0)
        this.yOffset = mlc.getPlaceholderDouble(arrayOf("yoffset", "yo"), 0.0)
        this.height = mlc.getPlaceholderDouble(arrayOf("height", "h"), 9.0)
        this.livingOnly = mlc.getBoolean(arrayOf("livingonly", "lo"), true)

        this.world = mlc.getPlaceholderString(arrayOf("world", "w"), null)
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
        }
    }

    override fun getEntities(metadata: SkillMetadata?): Collection<AbstractEntity> {
        if (metadata == null) return emptyList()
        initLocation(metadata)

        val casterId = metadata.caster.entity.uniqueId

        val angleDeg = angle!!.get(metadata).degrees
        val rangeVal = range!!.get(metadata)
        val thicknessVal = thickness!!.get(metadata)
        val rotationVal = rotation!!.get(metadata)
        val yOffsetVal = yOffset!!.get(metadata)
        val heightVal = height!!.get(metadata)

        val xVal = x!!.get(metadata)
        val yVal = y!!.get(metadata)
        val zVal = z!!.get(metadata)
        val yawVal = yaw!!.get(metadata)

        val outerRadiusSq = rangeVal * rangeVal
        val innerRadius = (rangeVal - thicknessVal).coerceAtLeast(0.0)
        val innerRadiusSq = innerRadius * innerRadius
        val halfHeight = heightVal * 0.5

        val world = if (world == null)
            metadata.origin.world
        else
            MythicBukkit.inst().bootstrap.getWorld(world!!.get(metadata))

        val origin = MythicBukkit.inst().bootstrap
            .newLocation(world, xVal, yVal + yOffsetVal, zVal)

        // ===== 方向ベクトル（3D対応） =====
        val finalYaw = Math.toRadians(yawVal + rotationVal)
        val pitchRad = Math.toRadians(metadata.origin.pitch.toDouble())

        val dirX = -sin(finalYaw) * cos(pitchRad)
        val dirY = -sin(pitchRad)
        val dirZ = cos(finalYaw) * cos(pitchRad)

        val cosHalfAngle = cos(Math.toRadians(angleDeg * 0.5))

        val useDirection = angleDeg < 360.0

        val filter = Predicate<AbstractEntity> { target ->
            if (target.uniqueId == casterId) return@Predicate false
            if (livingOnly && !target.isLiving) return@Predicate false

            val loc = target.location
            val dx = loc.x - origin.x
            val dy = loc.y - origin.y
            val dz = loc.z - origin.z

            val horizontalDistSq = dx * dx + dz * dz
            if (horizontalDistSq > outerRadiusSq) return@Predicate false
            if (horizontalDistSq < innerRadiusSq) return@Predicate false
            if (abs(dy) > halfHeight) return@Predicate false

            if (!useDirection) return@Predicate true

            val length = sqrt(dx * dx + dy * dy + dz * dz)
            if (length == 0.0) return@Predicate false

            val dot = (dx * dirX + dy * dirY + dz * dirZ) / length

            dot >= cosHalfAngle
        }

        return origin.world.getEntitiesNearLocation(origin, rangeVal, filter)
    }
}