package cz.zcu.kiv.cachesimulator.simulation

/** A result value shown in the result tables and charts. */
enum class ResultMetric(
    val label: String,
    private val values: UserStatistics.(policyName: String) -> List<Number>,
) {
    HIT_RATIO("Read Hit Ratio [%]", UserStatistics::hitRatios),
    HIT_COUNT("Read Hit Count", UserStatistics::hits),
    SAVED_BYTES_RATIO("Saved Bytes Ratio [%]", UserStatistics::savedBytesRatios),
    SAVED_BYTES("Saved Bytes [MB]", UserStatistics::savedMegabytes),
    TRANSFER_DECREASE_RATIO("Data Transfer Decrease Ratio [%]", UserStatistics::transferDecreaseRatios),
    TRANSFER_DECREASE("Data Transfer Decrease [MB]", UserStatistics::transferDecrease),
    ;

    /** @return one value per cache size, in the order of [UserStatistics.cacheSizesMb] */
    fun values(
        stats: UserStatistics,
        policyName: String,
    ): List<Number> = stats.values(policyName)

    override fun toString() = label
}
