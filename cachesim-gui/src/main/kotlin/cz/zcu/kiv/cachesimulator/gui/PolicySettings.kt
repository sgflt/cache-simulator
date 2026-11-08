package cz.zcu.kiv.cachesimulator.gui

import cz.zcu.kiv.cachesimulator.config.SimulationSettings
import cz.zcu.kiv.cachesimulator.policy.CachePolicies
import cz.zcu.kiv.cachesimulator.policy.PolicyDescriptor
import cz.zcu.kiv.cachesimulator.policy.PolicyParams
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.layout.GridPane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox

/** The settings that the policy forms read and change. */
internal interface SettingsHolder {
    val settings: SimulationSettings
    val policyParams: PolicyParams

    fun updateSettings(change: SimulationSettings.() -> SimulationSettings)

    fun updateParams(change: PolicyParams.() -> PolicyParams)
}

/** Settings forms of the cache policies that have parameters. */
internal object PolicySettings {
    fun create(
        policy: PolicyDescriptor,
        holder: SettingsHolder,
    ): Node {
        val params = holder.policyParams
        val form = Form(policy.label)
        when (policy) {
            CachePolicies.FBR -> {
                fbr(form, holder)
            }

            CachePolicies.LFU_SS -> {
                form.check("Send statistics back to server", holder.settings.sendLfuSsStatistics) {
                    holder.updateSettings { copy(sendLfuSsStatistics = it) }
                }
            }

            CachePolicies.LIRS -> {
                form.integer("Capacity for LIR [%]:", 1, 99, params.lirs.lirCapacity.toPercent()) {
                    holder.updateParams { copy(lirs = lirs.copy(lirCapacity = it / 100.0)) }
                }
            }

            CachePolicies.LRD_V2 -> {
                form
                    .integer("Interval for decreasing reference count:", 1, 199, params.lrdV2.interval) {
                        holder.updateParams { copy(lrdV2 = lrdV2.copy(interval = it)) }
                    }.decimal("Divisor for reference count:", 1.1, 20.0, params.lrdV2.divisor, 0.1) {
                        holder.updateParams { copy(lrdV2 = lrdV2.copy(divisor = it)) }
                    }
            }

            CachePolicies.LRFU -> {
                form
                    .decimal("P:", 1.0, 10.0, params.lrfu.p, 0.1) {
                        holder.updateParams { copy(lrfu = lrfu.copy(p = it)) }
                    }.decimal("Lambda:", 0.0, 10.0, params.lrfu.lambda, 0.01) {
                        holder.updateParams { copy(lrfu = lrfu.copy(lambda = it)) }
                    }
            }

            CachePolicies.LRFU_SS -> {
                form
                    .decimal("K1:", 0.0, 50.0, params.lrfuSs.k1, 0.01) {
                        holder.updateParams { copy(lrfuSs = lrfuSs.copy(k1 = it)) }
                    }.decimal("K2:", 0.0, 50.0, params.lrfuSs.k2, 0.01) {
                        holder.updateParams { copy(lrfuSs = lrfuSs.copy(k2 = it)) }
                    }.check("Send statistics back to server", holder.settings.sendLrfuSsStatistics) {
                        holder.updateSettings { copy(sendLrfuSsStatistics = it) }
                    }
            }

            CachePolicies.LRU_K -> {
                form
                    .integer("K:", 1, 10, params.lruK.k) {
                        holder.updateParams { copy(lruK = lruK.copy(k = it)) }
                    }.integer("Correlated reference period:", 1, 30, params.lruK.correlatedReferencePeriod) {
                        holder.updateParams { copy(lruK = lruK.copy(correlatedReferencePeriod = it)) }
                    }
            }

            CachePolicies.MQ -> {
                form
                    .integer("Life time:", 1, 50, params.mq.lifeTime) {
                        holder.updateParams { copy(mq = mq.copy(lifeTime = it)) }
                    }.integer("Out queue capacity:", 1, 50, params.mq.outQueueCapacity) {
                        holder.updateParams { copy(mq = mq.copy(outQueueCapacity = it)) }
                    }.integer("Queue count:", 1, 15, params.mq.queueCount) {
                        holder.updateParams { copy(mq = mq.copy(queueCount = it)) }
                    }
            }

            CachePolicies.TWO_QUEUE -> {
                form.integer("Capacity for FIFO [%]:", 1, 75, params.twoQueue.fifoCapacity.toPercent()) {
                    holder.updateParams { copy(twoQueue = twoQueue.copy(fifoCapacity = it / 100.0)) }
                }
            }

            else -> {
                form.text("This cache policy does not have any settings.")
            }
        }
        return form.box
    }

    /** OLD and NEW sections share the cache, at least 1 % stays for the middle section */
    private fun fbr(
        form: Form,
        holder: SettingsHolder,
    ) {
        val fbr = holder.policyParams.fbr
        val oldSection = Spinner<Int>()
        val newSection = Spinner<Int>()
        oldSection.setUp(1, 98, fbr.oldSection.toPercent()) {
            if (it + newSection.value > 99) {
                oldSection.valueFactory.value = 99 - newSection.value
            } else {
                holder.updateParams { copy(fbr = this.fbr.copy(oldSection = it / 100.0)) }
            }
        }
        newSection.setUp(1, 98, fbr.newSection.toPercent()) {
            if (it + oldSection.value > 99) {
                newSection.valueFactory.value = 99 - oldSection.value
            } else {
                holder.updateParams { copy(fbr = this.fbr.copy(newSection = it / 100.0)) }
            }
        }
        form
            .row("Cache capacity for OLD section [%]:", oldSection)
            .row("Cache capacity for NEW section [%]:", newSection)
    }

    /** title + label/control rows */
    private class Form(
        title: String,
    ) {
        private val grid = GridPane().apply { styleClass += "form-grid" }
        private var row = 0

        val box =
            VBox(Label("$title settings").apply { styleClass += "card-title" }, grid).apply {
                styleClass.addAll("card", "policy-settings")
                // keep the card as tall as its content, not the whole pane
                maxHeight = Region.USE_PREF_SIZE
            }

        fun row(
            label: String,
            control: Node,
        ) = apply {
            val text =
                Label(label).apply {
                    styleClass += "form-label"
                    labelFor = control
                }
            grid.addRow(row++, text, control)
        }

        fun integer(
            label: String,
            min: Int,
            max: Int,
            initial: Int,
            onChange: (Int) -> Unit,
        ) = row(label, Spinner<Int>().setUp(min, max, initial, onChange))

        fun decimal(
            label: String,
            min: Double,
            max: Double,
            initial: Double,
            step: Double,
            onChange: (Double) -> Unit,
        ) = row(label, Spinner<Double>().setUp(min, max, initial, step, onChange))

        fun check(
            label: String,
            initial: Boolean,
            onChange: (Boolean) -> Unit,
        ) = apply {
            val box = CheckBox(label).apply { isSelected = initial }
            box.selectedProperty().addListener { _, _, value -> onChange(value) }
            grid.add(box, 0, row++, 2, 1)
        }

        fun text(text: String) =
            apply {
                grid.add(Label(text).apply { styleClass += "hint" }, 0, row++, 2, 1)
            }
    }
}
