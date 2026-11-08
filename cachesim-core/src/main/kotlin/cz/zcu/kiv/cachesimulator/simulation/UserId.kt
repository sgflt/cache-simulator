package cz.zcu.kiv.cachesimulator.simulation

/**
 * A user from an AFS log is identified by host ID and IP address, packed into one number:
 * the host ID in the upper 32 bits, the IPv4 address in the lower 32 bits.
 * Generated requests all come from user 0.
 */
object UserId {
    const val SIMULATED: Long = 0

    fun of(
        hostId: Long,
        ip: String,
    ): Long = (hostId shl 32) + ipToLong(ip)

    fun hostId(userId: Long): Long = userId shr 32

    fun ip(userId: Long): String = longToIp(userId - (hostId(userId) shl 32))

    /** "Simulated user" for generated requests, otherwise "42, ip: 10.0.0.1" */
    fun label(userId: Long): String = if (userId == SIMULATED) "Simulated user" else "${hostId(userId)}, ip: ${ip(userId)}"

    internal fun ipToLong(ip: String): Long = ip.split('.').fold(0L) { result, part -> (result shl 8) or (part.toInt() and 0xFF).toLong() }

    internal fun longToIp(value: Long): String = listOf(24, 16, 8, 0).joinToString(".") { shift -> ((value shr shift) and 0xFF).toString() }
}
