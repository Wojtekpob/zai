# Telemetria Sensorów IoT – Serwer REST API (ZAI 26Z)

Aplikacja serwerowa w architekturze REST API zgodna ze specyfikacją OpenAPI 3.1 (`zai-api-26z.yaml`) oraz standardem RFC 9457 Problem Details (`application/problem+json`).

## Wymagania systemowe
- Java JDK 17+ (LTS)
- Apache Maven 3.9+ (lub dołączony skrypt `mvnw` / `mvnw.cmd`)
- Python 3.10+ (do uruchamiania generatora pomiarów)

## Uruchomienie lokalne

### 1. Budowanie projektu
```bash
./mvnw clean package -DskipTests
```
Na systemie Windows (PowerShell):
```powershell
.\mvnw.cmd clean package -DskipTests
```

### 2. Uruchomienie serwera
```powershell
.\mvnw.cmd spring-boot:run
```
LUB bezpośrednio z pliku JAR:
```powershell
java -jar target/zai-backend-1.0.0.jar
```

Domyślny port aplikacji: `8080`.
Adres sprawdzania dostępności: `http://localhost:8080/api/health`.

### 3. Domyślne konto administratora
Podczas pierwszego uruchomienia system automatycznie inicjalizuje konto testowe administratora:
- **Login:** `admin`
- **Hasło:** `Admin123!`

Dane te można zmienić zmiennymi środowiskowymi `APP_ADMIN_USERNAME` oraz `APP_ADMIN_PASSWORD`.

## Dokumentacja API i Swagger UI
Po uruchomieniu serwera w przeglądarce dostępne są:
- Interaktywny interfejs **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- Surowa specyfikacja OpenAPI w formacie JSON: `http://localhost:8080/v3/api-docs`

## Dostęp do bazy danych

Domyślnie aplikacja korzysta z wbudowanej relacyjnej bazy danych H2 pracującej w trybie kompatybilności z PostgreSQL (`MODE=PostgreSQL`).
Plik bazy danych utrwalany jest w katalogu `./data/zai_db.mv.db`.

### Przeglądarka bazodanowa (H2 Web Console)
Gdy serwer działa, konsola WWW bazy danych dostępna jest pod adresem:
- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:./data/zai_db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE`
- **User Name:** `sa`
- **Password:** *(pole puste)*

Wszystkie tabele, klucze obce oraz indeksy są zarządzane przez migracje **Flyway** (`src/main/resources/db/migration/V1__init_schema.sql`).

## Zmienne środowiskowe

| Zmienna | Domyślna wartość | Opis |
| --- | --- | --- |
| `PORT` | `8080` | Port nasłuchiwania serwera HTTP |
| `SPRING_PROFILES_ACTIVE` | `default` (H2) lub `prod` (PostgreSQL) | Aktywny profil aplikacji |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:./data/zai_db;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE` | Connection string JDBC do bazy danych |
| `SPRING_DATASOURCE_USERNAME` | `sa` | Użytkownik bazy danych |
| `SPRING_DATASOURCE_PASSWORD` | *(puste)* | Hasło użytkownika bazy |
| `APP_ADMIN_USERNAME` | `admin` | Login konta administratora |
| `APP_ADMIN_PASSWORD` | `Admin123!` | Hasło konta administratora |
| `JWT_SECRET` | *(klucz domyślny)* | 256-bitowy sekret HMAC-SHA256 dla tokenów JWT |
| `JWT_EXPIRATION_SECONDS` | `86400` | Czas ważności tokenu Bearer w sekundach (24h) |

## Generator Danych i Emulator Czujników
Narzędzie konsolowe emulujące czujniki IoT znajduje się w katalogu `generator/`.
Szczegółowy opis parametrów i trybów pracy znajduje się w pliku `generator/README.md`.
