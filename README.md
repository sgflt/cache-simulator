# Cache Simulator

Simulates client-side file caching policies (LRU, LFU, ARC, LIRS, MQ, 2Q, FBR, LRFU, …)
over generated request streams or AFS log files, and compares their hit ratios and saved
traffic in tables and charts.

## Requirements

- JDK 25 or newer
- Maven 3.9 or newer

The code is Kotlin (2.4); Maven downloads the Kotlin compiler, so nothing else needs installing.

## Modules

| Module | Artifact | Content |
|---|---|---|
| `cachesim-core` | `cache-core` | simulation engine, policies, request sources, CSV/XLS export (no UI) |
| `cachesim-gui` | `cache-gui` | JavaFX desktop application |
| `cachesim-cli` | `cache-cli` | command line and interactive terminal UI |

## Build

```sh
mvn clean verify
```

This runs the tests and ktlint (`mvn ktlint:format` fixes most style problems), and builds two
runnable jars with all dependencies:

- `cachesim-gui/target/cache-gui-3.0-alpha-jar-with-dependencies.jar`
- `cachesim-cli/target/cache-cli-3.0-alpha-jar-with-dependencies.jar`

## Run the GUI

```sh
java -jar cachesim-gui/target/cache-gui-3.0-alpha-jar-with-dependencies.jar
# or, after `mvn install -DskipTests`:
mvn -pl cachesim-gui javafx:run
```

The GUI is JavaFX. The runnable jar contains the JavaFX native libraries for the
platform it was **built on** (Linux, Windows or macOS); build it on the platform you want
to run it on. Running from the classpath prints a harmless
`Unsupported JavaFX configuration` warning at startup.

## Run in a terminal

```sh
alias cachesim='java -jar cachesim-cli/target/cache-cli-3.0-alpha-jar-with-dependencies.jar'

cachesim                      # interactive terminal UI (same as `cachesim tui`)
cachesim list-policies        # policy names usable with --policies
cachesim run --help           # all options

# Zipf requests, a few policies and sizes, results as tables
cachesim run -n 100000 -p lru,arc,lirs,2q,lfu-ss -c 8,64,512

# replay an AFS log, all policies, save a spreadsheet
cachesim run -l afs.log -f xls -o results.xls
```

`run` prints tables to stdout (or CSV with `-f csv`), and the progress bar to stderr only
when attached to a terminal, so its output can be piped. Ctrl+C stops a running simulation.
Settings not given on the command line come from `config.xml` when it exists.

The GUI stores its settings in `config.xml` in the working directory when it exits.

## Packages

All under `cz.zcu.kiv.cachesimulator`:

| Package | Content |
|---|---|
| `policy` | cache replacement policies (`CachePolicy` subclasses) and their registry, `CachePolicies` |
| `request` | request sources: random generators and the AFS log reader |
| `server` | the simulated file server and its per-cache request counters |
| `simulation` | the simulation itself (`runSimulation`) and its statistics |
| `config` | `SimulationSettings` and reading/writing `config.xml` |
| `report` | CSV / XLS / console export |
| `gui` | JavaFX user interface (`/fx/main.fxml`, `/fx/app.css`), in `cachesim-gui` |
| `cli` | picocli commands and the Lanterna terminal UI (`cli.tui`), in `cachesim-cli` |

Adding a policy: write a `CachePolicy` subclass and add a `PolicyDescriptor` for it in `CachePolicies`.
Known behaviour kept from the original simulator, on purpose, is listed in [FOLLOW-UPS.md](FOLLOW-UPS.md).
