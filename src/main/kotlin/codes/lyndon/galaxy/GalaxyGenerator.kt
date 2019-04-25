package codes.lyndon.galaxy

import org.apache.commons.math3.geometry.euclidean.threed.Rotation
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomDataGenerator
import java.awt.Color
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.text.StyleConstants.setIcon
import javax.swing.JLabel
import java.awt.FlowLayout
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.ImageIcon
import kotlin.math.absoluteValue


class GalaxyGenerator(
    private val random: RandomDataGenerator
) {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val generator = GalaxyGenerator(RandomDataGenerator(MersenneTwister(1L)))
            val stars = generator
                .generate(
                    10000,
                    5,
                    30.0,
                    0.05,
                    10.0
                )

            val image = renderStars(3, stars)
            val icon = ImageIcon(image)
            val frame = JFrame()
            frame.layout = FlowLayout()
            frame.setSize(image.width, image.height)
            val lbl = JLabel()
            lbl.icon = icon
            frame.add(lbl)
            frame.isVisible = true
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        }

        fun renderStars(
            scale: Int,
            stars: List<Vector3D>
        ) : BufferedImage {

            var width = 0
            var height = 0
            for (star in stars) {
                val x = Math.ceil(star.x.absoluteValue).toInt()
                val y = Math.ceil(star.y.absoluteValue).toInt()
                if (x > width) {
                    width = x
                }
                if(y > height) {
                    height = y
                }
            }
            width *= scale
            height *= scale

            val image = BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_RGB
            )

            val g2 = image.createGraphics()
            g2.color = Color.BLACK
            g2.fillRect(0, 0, width, height)
            g2.color = Color.WHITE
            val halfWidth = width / 2
            val halfHeight = height / 2
            val movement = Vector3D(halfWidth.toDouble(), halfHeight.toDouble(), 0.0)
            for (star in stars) {
                val scaled = star.scalarMultiply(scale.toDouble()).add(movement)
                g2.fillOval(scaled.x.toInt(), scaled.y.toInt(), scale, scale)
            }
            g2.dispose()
            return image
        }

    }


    fun generate(
        starCount: Int,
        armCount: Int,
        radius: Double,
        spinFactor: Double,
        armRadius: Double
    ) : List<Vector3D> {
        if (starCount <= 0) {
            throw IllegalArgumentException("starCount must be positive, was: $starCount")
        }
        if (armCount <= 0) {
            throw IllegalArgumentException("armCount must be positive, was $armCount")
        }

        val stars = genInitialStars(starCount, armCount, radius, armRadius)

        val spunStars = spinStars(stars, spinFactor)


        return spunStars
    }

    private fun spinStars(stars: List<Vector3D>, spinFactor: Double): List<Vector3D> {

        val spunStars = ArrayList<Vector3D>(stars.size)
        val axis = Vector3D(0.0, 0.0, 1.0)
        val centre = Vector3D.ZERO
        for(star in stars) {
            val distance = star.distance(centre)
            val angle = distance * spinFactor
            val rotation = Rotation(
                axis,
                angle,
                RotationConvention.VECTOR_OPERATOR
            )
            val spun = rotation.applyTo(star)
            spunStars.add(spun)
        }

        return spunStars
    }


    private fun genInitialStars(
        starCount: Int,
        armCount: Int,
        radius: Double,
        armRadius: Double
    ) : List<Vector3D> {

        val fullCircle = Math.PI * 2
        val radiansPerArm = fullCircle / armCount

        val rotation = Rotation(
            Vector3D(0.0, 0.0, 1.0),
            radiansPerArm,
            RotationConvention.VECTOR_OPERATOR)

        val starsPerArm = (starCount / armCount)
        val allStars = ArrayList<Vector3D>(starCount)
        for (arm in 0 until armCount) {
            val armStars = ArrayList<Vector3D>(starsPerArm)
            for (star in 0 until starsPerArm) {
                val x = random.nextExponential(radius)
                val y = random.nextExponential(armRadius) - random.nextExponential(armRadius)
                val z = 0.0
                var vector = Vector3D(x, y, z)
                for (rotCount in 0 until arm) {
                    vector = rotation.applyTo(vector)
                }
                armStars.add(vector)
            }
            allStars.addAll(armStars)
        }


        return allStars
    }

}