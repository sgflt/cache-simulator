# Follow-ups

The move to Kotlin kept every simulation result unchanged (`policy-regression.txt` pins them).
That meant keeping these quirks of the original simulator. Each one is a candidate fix. Fixing
any of them changes results, so update `policy-regression.txt` in the same change and say so.

## Results

- **Data transfer decrease mixes units.** `UserStatistics.transferDecrease` subtracts saved
  megabytes from requested *bytes* and then divides by 1 MB. Its ratio counterpart is fine.
- **The first server request counts as 0.** The server's per-cache counters start from 0 on the
  first increment (`RequestCounters.increment`), so every file's count is one short.
- **Server counters are shared per policy name and capacity.** Two caches with the same policy
  and capacity (e.g. the same policy simulated for several users) share one counter
  (`StatsKey`), as the original's `hashCode`-keyed counters did.
- **LRFU-SS uses `System.nanoTime()`.** Its results change from run to run, which is why the
  regression test and output comparisons leave it out. A simulated clock would make it repeatable.
- **ARC ghost lists never match.** Ghost entries are looked up by identity, with a new object each
  time, so a ghost hit never happens and ARC never adapts its target size.
- **Log replay asks the cache twice on a hit.** With sizes from the AFS log, a hit calls
  `CachePolicy.get` twice, which counts double in frequency-based policies.
- **Users below the request limit stay in the results.** They show up with empty results instead
  of being left out.

## Configuration and output

- **`config.xml` download window is truncated.** `cacheCapacityForDownloadWindow="0.29"` reads as
  28 % (`(0.29 * 100).toInt()`); rounding would be correct.
- **CSV extension is always added.** `cachesim run -f csv -o out.txt` writes `out.txt.csv`.
- **Policy parameters are not saved.** The GUI's policy settings (FBR, LIRS, LRFU, …) last only
  until the app closes; `config.xml` has no place for them.

## Behaviour that changed in the Kotlin version

These are intended and do not change any numbers:

- Results with several users (AFS logs) are sorted by user ID; before, they came in hash order.
- CSV files always use `\n` line endings.
- `CachePolicy.reset()` now also clears the used capacity and the pending downloads.
- The GUI and the TUI no longer share global settings: each run gets its own
  `SimulationSettings`, `PolicyParams` and `Server`.
