# SportsPulse CLI → Web Parity Audit

Branch: `web-feature-parity`

## Goal
Make the web interface expose the functionality that is actually implemented in the Java CLI/backend, without inventing UI for features that are only placeholders or README roadmap items.

## Feature parity matrix

| Area | Java/CLI status | Web status after audit |
|---|---|---|
| Player add/list/search/update/delete | Implemented | Implemented and wired to REST APIs |
| Team add/list/search/update/delete | Implemented | Implemented and wired to REST APIs |
| Match schedule/list/date search/update/delete | Implemented | Implemented and wired to REST APIs |
| Match Events | CLI recording is marked not implemented; web backend supports reading `events.csv` | Existing events are visible in Overview, Matches and Reports |
| Competition Management | Main CLI explicitly marks it not implemented and there is no Competition DAO/API | Not exposed as a fake working web feature |
| M1 String Algorithms | KMP + Rabin-Karp implemented | Real Java execution exposed in Algorithm Lab |
| M2 Suffix Structures | Suffix Array + LCP + indexed search implemented | Real Java execution exposed in Algorithm Lab |
| M3 Dynamic Programming | LIS + LCS + 0/1 Knapsack implemented | Real Java execution exposed in Algorithm Lab |
| M4 Network Flow | Dinic + Bipartite Matching implemented | Real Java execution exposed in Algorithm Lab |
| M5 NP-Completeness | 3-SAT + exact/approx Vertex Cover implemented | Real Java execution exposed in Algorithm Lab |
| M6 Optimization / Heuristics | Simulated Annealing + Genetic Algorithm implemented | Real Java execution exposed in Algorithm Lab |
| Performance Analytics | Top Scorers implemented | `/api/analytics/top-scorers` + Reports UI |
| Team Rankings / Player Trends | CLI explicitly says not fully implemented | Not presented as complete backend functionality |
| Player Compare / Rank / Performance menu items | CLI explicitly says not fully implemented | Not falsely presented as backend-complete features |
| Reports persistence | `reports.txt` only contains a header; no complete report DAO/API exists | Web Reports are generated from live players/teams/matches/events data |

## Problems found and fixed

1. Algorithm Lab module numbering did not match the CLI.
2. Frontend called `/api/algorithms/simulate`, but the server registered only `/api/algorithms/search`, making simulation routing invalid.
3. Algorithm UI used placeholder/fake logs rather than executing most Java algorithm classes.
4. Players, Teams and Matches REST APIs supported `PUT`, but the frontend had no Edit flow.
5. Match CLI search is date-based, but web filtering did not include the match date.
6. Player/team search fields did not expose all useful backend data fields.
7. Duplicate IDs were rejected in CLI flows but accepted by web POST handlers; REST APIs now return conflict responses.
8. Top Scorers existed in CLI analytics but had no dedicated web API/UI.
9. Reports navigation in older pages used `href="#"`; shared frontend logic routes it to `/reports.html`.

## Important README discrepancy

The root README lists Aho-Corasick, Levenshtein, Bitmask DP, Randomized QuickSort, Reservoir Sampling, Blelloch Scan and Parallel Reduce. Those algorithms are not present as implemented algorithm classes/methods in the current source tree, so this parity work intentionally does not create fake web executions for them.

## New/changed web endpoints

- `GET /api/algorithms/search?q=...`
- `GET /api/algorithms/run?module=m1&pattern=Mbappe`
- `GET /api/algorithms/run?module=m2&pattern=goal`
- `GET /api/algorithms/run?module=m3&budget=50`
- `GET /api/algorithms/run?module=m4`
- `GET /api/algorithms/run?module=m5`
- `GET /api/algorithms/run?module=m6`
- `GET /api/analytics/top-scorers`
- `GET /api/analytics/summary`

## Build note

`compile.bat` compiles `src\sportspulse\web\*.java`, so newly added web handlers are included automatically.
