# MindGarden Microservices

Ein Spring Boot Microservices-Projekt mit Eureka Service Discovery, zentraler Konfiguration und synchroner REST-Kommunikation über OpenFeign.

---

## Architektur

```
                        ┌─────────────────┐
                        │  Config Server  │  :8888
                        │  (classpath)    │
                        └────────┬────────┘
                                 │ lädt Konfiguration
          ┌──────────────────────┼──────────────────────┐
          ▼                      ▼                       ▼
┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
│ Discovery Server │   │ Customer Service │   │  Fraud Service   │
│  (Eureka)  :8761 │   │      :8090       │   │      :8091       │
└──────────────────┘   └────────┬─────────┘   └──────────────────┘
                                │ OpenFeign (service discovery)
                                └──────────────────────▶ fraud-service

(Geplant / noch leer)
┌──────────────┐   ┌────────────────────┐   ┌─────────────────┐
│  API Gateway │   │   Order Service    │   │  Notifications  │
│  (leer)      │   │   (leer)  :8081    │   │  (leer)  :8082  │
└──────────────┘   └────────────────────┘   └─────────────────┘
```

---

## Services im Überblick

| Service            | Port | Status        | Beschreibung                                          |
|--------------------|------|---------------|-------------------------------------------------------|
| `config-server`    | 8888 | fertig        | Zentrale Konfiguration (native classpath)             |
| `discovery-server` | 8761 | fertig        | Eureka Service Registry                               |
| `customer`         | 8090 | fertig        | CRUD + Statusverwaltung für Kunden, ruft Fraud auf    |
| `fraud`            | 8091 | fertig        | Fraud-Prüfung, Blacklist, Check-Historie              |
| `shared`           | –    | fertig        | Gemeinsame DTOs (`FraudCheckRequest/Response`)        |
| `api-gateway`      | –    | Stub (leer)   | Noch kein Code                                        |
| `order`            | 8081 | Stub (leer)   | Nur leerer Service + ApplicationClass                 |
| `notifications`    | 8082 | Stub (leer)   | Nur leerer Service + ApplicationClass                 |
| `frontend`         | –    | Stub (leer)   | pom.xml angelegt, kein Code                          |

---

## Was der Customer Service kann

**Basis-CRUD:**
- `POST /api/v1/customers` — Kunde anlegen (löst automatisch Fraud-Check aus)
- `GET /api/v1/customers/{id}` — Kunde per ID abrufen
- `GET /api/v1/customers` — Alle Kunden abrufen
- `PUT /api/v1/customers/{id}` — Kunde aktualisieren
- `GET /api/v1/customers/search?email=` — Kunde per E-Mail suchen

**Statusverwaltung:**
- `POST /api/v1/customers/{id}/activate` — Kunde aktivieren
- `POST /api/v1/customers/{id}/deactivate` — Kunde deaktivieren
- `PATCH /api/v1/customers/{id}/block` — Kunde sperren

**Kundenstatusmodell:** `ACTIVE` → `INACTIVE` ↔ `ACTIVE`, `ACTIVE` → `BLOCKED`

**Validierungen & Fehlerbehandlung:**
- E-Mail muss eindeutig sein (409 Conflict)
- Doppelte Statusübergänge werden abgelehnt (409 Conflict)
- Bean Validation auf allen Request-DTOs (400 Bad Request)
- Fraud-Check blockiert Registrierung wenn Status `REJECTED`

---

## Was der Fraud Service kann

**Fraud-Check:**
- `POST /api/v1/fraud-check` — Prüft ob ein Kunde betrügerisch ist
- `GET /api/v1/fraud-check/{customerId}/history` — Prüfhistorie eines Kunden

**Blacklist:**
- `POST /api/v1/blacklist/{customerId}?reason=` — Kunde auf Blacklist setzen
- `DELETE /api/v1/blacklist/{customerId}` — Kunde von Blacklist entfernen

**Fraud-Regeln (FraudRuleEvaluator):**
1. Ist der Kunde auf der Blacklist? → `REJECTED`
2. Mehr als 3 Checks in 24 Stunden? → `REJECTED`
3. Sonst → `APPROVED`

---

## Projekt starten

### Voraussetzungen

- Java 17+
- Maven 3.8+
- Docker + Docker Compose

### 1. Infrastruktur starten

```bash
cd docker
# .env Datei anlegen:
cat > .env <<EOF
POSTGRES_USER=mindgarden
POSTGRES_PASSWORD=password
PGADMIN_DEFAULT_EMAIL=admin@mindgarden.com
PGADMIN_DEFAULT_PASSWORD=admin
EOF

docker compose up -d postgres pgadmin
```

Die Datenbanken `customers` und `fraud` werden automatisch per `init.sql` angelegt.

| Tool        | URL                        | Login                        |
|-------------|----------------------------|------------------------------|
| pgAdmin     | http://localhost:5050      | admin@mindgarden.com / admin |

### 2. Services in der richtigen Reihenfolge starten

Die Startreihenfolge ist wichtig, da die Services auf Config- und Discovery-Server warten:

```bash
# 1. Config Server (zuerst – alle anderen holen hier ihre Konfiguration)
cd config-server && mvn spring-boot:run

# 2. Discovery Server (Eureka)
cd discovery-server && mvn spring-boot:run

# 3. Fraud Service
cd fraud && mvn spring-boot:run

# 4. Customer Service (zuletzt – ruft Fraud per Feign auf)
cd customer && mvn spring-boot:run
```

### 3. Services prüfen

- Eureka Dashboard: http://localhost:8761
- Customer API: http://localhost:8090/api/v1/customers
- Fraud API: http://localhost:8091/api/v1/fraud-check

### 4. Beispiel-Request

```bash
# Kunde anlegen
curl -X POST http://localhost:8090/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstname": "Max",
    "lastname": "Mustermann",
    "email": "max@example.de",
    "phone": "+49 151 12345678",
    "address": {
      "street": "Musterstraße",
      "houseNumber": "1",
      "zipCode": "10115",
      "city": "Berlin",
      "country": "Deutschland"
    }
  }'
```

---

## Tests

Der Customer Service hat Integrationstests mit Testcontainers (echte PostgreSQL-Instanz):

```bash
cd customer && mvn test
```

Getestete Szenarien: Customer anlegen, Adresse mitgeben, doppelte E-Mail, Statusübergänge (activate/deactivate/block), Suche per E-Mail.

---

## Tech Stack

| Technologie              | Version  | Verwendung                          |
|--------------------------|----------|-------------------------------------|
| Spring Boot              | 3.5.x    | Application Framework               |
| Spring Cloud             | 2025.0.x | Config Server, Eureka, OpenFeign    |
| Spring Data JPA          | –        | Datenbankzugriff                    |
| PostgreSQL               | –        | Datenbank (customers, fraud)        |
| Lombok                   | –        | Boilerplate-Reduktion               |
| Testcontainers           | –        | Integrationstests mit echter DB     |
| Docker Compose           | –        | Lokale Infrastruktur                |
| OWASP Dependency-Check   | 10.0.4   | Sicherheits-Scan der Dependencies   |

---

## Nächste Schritte

- [ ] **API Gateway** implementieren (Spring Cloud Gateway) — zentraler Einstiegspunkt, Routing zu den Services
- [ ] **Order Service** implementieren — Bestellung anlegen, Kundenstatus prüfen, Fraud-Check auslösen
- [ ] **Notification Service** implementieren — z.B. E-Mail bei Registrierung oder Statusänderung (Maildev läuft bereits in Docker)
- [ ] **Asynchrone Kommunikation** einführen — aktuell läuft alles synchron über REST/Feign; für Bestellungen und Benachrichtigungen wäre Kafka oder RabbitMQ sinnvoll
- [ ] **Frontend** entwickeln — Modul ist angelegt, aber leer
- [ ] **Bug im FraudCheckService** beheben — `history.setFraudStatus(FraudStatus.APPROVED)` wird hardcoded gesetzt, unabhängig vom tatsächlichen Ergebnis des Evaluators
- [ ] **CustomerStatus-Übergang** vervollständigen — `TODO` im Code: BLOCKED-Kunden können noch nicht aktiviert werden
- [ ] **Integrationstests für Fraud Service** schreiben
- [ ] **Docker-Compose** um alle Services erweitern (aktuell nur DB + pgAdmin)

---

## Projektstruktur

```
microservices/
├── config-server/          # Zentrale Konfiguration für alle Services
├── discovery-server/       # Eureka Service Registry
├── shared/                 # Gemeinsame DTOs (FraudCheckRequest/Response)
├── customer/               # Customer Service (fertig + getestet)
├── fraud/                  # Fraud Service (fertig)
├── order/                  # Order Service (Stub)
├── notifications/          # Notification Service (Stub)
├── api-gateway/            # API Gateway (Stub)
├── frontend/               # Frontend (Stub)
├── docker/
│   ├── docker-compose.yml  # PostgreSQL, pgAdmin, Maildev, MongoDB
│   └── scripts/init.sql    # DB-Initialisierung
└── pom.xml                 # Root-POM (Multi-Module Maven)
```
