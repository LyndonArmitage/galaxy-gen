package codes.lyndon.galaxy

import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomDataGenerator
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.filechooser.FileFilter

class Tool(
    private val random: RandomDataGenerator = RandomDataGenerator(MersenneTwister())
) {

    private lateinit var galaxy: BufferedImage
    private val generator = GalaxyGenerator(random)
    private val frame = JFrame("Galaxy Tool")

    private var renderIcon: ImageIcon = ImageIcon(
        BufferedImage(800, 600, BufferedImage.TYPE_BYTE_BINARY)
    )
    private val renderLabel = JLabel(renderIcon)
    private val renderPane = JScrollPane(renderLabel)

    private companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            Tool().start()

        }
    }

    fun start() {

        val mainPanel = JPanel(FlowLayout())

        val controlsPanel = makeControlsPanel()
        val renderPanel = makeRenderPanel()

        mainPanel.add(controlsPanel)
        mainPanel.add(renderPanel)


        frame.add(mainPanel)
        // default size
        frame.setSize(800, 600)
        frame.pack()
        frame.isVisible = true
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    }

    private fun makeControlsPanel(): JPanel {
        val layout = GridBagLayout()
        val panel = JPanel(
            layout
        )

        val c = GridBagConstraints()
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0
        c.gridy = 0
        c.gridwidth = 2
        panel.add(
            JLabel("Controls"),
            c
        )

        c.gridwidth = 1

        c.gridx = 0
        c.gridy = 1
        panel.add(
            JLabel("Seed:"),
            c
        )

        val seedModel = SpinnerNumberModel(
            0L,
            Long.MIN_VALUE,
            Long.MAX_VALUE,
            1
        )
        val seedSpinner = JSpinner(seedModel)
        c.gridx = 1
        c.gridy = 1
        panel.add(
            seedSpinner,
            c
        )

        val randomSeedTickBox = JCheckBox("Random Seed?")
        randomSeedTickBox.isSelected = true
        c.gridx = 0
        c.gridy = 2
        panel.add(
            randomSeedTickBox,
            c
        )

        c.gridx = 0
        c.gridy = 3
        panel.add(
            JLabel("Star Count:"),
            c
        )

        val starCountModel = SpinnerNumberModel(
            1000,
            1,
            Integer.MAX_VALUE,
            100
        )
        val starCountSpinner = JSpinner(starCountModel)
        c.gridx = 1
        c.gridy = 3
        panel.add(
            starCountSpinner,
            c
        )


        c.gridx = 0
        c.gridy = 4
        panel.add(
            JLabel("Arm Count:"),
            c
        )

        val armCountModel = SpinnerNumberModel(
            3,
            1,
            100,
            1
        )
        val armCountSpinner = JSpinner(armCountModel)
        c.gridx = 1
        c.gridy = 4
        panel.add(
            armCountSpinner,
            c
        )


        c.gridx = 0
        c.gridy = 5
        panel.add(
            JLabel("Galaxy Radius:"),
            c
        )

        val galaxyRadiusModel = SpinnerNumberModel(
            100.0,
            1.0,
            10_000.0,
            0.01
        )
        val galaxyRadiusSpinner = JSpinner(galaxyRadiusModel)
        c.gridx = 1
        c.gridy = 5
        panel.add(
            galaxyRadiusSpinner,
            c
        )


        c.gridx = 0
        c.gridy = 6
        panel.add(
            JLabel("Arm Radius:"),
            c
        )

        val armRadiusModel = SpinnerNumberModel(
            10.0,
            1.0,
            10_000.0,
            0.01
        )
        val armRadiusSpinner = JSpinner(armRadiusModel)
        c.gridx = 1
        c.gridy = 6
        panel.add(
            armRadiusSpinner,
            c
        )



        c.gridx = 0
        c.gridy = 7
        panel.add(
            JLabel("Spin Factor:"),
            c
        )

        val spinFactorModel = SpinnerNumberModel(
            0.001,
            0.0,
            1.0,
            0.001
        )
        val spinFactorSpinner = JSpinner(spinFactorModel)
        c.gridx = 1
        c.gridy = 7
        panel.add(
            spinFactorSpinner,
            c
        )

        val generateButton = JButton("Generate")
        c.gridheight = 2
        c.gridwidth = 2
        c.gridx = 0
        c.gridy = 8
        panel.add(
            generateButton,
            c
        )


        val saveImageButton = JButton("Save Image")
        saveImageButton.isEnabled = false
        c.gridheight = 2
        c.gridwidth = 2
        c.gridx = 0
        c.gridy = 10
        panel.add(
            saveImageButton,
            c
        )

        generateButton.addActionListener {

            val useRandomSeed = randomSeedTickBox.isSelected

            val seed = if (useRandomSeed) {
                val newSeed = random.nextLong(Long.MIN_VALUE, Long.MAX_VALUE)
                seedModel.value = newSeed
                newSeed
            } else {
                seedModel.number.toLong()
            }
            val starCount = starCountModel.number.toInt()
            val armCount = armCountModel.number.toInt()
            val galaxyRadius = galaxyRadiusModel.number.toDouble()
            val armRadius = armRadiusModel.number.toDouble()
            val spinFactor = spinFactorModel.number.toDouble()

            generate(
                seed,
                starCount,
                armCount,
                galaxyRadius,
                armRadius,
                spinFactor
            )
            saveImageButton.isEnabled = true
        }


        saveImageButton.addActionListener {
            if (!saveImageButton.isEnabled) {
                return@addActionListener
            }

            val chooser = JFileChooser()
            chooser.selectedFile = File("galaxy${seedModel.value}.png")
            chooser.fileFilter = object : FileFilter() {
                override fun accept(f: File): Boolean {
                    return f.name.endsWith(".png") || f.isDirectory
                }

                override fun getDescription(): String = "PNG file"
            }
            val dialog = chooser.showSaveDialog(frame)
            if (dialog == JFileChooser.APPROVE_OPTION) {
                val file = chooser.selectedFile
                ImageIO.write(galaxy, "png", file)
                JOptionPane.showMessageDialog(frame, "Saved to ${file.path}");
            }
        }


        return panel
    }

    private fun makeRenderPanel(): JPanel {
        val panel = JPanel()
        panel.add(renderPane)
        return panel
    }

    private fun generate(
        seed: Long,
        starCount: Int,
        armCount: Int,
        galaxyRadius: Double,
        armRadius: Double,
        spinFactor: Double
    ) {
        random.reSeed(seed)
        val stars = generator.generate(
            starCount,
            armCount,
            galaxyRadius,
            spinFactor,
            armRadius
        )

        galaxy = GalaxyGenerator.renderStars(3, stars)
        renderIcon = ImageIcon(galaxy)
        renderLabel.icon = renderIcon
        frame.repaint()

        val horizontalScrollBar = renderPane.horizontalScrollBar
        horizontalScrollBar.value = horizontalScrollBar.maximum
        horizontalScrollBar.value = horizontalScrollBar.value / 2
        val verticalScrollBar = renderPane.verticalScrollBar
        verticalScrollBar.value = verticalScrollBar.maximum
        verticalScrollBar.value = verticalScrollBar.value / 2
    }


}