# Cloud Task Scheduling Algorithms in CloudSim

A Java project that implements and compares **7 task-scheduling algorithms** on a simulated cloud datacenter using [CloudSim](https://github.com/Cloudslab/cloudsim). It runs every algorithm on the same randomly generated workloads, saves charts and a CSV, and prints a recommendation of which algorithm suits the workload best.

The algorithms are ordered as a learning chain: each one exists to fix a weakness of the previous one.

```
Round Robin -> Greedy -> Min-Min -> Max-Min -> GA -> PSO -> ACO
```

## The problem

Given **N tasks** (cloudlets) and **M virtual machines** with different speeds, decide which task runs on which VM so that everything finishes as early as possible.

- **Makespan** = the finish time of the busiest VM. This is the main value we minimize.
- **ETC matrix** = `etc[task][vm]`, the time a task takes on a VM (task length in MI divided by VM MIPS).
- **Solution** = an array `map[task] = vm`.

## Algorithms

| # | Algorithm | Type | Idea | Weakness it exposes |
|---|---|---|---|---|
| 1 | Round Robin | Heuristic | Deal tasks to VMs in turn | Ignores VM speed and task size |
| 2 | Greedy | Heuristic | Give each task to the VM that finishes it earliest | Depends on task arrival order |
| 3 | Min-Min | Heuristic | Repeatedly schedule the task with the smallest best completion time | Long tasks get pushed to the end |
| 4 | Max-Min | Heuristic | Same as Min-Min but picks the largest | One pass, can't undo decisions |
| 5 | GA (Genetic Algorithm) | Metaheuristic | Evolve a population by selection, crossover and mutation | Random start, many parameters |
| 6 | PSO (Particle Swarm) | Metaheuristic | Particles move toward personal and global best | Continuous values for a discrete problem, premature convergence |
| 7 | ACO (Ant Colony) | Metaheuristic | Ants build mappings guided by a heuristic plus pheromone memory | Slowest, many parameters |

## Project structure

```
.
├── src/sched/
│   ├── Scheduler.java      # interface every algorithm implements
│   ├── Metrics.java        # makespan (fitness) calculation
│   ├── RoundRobin.java
│   ├── Greedy.java
│   ├── MinMinMaxMin.java   # Min-Min and Max-Min (one flag switches between them)
│   ├── GA.java
│   ├── PSO.java
│   ├── ACO.java
│   ├── Runner.java         # builds the CloudSim datacenter and runs one algorithm
│   ├── Chart.java          # draws bar charts to PNG (java.awt only)
│   └── Compare.java        # main class: runs all experiments, charts, scoring
├── lib/                    # put the two jars here (not committed)
└── out/                    # compiled classes (not committed)
```

## Prerequisites

- **JDK 8 or 11** (a full JDK, not just a JRE)
- **CloudSim 4.0** jar
- **Apache Commons Math 3.6.1** jar

Check Java with `java -version` and `javac -version`.

## Setup

Create a `lib` folder and download the two JARs into it.

**PowerShell (Windows):**
```powershell
mkdir lib
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/cloudbus/cloudsim/cloudsim/4.0/cloudsim-4.0.jar" -OutFile "lib\cloudsim-4.0.jar"
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar" -OutFile "lib\commons-math3-3.6.1.jar"
```

**Linux / macOS:**
```bash
mkdir -p lib
curl -L -o lib/cloudsim-4.0.jar https://repo1.maven.org/maven2/org/cloudbus/cloudsim/cloudsim/4.0/cloudsim-4.0.jar
curl -L -o lib/commons-math3-3.6.1.jar https://repo1.maven.org/maven2/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar
```

You can also download the JARs manually from the CloudSim releases page and Apache Commons. Use only the plain JARs, not the `-sources`, `-javadoc`, or `-tests` versions.

## Compile and run

**Windows (PowerShell):**
```powershell
mkdir out -ErrorAction SilentlyContinue
$cp = "lib\cloudsim-4.0.jar;lib\commons-math3-3.6.1.jar"
javac -cp $cp -d out src\sched\*.java
java -cp "out;$cp" sched.Compare
```

**Linux / macOS:**
```bash
mkdir -p out
CP="lib/cloudsim-4.0.jar:lib/commons-math3-3.6.1.jar"
javac -cp "$CP" -d out src/sched/*.java
java -cp "out:$CP" sched.Compare
```

> The wildcard form `lib/*` can fail in PowerShell, so the jars are listed explicitly.

A full run (10 runs x 7 algorithms) can take a few minutes because GA, PSO and ACO do thousands of evaluations. For a quick test, set `RUNS = 3` in `Compare.java`.

## Output

Files are written to the folder you run the command from.

| File | Contents |
|---|---|
| `run_01.png` ... `run_10.png` | Makespan bar chart per run (a new random workload each run), best algorithm outlined |
| `summary_makespan.png` | Mean makespan relative to the best of each run (1.0 = best) |
| `summary_time.png` | Scheduling time, log scale |
| `summary_balance.png` | Load imbalance across VMs (lower = more balanced) |
| `summary_completion.png` | Mean task completion time |
| `results.csv` | All raw numbers |

The console also prints a table with these columns:

| Column | Meaning |
|---|---|
| MeanRel | Mean makespan divided by the best makespan of each run. Closer to 1.0 is better. |
| Wins | Number of runs where the algorithm tied for best |
| AvgCT | Mean task completion time |
| Imbal | `(max VM load - min VM load) / mean VM load` |
| Time ms | Time the algorithm took to compute its schedule |
| Score | Weighted combination of quality and speed (lower is better) |

## Configuration

Edit the constants and recompile.

| What | Where | Default |
|---|---|---|
| Number of runs | `Compare.java`: `RUNS` | 10 |
| Number of tasks | `Compare.java`: `TASKS` | 100 |
| Number of VMs | `Compare.java`: `VMS` | 20 |
| Quality vs speed weight | `Compare.java`: `W_QUALITY` | 0.7 |
| VM speeds (MIPS) | `Runner.java`: `mipsOptions` | 500, 1000, 1500, 2000 |
| Task length range (MI) | `Runner.java`: `1000 + rnd.nextInt(19000)` | 1000 to 19999 |
| GA parameters | `GA.java`: `pop`, `gens`, `cx`, `mut` | 60, 200, 0.8, 0.02 |
| PSO parameters | `PSO.java`: `swarm`, `iters`, `w`, `c1`, `c2` | 40, 200, 0.7, 1.5, 1.5 |
| ACO parameters | `ACO.java`: `ants`, `iters`, `alpha`, `beta`, `rho` | 30, 100, 1.0, 2.0, 0.1 |

`W_QUALITY = 1.0` means only makespan matters. Lower values favor fast scheduling.

## How it works

1. `Runner` creates a CloudSim datacenter, VMs, and cloudlets. The workload seed is the run number, so every algorithm sees identical tasks within a run.
2. It builds the ETC matrix and passes it to the algorithm's `schedule()` method, which returns `map[task] = vm`.
3. The mapping is applied with `broker.bindCloudletToVm(...)` and CloudSim simulates execution (space-shared VMs, one task at a time).
4. `Compare` collects makespan, completion time, load imbalance, and scheduling time, converts makespans to relative values, draws charts, and prints the verdict.

## Adding your own algorithm

1. Create `src/sched/MyAlgo.java` implementing `Scheduler`:
```java
package sched;
public class MyAlgo implements Scheduler {
    public String name() { return "MyAlgo"; }
    public int[] schedule(double[][] etc) {
        int[] map = new int[etc.length];
        // fill map[task] = vm
        return map;
    }
}
```
2. Add `new MyAlgo()` to the list in `Compare.algos(...)`.
3. Recompile and run.

## Notes and limitations

- Results come from a simulation with simplified network and hardware models.
- GA, PSO, and ACO results depend heavily on their parameters. Give them comparable evaluation budgets for a fair comparison.
- With small differences (around 1%), treat algorithms as tied rather than declaring a winner.
- CloudSim keeps static state. If you see odd results after the first simulation, run one algorithm per execution.
- If `Log.disable()` does not compile in your CloudSim version, use `Log.setDisabled(true)` in `Runner.java`.

## References

- CloudSim: https://github.com/Cloudslab/cloudsim
- Calheiros et al., "CloudSim: a toolkit for modeling and simulation of cloud computing environments and evaluation of resource provisioning algorithms", *Software: Practice and Experience*, 2011.


