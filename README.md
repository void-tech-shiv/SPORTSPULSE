# SportsPulse

A complete CLI-based Sports Performance Analytics Platform focused heavily on Data Structures and Algorithms (DSA) as an academic project.

## Objective
SportsPulse manages players, teams, competitions, matches, and match statistics. It provides advanced algorithms to offer fast player searching, report indexing, commentary analysis, player comparison, and more. 
Every DSA algorithm is directly connected to a meaningful sports analytics feature.

## Technology Used
- **Language**: Java 17+
- **Interface**: CLI / Terminal
- **Persistence**: File I/O (CSV / Text files)
- **Architecture**: Modular Object-Oriented Design (CLI -> Service -> Algorithm -> Model -> File Persistence)
- **No External Dependencies** (No Spring Boot, No React, No external DB)

## Features & DSA Mapping

| Module | Algorithm            | SportsPulse Feature        |
| ------ | -------------------- | -------------------------- |
| M1     | KMP                  | Player/Event Search        |
| M1     | Aho-Corasick         | Multi-event Detection      |
| M2     | Suffix Array         | Report Indexing            |
| M2     | LCP                  | Report Similarity          |
| M3     | Levenshtein          | Name Correction            |
| M3     | Bitmask DP           | Team Selection             |
| M4     | Dinic                | Resource Allocation        |
| M4     | Matching             | Player-Position Assignment |
| M5     | 3-SAT                | Selection Constraints      |
| M5     | Vertex Cover         | Conflict Analysis          |
| M6     | Randomized QuickSort | Player Ranking             |
| M6     | Reservoir Sampling   | Live Event Sampling        |
| M6     | Blelloch Scan        | Cumulative Statistics      |
| M6     | Parallel Reduce      | Data Aggregation           |

## How to Compile and Run
For Windows users, simply execute the `run.bat` file in the root directory:
```bash
run.bat
```

Alternatively, compile and run manually:
```bash
# Create bin directory
mkdir bin
# Compile
javac -d bin src/sportspulse/**/*.java src/sportspulse/*.java
# Run
java -cp bin sportspulse.Main
```
