package cz.zcu.kiv.cacheSimulator.fx;

import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

/**
 * Small helpers for setting up controls.
 */
final class Controls {

  private Controls() {
  }

  /**
   * editable integer spinner; {@code onChange} gets every new value
   */
  static Spinner<Integer> intSpinner(final Spinner<Integer> spinner, final int min, final int max,
                                     final int initial, final IntConsumer onChange) {
    spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, clamp(initial, min, max)));
    spinner.valueProperty().addListener((obs, old, value) -> {
      if (value != null) {
        onChange.accept(value);
      }
    });
    makeEditable(spinner);
    return spinner;
  }

  static Spinner<Integer> intSpinner(final int min, final int max, final int initial, final IntConsumer onChange) {
    return intSpinner(new Spinner<>(), min, max, initial, onChange);
  }

  /**
   * editable decimal spinner; {@code onChange} gets every new value
   */
  static Spinner<Double> doubleSpinner(final Spinner<Double> spinner, final double min, final double max,
                                       final double initial, final double step, final DoubleConsumer onChange) {
    spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
      min, max, Math.max(min, Math.min(max, initial)), step));
    spinner.valueProperty().addListener((obs, old, value) -> {
      if (value != null) {
        onChange.accept(value);
      }
    });
    makeEditable(spinner);
    return spinner;
  }

  static Spinner<Double> doubleSpinner(final double min, final double max, final double initial,
                                       final double step, final DoubleConsumer onChange) {
    return doubleSpinner(new Spinner<>(), min, max, initial, step, onChange);
  }

  /**
   * typed values are committed on focus loss too, invalid text is reverted
   */
  private static void makeEditable(final Spinner<?> spinner) {
    spinner.setEditable(true);
    spinner.getEditor().focusedProperty().addListener((obs, was, focused) -> {
      if (!focused) {
        commit(spinner);
      }
    });
    spinner.getEditor().setOnAction(e -> commit(spinner));
  }

  private static void commit(final Spinner<?> spinner) {
    try {
      spinner.commitValue();
    } catch (final RuntimeException e) {
      spinner.cancelEdit();
    }
  }

  static int clamp(final int value, final int min, final int max) {
    return Math.max(min, Math.min(max, value));
  }

  static int percent(final double fraction) {
    return (int) Math.round(fraction * 100);
  }
}
