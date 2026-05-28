# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

AstroLog is a Java SE 17 + Swing + MySQL 8.0 desktop application for astronomical observation logging and star management. Currently in **design phase** — no source code exists yet. Target: ~100 Java files, ~14,100 lines of code (3x the reference project `租车系统/`).

## Key Documents

| Document                            | Purpose                                                                                  |
| ----------------------------------- | ---------------------------------------------------------------------------------------- |
| `AstroLog-Design.md`                | Original preliminary design                                                              |
| `docs/01-软件需求规格说明书-SRS.md` | Software Requirements Specification (IEEE 830 framework, 16 use cases, 13 modules)       |
| `docs/02-架构设计文档-SAD.md`       | Architecture Design Document (3-layer, 11 DB tables, package structure, design patterns) |
| `docs/03-开发执行计划.md`           | Development Execution Plan (3 phases, 9 sub-stages)                                      |
| `docs/04-测试与审核流程.md`         | Testing & Review Process (test pyramid, ~165 cases, 4 review gates)                      |

## Architecture

- **Pattern:** Three-layer architecture — `ui` (Swing) → `service` (business logic) → `dao` (JDBC)
- **Package root:** `com.astrolog`
- **Database:** 11 tables (5 core + 6 extension), InnoDB, utf8mb4, no ORM (raw JDBC via PreparedStatement)
- **Key dependencies:** JFreeChart 1.5.4, JasperReports 6.20, jBCrypt 0.4, Gson 2.10, JUnit 5, Mockito
- **Design patterns in use:** DAO, Template Method (BaseDao), Strategy (export/chart types), Observer (theme switching), Singleton (DBUtil, ThemeManager), Decorator (Validator chain), Factory (Service/Chart creation)
- **Build tool:** Maven (not yet set up)

## Reference Project

`租车系统/租车/` — a car rental system (32 Java files, ~4,200 lines, 2 DB tables). AstroLog must be 3x this size. Reference its `DBUtil` connection pool pattern and `BaseDao` template method approach.

## Development Workflow

3 phases with 9 sub-stages, each ending with 4 human review gates (G1 code review, G2 feature acceptance, G3 test coverage >70%, G4 doc sync):

1. **Phase 1 — Foundation:** S1 project skeleton + S2 user auth system
2. **Phase 2 — Feature Slices:** S3 star body mgmt → S4 equipment mgmt → S5 observation mgmt → S6 visualization
3. **Phase 3 — Extension & Finalize:** S7 night sky + sites → S8 Messier marathon + constellation culture → S9 reports + polish

## Testing

- **Unit:** JUnit 5 + Mockito (80 cases, service/utils layers)
- **Integration:** JUnit 5 + real MySQL (40 cases, DAO + service+DAO)
- **Manual UI:** ~30-item checklist for Swing panels
- **Coverage target:** >70% line coverage via JaCoCo

## Build Commands (to be configured)

```
mvn clean compile      # compile
mvn test               # run all tests
mvn jacoco:report      # coverage report
java -jar target/astrolog.jar   # run application
```

## AI Collaboration Model

Full AI assistance with human review gates at each stage. AI generates code frameworks and boilerplate; human reviews core logic and validates against design docs. No code should be committed without passing G1-G4 gates.
