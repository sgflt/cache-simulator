package cz.zcu.kiv.cachesimulator.gui

import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import java.text.DecimalFormat
import kotlin.math.roundToInt

// Small helpers for setting up controls.

/** makes [this] an editable integer spinner; [onChange] gets every new value */
internal fun Spinner<Int>.setUp(
    min: Int,
    max: Int,
    initial: Int,
    onChange: (Int) -> Unit = {},
): Spinner<Int> =
    apply {
        valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial.coerceIn(min, max))
        valueProperty().addListener { _, _, value -> value?.let(onChange) }
        makeEditable()
    }

/** makes [this] an editable decimal spinner; [onChange] gets every new value */
internal fun Spinner<Double>.setUp(
    min: Double,
    max: Double,
    initial: Double,
    step: Double,
    onChange: (Double) -> Unit,
): Spinner<Double> =
    apply {
        valueFactory = SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initial.coerceIn(min, max), step)
        valueProperty().addListener { _, _, value -> value?.let(onChange) }
        makeEditable()
    }

/** typed values are committed on focus loss too, invalid text is reverted */
private fun Spinner<*>.makeEditable() {
    isEditable = true
    editor.focusedProperty().addListener { _, _, focused -> if (!focused) commitOrRevert() }
    editor.setOnAction { commitOrRevert() }
}

private fun Spinner<*>.commitOrRevert() {
    try {
        commitValue()
    } catch (_: RuntimeException) {
        cancelEdit()
    }
}

internal fun Double.toPercent(): Int = (this * 100).roundToInt()

/** formats a result value the way the tables show it */
internal fun formatResult(value: Number): String =
    DecimalFormat(if (value is Double || value is Float) "#,##0.00" else "#,##0").format(value)
