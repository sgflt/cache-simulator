package cz.zcu.kiv.cachesimulator.simulation

import cz.zcu.kiv.cachesimulator.config.SimulationSettings
import cz.zcu.kiv.cachesimulator.policy.PolicyDescriptor
import cz.zcu.kiv.cachesimulator.policy.PolicyParams
import cz.zcu.kiv.cachesimulator.server.Server

/**
 * What the caches of one simulation run share: the settings, the server and the clock.
 */
class SimulationContext(
    val settings: SimulationSettings = SimulationSettings(),
    val policyParams: PolicyParams = PolicyParams(),
    val server: Server = Server(),
) {
    /** time of the request being simulated, in ms; downloads of files bigger than the cache finish by it */
    var now: Long = 0
        internal set
}

/**
 * What to simulate: every policy with every cache size.
 *
 * @property cacheSizesMb cache capacities in MB
 */
data class SimulationConfig(
    val policies: List<PolicyDescriptor>,
    val cacheSizesMb: List<Int>,
)
