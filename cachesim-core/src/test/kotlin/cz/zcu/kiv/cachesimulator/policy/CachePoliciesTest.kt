package cz.zcu.kiv.cachesimulator.policy

import cz.zcu.kiv.cachesimulator.simulation.SimulationContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class CachePoliciesTest {
    @Test
    fun `all 36 policies are registered with unique ids, labels and names`() {
        val all = CachePolicies.all
        assertEquals(36, all.size)
        assertEquals(all.size, all.map { it.id }.toSet().size)
        assertEquals(all.size, all.map { it.label }.toSet().size)
        val names = all.map { it.create(SimulationContext(), 1024).name }
        assertEquals(all.size, names.toSet().size)
    }

    @Test
    fun `policies are listed in the order earlier versions used`() {
        val ids = CachePolicies.all.map { it.id }
        assertEquals(listOf("ARC", "Clock", "FBR", "FIFO", "FIFO_2ndChance"), ids.take(5))
        assertEquals(listOf("WLFU_SS10", "WLFU_SS10X", "WLFU_SS2"), ids.subList(ids.indexOf("WLFU_SS10"), ids.indexOf("WLFU_SS10") + 3))
        assertEquals(listOf("WLFU_SS", "WLFU_SSX", "_2Q"), ids.takeLast(3))
    }

    @Test
    fun `policies are found by id`() {
        assertSame(CachePolicies.TWO_QUEUE, CachePolicies["_2Q"])
        assertEquals("2Q", CachePolicies.require("_2Q").create(SimulationContext(), 1024).name)
        assertNull(CachePolicies["NOPE"])
        assertFailsWith<IllegalArgumentException> { CachePolicies.require("NOPE") }
    }

    @Test
    fun `X variants ignore files bigger than half of the cache`() {
        val cache = CachePolicies.require("WLFU_SSX").create(SimulationContext(), 1000)
        cache.insertFile(CachedFile("big", 501, 0))
        cache.insertFile(CachedFile("small", 500, 0))
        assertNull(cache.get("big"))
        assertEquals("small", cache.get("SMALL")?.name)
    }
}
