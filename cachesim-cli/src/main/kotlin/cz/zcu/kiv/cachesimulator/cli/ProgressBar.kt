package cz.zcu.kiv.cachesimulator.cli

import java.io.PrintStream

/** One-line progress bar redrawn in place on a terminal. */
internal class ProgressBar(
    private val out: PrintStream,
) {
    private var last = -1

    fun update(percent: Int) {
        val value = percent.coerceIn(0, 100)
        if (value == last) return
        last = value
        val filled = value * WIDTH / 100
        out.print("\r[${"#".repeat(filled)}${" ".repeat(WIDTH - filled)}] $value %")
        out.flush()
    }

    fun finish() {
        out.print("\r${" ".repeat(WIDTH + 8)}\r")
        out.flush()
    }

    private companion object {
        const val WIDTH = 40
    }
}
