# SportsPulse

SportsPulse is a Java 17+ sports performance analytics platform with both CLI and web interfaces. The project focuses on Data Structures and Algorithms (DSA) while supporting persistent sports data.

## Objective
SportsPulse manages players, teams, competitions, matches, match events, and sports analytics. DSA algorithms are connected to meaningful sports analytics features such as searching, report indexing, player comparison, allocation, ranking, and aggregation.

## Technology Used
- **Language:** Java 17+
- **Interfaces:** CLI + built-in Java HTTP web server
- **Primary Database:** Neon PostgreSQL through JDBC
- **Fallback Persistence:** CSV/Text files when `DATABASE_URL` is not configured
- **Build:** Windows batch scripts or Maven
- **Architecture:** CLI/Web -> DAO -> PostgreSQL or CSV fallback -> Models/Algorithms

## Neon PostgreSQL
The application uses the `DATABASE_URL` environment variable. Use the PostgreSQL connection string from the Neon dashboard. Never commit the real connection string because it contains a database password.

### Windows PowerShell
```powershell
$env:DATABASE_URL="postgresql://USER:PASSWORD@HOST/sportspulse?sslmode=require"
.\compile.bat
.\run-web.bat
```

For the CLI:
```powershell
$env:DATABASE_URL="postgresql://USER:PASSWORD@HOST/sportspulse?sslmode=require"
.\run.bat
```

The first database connection automatically ensures the required tables and indexes exist. If `DATABASE_URL` is missing or PostgreSQL is temporarily unavailable, the current CSV files are used as a fallback.

## Maven
The PostgreSQL JDBC driver is declared in `pom.xml`.

Compile:
```bash
mvn clean compile
```

Run web server:
```bash
mvn exec:java -Dexec.mainClass=sportspulse.web.WebMain
```

Run CLI:
```bash
mvn exec:java -Dexec.mainClass=sportspulse.Main
```

## Features & DSA Mapping

| Module | Algorithm | SportsPulse Feature |
| --- | --- | --- |
| M1 | KMP | Player/Event Search |
| M1 | Aho-Corasick | Multi-event Detection |
| M2 | Suffix Array | Report Indexing |
| M2 | LCP | Report Similarity |
| M3 | Levenshtein | Name Correction |
| M3 | Bitmask DP | Team Selection |
| M4 | Dinic | Resource Allocation |
| M4 | Matching | Player-Position Assignment |
| M5 | 3-SAT | Selection Constraints |
| M5 | Vertex Cover | Conflict Analysis |
| M6 | Randomized QuickSort | Player Ranking |
| M6 | Reservoir Sampling | Live Event Sampling |
| M6 | Blelloch Scan | Cumulative Statistics |
| M6 | Parallel Reduce | Data Aggregation |

## Windows Quick Start
```bat
run.bat
```

`compile.bat` / `run.bat` automatically download the PostgreSQL JDBC driver into `lib/` when needed. The downloaded JAR and local `.env` files are ignored by Git.
