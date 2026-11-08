package cz.zcu.kiv.cachesimulator.cli

import cz.zcu.kiv.cachesimulator.policy.CachePolicies
import cz.zcu.kiv.cachesimulator.policy.PolicyDescriptor
import java.util.Locale

/** Finds policies by the names people type. */
object Policies {
    /**
     * Accepts ids (`LFU_SS`, `_2Q`) and labels (`LFU-SS`, `2 Queues`), ignoring case, or `all`.
     * No names means all policies.
     *
     * @throws IllegalArgumentException for an unknown name
     */
    fun resolve(names: List<String>): List<PolicyDescriptor> {
        if (names.isEmpty() || names.any { it.equals("all", ignoreCase = true) }) return CachePolicies.all
        return names
            .map { name ->
                val key = normalize(name)
                CachePolicies.all.find { normalize(it.id) == key || normalize(it.label) == key }
                    ?: throw IllegalArgumentException("Unknown cache policy '$name'. Run 'list-policies' to see them all.")
            }.distinct()
    }

    private fun normalize(name: String): String =
        name
            .trim()
            .lowercase(Locale.ROOT)
            .replace('-', '_')
            .replace(' ', '_')
            .trimStart('_')
}
