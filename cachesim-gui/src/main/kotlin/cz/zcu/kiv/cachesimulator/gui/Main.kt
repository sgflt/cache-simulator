package cz.zcu.kiv.cachesimulator.gui

import javafx.application.Application

/**
 * Starts the GUI. It is not inside [CacheSimulatorApp], because JavaFX refuses to start an
 * [Application] subclass from the classpath (fat jar).
 */
fun main(args: Array<String>) {
    Application.launch(CacheSimulatorApp::class.java, *args)
}
