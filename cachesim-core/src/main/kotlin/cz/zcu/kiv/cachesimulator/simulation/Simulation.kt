package cz.zcu.kiv.cachesimulator.simulation

import cz.zcu.kiv.cachesimulator.config.SimulationSettings
import cz.zcu.kiv.cachesimulator.policy.PolicyParams
import cz.zcu.kiv.cachesimulator.request.ProgressListener
import cz.zcu.kiv.cachesimulator.request.RequestSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Runs one whole simulation, the same way for every front end. It runs on [Dispatchers.Default], so it can be
 * called from any thread; cancel the calling coroutine to stop it.
 *
 * @param onProgress gets the progress in percent, on the simulation's thread
 * @return results per user, sorted by user ID
 */
suspend fun runSimulation(
    source: RequestSource,
    config: SimulationConfig,
    settings: SimulationSettings = SimulationSettings(),
    policyParams: PolicyParams = PolicyParams(),
    onProgress: ProgressListener = {},
): List<UserStatistics> =
    withContext(Dispatchers.Default) {
        val context = SimulationContext(settings, policyParams)
        val simulation = AccessSimulation(source.createQueue(settings), config, context, onProgress)
        if (source == RequestSource.AFS_LOG && !settings.randomFileSizesForLog) {
            simulation.simulateFromLog()
        } else {
            simulation.simulateRandomFileSizes()
        }
        simulation.results()
    }
