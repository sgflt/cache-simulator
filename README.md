# Cache Simulator

Simulates client-side file caching policies (LRU, LFU, ARC, LIRS, MQ, 2Q, FBR, LRFU, …)
over generated request streams or AFS log files, and compares their hit ratios and saved
traffic in tables and charts.

## Requirements

- JDK 25 or newer
- Maven 3.9 or newer

## Build

```sh
mvn clean verify
```

This runs the tests and builds a runnable jar with all dependencies:
`cachesim2/target/cache-core-3.0-alpha-jar-with-dependencies.jar`.

## Run

```sh
mvn -pl cachesim2 javafx:run
# or
java -jar cachesim2/target/cache-core-3.0-alpha-jar-with-dependencies.jar
```

The GUI is JavaFX. The runnable jar contains the JavaFX native libraries for the
platform it was **built on** (Linux, Windows or macOS); build it on the platform you want
to run it on. Running from the classpath prints a harmless
`Unsupported JavaFX configuration` warning at startup.

Settings are stored in `config.xml` in the working directory when the application exits.

## Project layout

| Package | Content |
|---|---|
| `cachePolicies` | cache replacement policies (`ICache` implementations) |
| `dataAccess` | request sources: random generators and the AFS log reader |
| `simulation` | the simulation itself and its statistics |
| `output` | CSV / XLS / console export |
| `fx` | JavaFX user interface (`/fx/main.fxml`, `/fx/app.css`) |
