package cz.zcu.kiv.cacheSimulator.fx;

import cz.zcu.kiv.cacheSimulator.cachePolicies.FBR;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LIRS;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LRDv2;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LRFU;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LRFU_SS;
import cz.zcu.kiv.cacheSimulator.cachePolicies.LRU_K;
import cz.zcu.kiv.cacheSimulator.cachePolicies.MQ;
import cz.zcu.kiv.cacheSimulator.cachePolicies._2Q;
import cz.zcu.kiv.cacheSimulator.shared.GlobalVariables;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

import static cz.zcu.kiv.cacheSimulator.fx.Controls.percent;

/**
 * Settings forms of the cache policies that have parameters.
 * Values are written straight to the policies' static settings, as in the original GUI.
 */
final class PolicySettings {

  private PolicySettings() {
  }

  /**
   * @param policy simple class name of the policy
   * @param name   display name of the policy
   */
  static Node create(final String policy, final String name) {
    return switch (policy) {
      case "FBR" -> fbr(name);
      case "LFU_SS" -> new Form(name)
        .check("Send statistics back to server", GlobalVariables.isSendStatisticsToServerLFUSS(),
          GlobalVariables::setSendStatisticsToServerLFUSS)
        .build();
      case "LIRS" -> new Form(name)
        .integer("Capacity for LIR [%]:", 1, 99, percent(LIRS.getLIR_CAPACITY()),
          v -> LIRS.setLIR_CAPACITY(v / 100.0))
        .build();
      case "LRDv2" -> new Form(name)
        .integer("Interval for decreasing reference count:", 1, 199, LRDv2.getINTERVAL(), LRDv2::setINTERVAL)
        .decimal("Divisor for reference count:", 1.1, 20, LRDv2.getK1(), 0.1, LRDv2::setK1)
        .build();
      case "LRFU" -> new Form(name)
        .decimal("P:", 1, 10, LRFU.getP(), 0.1, LRFU::setP)
        .decimal("Lambda:", 0, 10, LRFU.getLAMBDA(), 0.01, LRFU::setLAMBDA)
        .build();
      case "LRFU_SS" -> new Form(name)
        .decimal("K1:", 0, 50, LRFU_SS.getK1(), 0.01, LRFU_SS::setK1)
        .decimal("K2:", 0, 50, LRFU_SS.getK2(), 0.01, LRFU_SS::setK2)
        .check("Send statistics back to server", GlobalVariables.isSendStatisticsToServerLRFUSS(),
          GlobalVariables::setSendStatisticsToServerLRFUSS)
        .build();
      case "LRU_K" -> new Form(name)
        .integer("K:", 1, 10, LRU_K.getK(), LRU_K::setK)
        .integer("Correlated reference period:", 1, 30, LRU_K.getCORRELATED_REFERENCE_PERIOD(),
          LRU_K::setCORRELATED_REFERENCE_PERIOD)
        .build();
      case "MQ" -> new Form(name)
        .integer("Life time:", 1, 50, MQ.getLIFE_TIME(), MQ::setLIFE_TIME)
        .integer("Out queue capacity:", 1, 50, MQ.getQOUT_CAPACITY(), MQ::setQOUT_CAPACITY)
        .integer("Queue count:", 1, 15, MQ.getQUEUE_COUNT(), MQ::setQUEUE_COUNT)
        .build();
      case "_2Q" -> new Form(name)
        .integer("Capacity for FIFO [%]:", 1, 75, percent(_2Q.getFIFO_CAPACITY()),
          v -> _2Q.setFIFO_CAPACITY(v / 100.0))
        .build();
      default -> new Form(name).text("This cache policy does not have any settings.").build();
    };
  }

  /**
   * OLD and NEW sections share the cache, at least 1 % stays for the middle section
   */
  private static Node fbr(final String name) {
    final Spinner<Integer> oldSection = new Spinner<>();
    final Spinner<Integer> newSection = new Spinner<>();
    Controls.intSpinner(oldSection, 1, 98, percent(FBR.getOLD_SECTION()), v -> {
      if (v + newSection.getValue() > 99) {
        oldSection.getValueFactory().setValue(99 - newSection.getValue());
      } else {
        FBR.setOLD_SECTION(v / 100.0);
      }
    });
    Controls.intSpinner(newSection, 1, 98, percent(FBR.getNEW_SECTION()), v -> {
      if (v + oldSection.getValue() > 99) {
        newSection.getValueFactory().setValue(99 - oldSection.getValue());
      } else {
        FBR.setNEW_SECTION(v / 100.0);
      }
    });
    return new Form(name)
      .row("Cache capacity for OLD section [%]:", oldSection)
      .row("Cache capacity for NEW section [%]:", newSection)
      .build();
  }

  /**
   * title + label/control rows
   */
  private static final class Form {

    private final VBox box = new VBox();
    private final GridPane grid = new GridPane();
    private int row;

    Form(final String title) {
      final Label heading = new Label(title + " settings");
      heading.getStyleClass().add("card-title");
      this.grid.getStyleClass().add("form-grid");
      this.box.getStyleClass().addAll("card", "policy-settings");
      // keep the card as tall as its content, not the whole pane
      this.box.setMaxHeight(Region.USE_PREF_SIZE);
      this.box.getChildren().addAll(heading, this.grid);
    }

    Form row(final String label, final Node control) {
      final Label l = new Label(label);
      l.getStyleClass().add("form-label");
      l.setLabelFor(control);
      this.grid.addRow(this.row++, l, control);
      return this;
    }

    Form integer(final String label, final int min, final int max, final int initial, final IntConsumer onChange) {
      return row(label, Controls.intSpinner(min, max, initial, onChange));
    }

    Form decimal(final String label, final double min, final double max, final double initial,
                 final double step, final DoubleConsumer onChange) {
      return row(label, Controls.doubleSpinner(min, max, initial, step, onChange));
    }

    Form check(final String label, final boolean initial, final Consumer<Boolean> onChange) {
      final CheckBox box = new CheckBox(label);
      box.setSelected(initial);
      box.selectedProperty().addListener((obs, old, value) -> onChange.accept(value));
      this.grid.add(box, 0, this.row++, 2, 1);
      return this;
    }

    Form text(final String text) {
      final Label l = new Label(text);
      l.getStyleClass().add("hint");
      this.grid.add(l, 0, this.row++, 2, 1);
      return this;
    }

    Node build() {
      return this.box;
    }
  }
}
