package cz.zcu.kiv.cachesimulator.gui

import cz.zcu.kiv.cachesimulator.config.ConfigFile
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

/** JavaFX application: the main window. */
class CacheSimulatorApp : Application() {
    private lateinit var controller: MainController

    override fun start(stage: Stage) {
        controller = MainController(ConfigFile.read())
        val loader = FXMLLoader(resource("/fx/main.fxml")).apply { setControllerFactory { controller } }
        val root = loader.load<Parent>()
        stage.apply {
            title = "Cache Simulator"
            icons += Image(resource("/ico/simulation.png").toExternalForm())
            minWidth = 900.0
            minHeight = 600.0
            scene = Scene(root, 1120.0, 740.0).apply { stylesheets += STYLESHEET }
            show()
        }
    }

    /** saves the settings on every way out (menu, toolbar, closing the window) */
    override fun stop() {
        if (::controller.isInitialized) {
            controller.close()
            ConfigFile.write(controller.settings)
        }
    }

    internal companion object {
        val STYLESHEET: String by lazy { resource("/fx/app.css").toExternalForm() }

        fun resource(path: String) = checkNotNull(CacheSimulatorApp::class.java.getResource(path)) { "Missing resource $path" }
    }
}
