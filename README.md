# fintrack

[![MIT license](https://img.shields.io/badge/license-MIT-blue.svg)](./LICENSE.md)

A personal finance tracker for importing, analyzing, and visualizing bank transactions.

## Prerequisites

### Node, npm or pnpm

It's recommended to use [nvm (Node version Manager)](https://github.com/nvm-sh/nvm).

- `node 24.16.0` or higher in combination with
  - `npm 11.13.0` or higher or
  - `pnpm 11.24.0` or higher, used in this repository

Install pnpm by running:

```bash
npm install -g pnpm@11.24.0
```

### Info for npm and pnpm

This repo uses `pnpm` as package manager.
You can also use `npm` for your local work but changes will be made by `pnpm` only.

### Angular CLI

- `@angular/cli 22.2.0` or higher

Install @angular/cli by running:

```bash
pnpm install -g @angular/cli@22
```

### Java

- `jdk 25` or higher

### Docker (when running services within docker)

- `docker 28.3.2` or higher

## Getting started

### Clone project

```bash
git clone https://github.com/inpercima/fintrack/
cd fintrack
```

### Install tools

Some tools are both used by backend and frontend.
Run the following command to install:

```bash
pnpm install
```

## Development Mode

### Starting the application in development

For development, you need a running MySQL instance.
Start one via Docker (see [Docker Guide](./docker/README.md)) or use an external database.

Database schema and seed data are managed by [Flyway](https://flywaydb.org/) and applied automatically on application
startup (see `backend/src/main/resources/db/migration/`).

#### Option 1: Start everything with one command (Recommended)

```bash
pnpm start
```

This command will start both backend and frontend concurrently.

#### Option 2: Start backend and frontend separately

Use two separate terminals for more control and better log visibility:

**Terminal 1 - Backend:**

```bash
cd backend
./mvnw
```

**Terminal 2 - Frontend:**

```bash
cd frontend
pnpm start
```

For detailed development setup and configuration options, check:

- [Frontend Development Guide](./frontend/README.md)
- [Backend Development Guide](./backend/README.md)
- [Docker Setup for Development](./docker/README.md)

### Access the application

- **Frontend:** [http://localhost:4200/](http://localhost:4200/)
- **Backend API:** [http://localhost:8080/](http://localhost:8080/)
- **phpMyAdmin:** [http://localhost:81](http://localhost:81) (or configured `PHPMYADMIN_PORT` in `.env`)

## Production Mode

### Prerequisites for production

- Docker and Docker Compose installed on the server
- nginx configuration files (see `docker/nginx/`)
- SSL certificates for HTTPS

### Build process

1. **Prepare frontend environment:**

   Check for the existence of `environment.prod.ts` as described in [Frontend Guide](./frontend/README.md).

2. **Build the backend (includes frontend):**

   ```bash
   cd backend
   ./mvnw clean package -Pprod
   ```

   The prod profile automatically builds the frontend via pnpm and bundles it into the JAR. This creates `fintrack-<VERSION>.jar` in the `target/` directory.

### Deployment

1. **Prepare configuration files:**

   Copy the following files to your server:
   - `.env` (Docker environment configuration)
   - `docker-compose.yml` and `docker-compose.prod.yml`
   - `fintrack-<VERSION>.jar` (from backend/target)
   - `application-prod.yml` (Spring Boot production configuration)

2. **Configure for your environment:**
   - Modify `.env` with your production settings
   - Update `application-prod.yml` with production database credentials and API keys
   - Configure nginx with your domain and SSL certificates, for this create the folder `nginx/` in `docker/` and add a `nginx.conf` file

3. **Deploy and run:**

   ```bash
   docker compose --project-name fintrack -f docker-compose.yml -f docker-compose.prod.yml up -d --build
   ```

   This starts MySQL, the Spring Boot webapp (with embedded frontend), and nginx as reverse proxy.

   Database migrations are applied automatically by Flyway on startup.

For detailed production deployment instructions, see [Docker Guide](./docker/README.md).



Erster read-only Test für den Zugriff auf ein eigenes GLS-Konto über FinTS/HBCI und HBCI4Java.

## Voraussetzungen

- Java 25
- Maven 3.9+
- GLS-Onlinebanking-Zugang
- FinTS/HBCI PIN/TAN Zugang

Die GLS veröffentlicht aktuell für PIN/TAN:

- BLZ: `43060967`
- FinTS: `3.0`
- Host: `fints1.atruvia.de`
- Port: `443`
- Filter: `Base64`

## WICHTIG

Dieses Projekt ist absichtlich read-only. Es enthält aktuell keinen Code zum Ausführen von Überweisungen.

Die Zugangsdaten niemals in Git committen.

## Konfiguration

Zum Beispiel im Terminal:

```bash
export GLS_USER_ID='DEIN_VR_NETKEY_ODER_ALIAS'
export GLS_PIN='DEINE_FINTS_PIN'
export GLS_PASSPORT_PASSWORD='EIN_LOKALES_PASSWORT'
```

Dann:

```bash
./mvnw spring-boot:run
```

oder, falls kein Maven Wrapper vorhanden ist:

```bash
mvn spring-boot:run
```

Beim ersten Start wird unter `data/gls-passport.dat` ein lokaler FinTS-Passport angelegt.

## Hinweis zur GLS-PIN

Die GLS unterscheidet Onlinebanking-PIN/TAN und FinTS/HBCI-Verfahren. Falls dein Zugang nicht akzeptiert wird, prüfen wir als Nächstes, welches konkrete FinTS-Verfahren für dein Konto freigeschaltet ist.

## Nächste Schritte

1. Verbindung testen
2. Konten sauber modellieren
3. Umsätze in eigene DTOs übertragen
4. REST-API hinzufügen
5. Zeitraum für Umsatzabfragen kontrollieren
6. Optional MySQL
7. Erst ganz am Ende über schreibende Bankoperationen sprechen
