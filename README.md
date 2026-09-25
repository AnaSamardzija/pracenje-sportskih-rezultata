# Praćenje rezultata amaterskih sportskih susreta

Web aplikacija za vođenje evidencije o amaterskim sportskim mečevima, praćenje
ličnih statistika i rang-lista unutar grupa. Aplikacija je izgrađena tako da radi
nad **dve nezavisne baze podataka** — **MariaDB** (relaciona) i **MongoDB**
(nerelaciona). Korisnik pri prijavi bira sa kojom bazom radi u tom trenutku, a
aplikacija se ponaša identično bez obzira na izbor.

> Opis funkcionalnosti, korisničkih uloga i planiranih endpointa nalazi se u
> [`PROJECT.md`](./PROJECT.md).

---

## Tehnologije

| Komponenta        | Verzija / alat             |
|-------------------|----------------------------|
| Jezik             | Java 21                    |
| Framework         | Spring Boot 4.0.x          |
| Build alat        | Maven                      |
| Relaciona baza    | MariaDB 11.4               |
| Nerelaciona baza  | MongoDB 8.0                |
| Migracije šeme    | Flyway (MariaDB)           |
| Autentifikacija   | Spring Security + JWT      |
| Dokumentacija API | springdoc-openapi (Swagger)|
| Kontejneri        | Docker + Docker Compose    |

---

## Preduslovi (šta treba instalirati)

Pre pokretanja je potrebno imati instalirano:

1. **JDK 21** — Docker se koristi samo za baze podataka; sama aplikacija
   se pokreće lokalno (iz razvojnog okruženja), pa je JDK neophodan.
   ```bash
   java -version   # treba da prikaže verziju 21
   ```
2. **Docker** i **Docker Compose** — [Docker Desktop](https://www.docker.com/products/docker-desktop/)
   (Windows/Mac) ili Docker Engine + plugin `docker compose` (Linux).
   ```bash
   docker --version
   docker compose version
   ```
3. **Git** — za kloniranje repozitorijuma.

---

## Kloniranje projekta

```bash
git clone https://github.com/AnaSamardzija/pracenje-sportskih-rezultata.git
cd pracenje-sportskih-rezultata
```

---

## Pokretanje baza podataka (Docker)

Obe baze se dižu jednom komandom iz korena projekta:

```bash
docker compose up -d
```

Time se pokreću dva kontejnera:

| Servis    | Image        | Port (host) | Kontejner                       |
|-----------|--------------|-------------|---------------------------------|
| MariaDB   | mariadb:11.4 | `3306`      | `dual-db-maria-db-container`    |
| MongoDB   | mongo:8.0    | `27017`     | `dual-db-mongodb-container`     |

Zaustavljanje baza (podaci ostaju):
```bash
docker compose down
```

Zaustavljanje uz brisanje podataka (čist start):
```bash
docker compose down -v
```

---

## Pokretanje aplikacije

Projekat se otvara u razvojnom okruženju (npr. IntelliJ IDEA). Kada se baze pokrenu
(prethodni korak) i okruženje završi učitavanje projekta, aplikacija se pokreće
klikom na dugme **Run** (▶) na glavnoj klasi `DualDatabaseAccessApplication`.

Aplikacija se podiže na **http://localhost:8080**.

Pri prvom pokretanju, klasa `DataInitializer` automatski kreira osnovne uloge i
permisije (u MariaDB) i podrazumevanog **admin** korisnika **u obe baze**:

| Polje    | Vrednost     |
|----------|--------------|
| username | `admin`      |
| password | `admin.123`  |

---

## Dokumentacija API-ja (Swagger)

Dok aplikacija radi, dokumentacija svih REST ruta dostupna je preko Swagger UI-ja na
**http://localhost:8080/doc**. Rute su grupisane po resursima, a za svaku je naveden opis,
ko sme da je pozove, statusi koje vraća i primer tela zahteva.

Rute se mogu isprobati direktno iz Swagger UI-ja:

1. U grupi **Auth** otvoriti `POST /api/v1/auth/login`, kliknuti **Try it out** i **Execute**
   (primer je admin nad MariaDB; za MongoDB promeniti `storageType` u `MONGODB`).
2. Kopirati `accessToken` iz odgovora, kliknuti dugme **Authorize** gore desno, uneti token
   (bez reči `Bearer`) i potvrditi.
3. Od tada se sve zaštićene rute pozivaju sa tim tokenom, nad bazom izabranom pri prijavi.

OpenAPI opis u JSON obliku je na http://localhost:8080/v3/api-docs.

---

## Struktura projekta (ukratko)

```
src/main/java/rs/ac/ni/pmf/marko/dualdb/
├── controller/      # REST kontroleri (jedinstveni za obe baze)
├── service/         # Poslovna logika (jedinstvena za obe baze)
├── model/           # Model nezavisan od baze (User, ...)
├── dto/             # DTO objekti i mapperi ka modelu
├── storage/         # Apstrakcija nad bazama
│   ├── DataStorage, StorageResolver, CurrentStorageTypeProvider
│   └── user/        # MariaDbUserStorage, MongoDbUserStorage
├── data/
│   ├── StorageType.java          # enum: MARIADB / MONGODB
│   ├── mariadb/ {entity,mapper,repository}
│   └── mongodb/ {document,mapper,repository}
├── security/        # JWT, Spring Security, izbor baze pri autentifikaciji
└── config/          # DataInitializer
```

Kontroleri i servisi su jedinstveni za obe baze, a razdvajanje po bazama nastupa tek
u `storage` i `data` delu.
