# Pearl Nails Scheduling

A salon scheduling and administration platform built for managing services,
technicians, working hours, pricing, and customer appointments.

## Project Status

Phase One: Internal salon administration prototype.

## Technology Stack

### Frontend

- Next.js
- TypeScript
- Tailwind CSS

### Backend

- Java
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway

### Development Infrastructure

- Docker Compose
- GitHub
- JUnit
- Testcontainers

## Repository Structure

```text
backend/    Spring Boot REST API
frontend/   Next.js administration interface
docs/       Architecture and business documentation
```


## Run locally

From the repository root, create your local configuration if it does not exist:

```bash
cp -n .env.example .env
docker compose up -d --wait postgres
cd backend
./mvnw spring-boot:run
```

The backend loads the root `.env` directly; you do not need to source it.
Keep its entries in simple, unquoted `KEY=value` format, as Spring reads it
as a properties file. Keep `DB_PASSWORD` and `POSTGRES_PASSWORD` equal.
Verify startup at <http://localhost:8080/actuator/health>.

### Database password troubleshooting

PostgreSQL stores its password in the Docker volume. Changing
`POSTGRES_PASSWORD` in `.env` or recreating the container does not update
the password in an existing database.

To update the existing local database password while preserving data, run
from the repository root:

```bash
docker compose exec postgres psql -U pearl_admin -d pearl_nails
```

At the psql prompt, run `\password pearl_admin` and enter the password
from `.env` twice, then run `\q`. Substitute your configured username and
database if you changed them. Restart the backend afterward.

If an error names `pearl_admin\r`, a shell variable contains a Windows
carriage return. In the terminal where you run Maven, clear previously
exported database overrides, then let Spring load `.env`:

```bash
unset DB_URL DB_USERNAME DB_PASSWORD
unset SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD
./mvnw spring-boot:run
```

Do not use `docker compose down -v` to fix authentication; it deletes the
database volume.
