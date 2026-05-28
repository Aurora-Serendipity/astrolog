# AstroLog — Astronomical Observation Log & Star Management System

AstroLog is a Java SE 17 desktop application for astronomical observation logging, celestial body management, and night sky exploration. Built with Swing + MySQL 8.0.

## Features

| Module                     | Description                                                                     |
| -------------------------- | ------------------------------------------------------------------------------- |
| User Authentication        | Registration, login, BCrypt encryption, account lockout after 5 failures        |
| Celestial Body Management  | CRUD, multi-condition search, batch CSV import, favorites                       |
| Observation Records        | CRUD with multi-select equipment/tags/sites, composite query                    |
| Equipment Management       | CRUD, maintenance logs, usage statistics                                        |
| Statistics & Visualization | Bar/pie/line/radar charts, observation calendar heatmap, constellation star map |
| Night Sky Tonight          | Visible constellation & bright star recommendations based on date + location    |
| Messier Marathon           | Full 110-object catalog, observation progress tracking, completion certificate  |
| Constellation Culture      | 88 constellations with Chinese/Greek mythology, interactive star map            |
| Report Export              | HTML & PDF annual observation reports, Messier certificate                      |
| System Settings            | Dark/Light/Starry themes, font size adjustment, data backup/restore             |

## Tech Stack

- **Language:** Java SE 17 (LTS)
- **UI:** Swing + JFreeChart 1.5.4
- **Database:** MySQL 8.0 + raw JDBC (no ORM)
- **Reports:** OpenPDF
- **Auth:** jBCrypt 0.4
- **Build:** Maven 3.8+
- **Test:** JUnit 5 + Mockito (135 tests)

## Architecture

Three-layer architecture: `ui` (Swing) → `service` (business logic) → `dao` (JDBC)

```
com.astrolog
├── config/         — AppConfig, ThemeConfig, DBConfig
├── model/          — 13 entities + 5 enums
├── dao/            — 11 DAOs (BaseDao template method)
├── service/        — 11 Services
├── ui/             — Frames, panels, dialogs, components
└── util/           — DBUtil, ChartUtil, Validator, ExportUtil, etc.
```

**Design patterns:** DAO, Template Method, Strategy, Observer, Singleton, Decorator, Factory

## Quick Start

### Prerequisites

- JDK 17+
- MySQL 8.0+
- Maven 3.8+

### Setup

```bash
# 1. Clone
git clone https://github.com/Aurora-Serendipity/astrolog.git
cd astrolog

# 2. Initialize database
mysql -u root -p < sql/init.sql

# 3. Configure database connection
# Edit src/main/resources/db.properties with your MySQL credentials

# 4. Optionally load seed data
mysql -u root -p astrolog < sql/seed_bodies.sql

# 5. Build
mvn clean package

# 6. Run
java -jar target/astrolog-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Project Stats

- **99 Java source files** (~15,000+ lines of code)
- **135 automated tests** (0 failures)
- **11 database tables** (InnoDB, utf8mb4)
- **13 functional modules**

## Documentation

- [Software Requirements Specification (SRS)](docs/01-软件需求规格说明书-SRS.md)
- [Architecture Design Document (SAD)](docs/02-架构设计文档-SAD.md)
- [Development Execution Plan](docs/03-开发执行计划.md)
- [Testing & Review Process](docs/04-测试与审核流程.md)
- [User Manual](docs/用户操作手册.md)
- [Deployment Guide](docs/安装部署指南.md)

## License

MIT License — see [LICENSE](LICENSE) file for details.
