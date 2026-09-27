# Aplikacija za praćenje rezultata amaterskih sportskih susreta

## Opis

Web aplikacija koja omogućava grupama prijatelja, kolega ili amaterskim klubovima
da vode evidenciju o međusobnim sportskim susretima u različitim sportovima, prate
svoje rezultate kroz vreme i porede se sa ostalim članovima grupe. Aplikacija
podržava i **pojedinačne sportove** (tenis, stoni tenis, šah) i **timske sportove**
(fudbal, košarka, odbojka).

Cilj je da grupa koja redovno igra neki sport ima jedno mesto gde se beleže svi
rezultati, vidi se ko je trenutno u najboljoj formi i kako je svako napredovao
tokom vremena — umesto da se rezultati pamte napamet ili razbacano po porukama.

---

## Korisničke uloge

- **Igrač** — registruje se, pridružuje se grupama, unosi rezultate mečeva u svojim
  grupama (ne mora sam da igra) i prati svoje statistike i poziciju na rang-listi.
- **Administrator grupe** — kreira grupu i upravlja njome: dodaje i uklanja članove,
  uređuje njena podešavanja i može da menja i briše sve mečeve u grupi. Svaki igrač
  koji kreira grupu postaje njen administrator.
- **Sistem administrator** — upravlja katalogom sportova i svim korisnicima.

---

## Glavne funkcionalnosti

Osnovni, obavezni deo aplikacije:

- Registracija, prijava i upravljanje korisničkim profilom
- Kreiranje grupa i pridruživanje postojećim grupama (npr. „Subotnji fudbal",
  „Tenis kvarta")
- Unos rezultata odigranog meča — igrač bira sport, protivnika i unosi rezultat, a
  sistem na osnovu rezultata određuje pobednika
- Pregled liste odigranih mečeva (svi mečevi, mečevi u okviru grupe, sopstveni
  mečevi)
- Rang-lista igrača unutar grupe i po sportu, na osnovu osvojenih bodova i broja
  pobeda
- Lični profil sa istorijom odigranih mečeva i osnovnom statistikom (broj pobeda,
  poraza i procenat uspešnosti)
- Mogućnost izbora baze podataka (relaciona ili nerelaciona) pri prijavi, pri čemu
  aplikacija radi identično bez obzira na izabranu bazu

---

## Rad sa dve baze podataka

Centralna ideja projekta je da ista aplikacija radi nad dve potpuno **nezavisne**
baze:

- **MariaDB** — relaciona baza (Spring Data JPA / Hibernate).
- **MongoDB** — nerelaciona baza (Spring Data MongoDB).

Korisnik **pri prijavi bira bazu** (`storageType`: `MARIADB` ili `MONGODB`). Izbor
se upisuje u JWT token, pa svi naredni zahtevi automatski rade nad izabranom bazom.
Baze su međusobno nezavisne — podaci uneti u jednu ne postoje u drugoj. Da bi se
prešlo na drugu bazu, korisnik se ponovo prijavljuje sa drugim `storageType`.

Kontroleri i servisi su jedinstveni za obe baze; razlika između relacione i
nerelacione baze postoji samo na nivou pristupa podacima (zasebni entiteti/dokumenti,
mapperi i repozitorijumi za svaku bazu).

### Uloge i permisije

- Globalne uloge su `USER` i `SYSTEM_ADMIN`. Uloga `GROUP_ADMIN` nije globalna, već važi
  za jednu grupu i čuva se u članstvu korisnika u toj grupi.
- Obe baze uloge i permisije čuvaju zasebno: u **MariaDB** su to tabele sa relacijama
  (korisnik → uloge → permisije), a u **MongoDB** kolekcije `roles` i `permissions`, na
  koje korisnički dokument pokazuje referencama.

---

## REST endpointi

> Bazna putanja: `/api/v1`. Sve rute osim prijave i registracije traže JWT token.
> Detaljan opis svake rute nalazi se u Swagger UI-ju na `http://localhost:8080/doc`.

### Autentikacija i sopstveni nalog — `/api/v1/auth`, `/api/v1/users/me`

| Metoda | Putanja                     | Opis                                 |
|--------|-----------------------------|--------------------------------------|
| POST   | `/api/v1/auth/register`     | Registracija novog igrača            |
| POST   | `/api/v1/auth/login`        | Prijava (JWT) uz izbor baze          |
| GET    | `/api/v1/users/me`          | Moj profil                           |
| PUT    | `/api/v1/users/me`          | Izmena profila (ime, prezime, email) |
| PUT    | `/api/v1/users/me/password` | Promena lozinke                      |

### Sportovi — `/api/v1/sports` (sistem administrator upravlja, ostali čitaju)

| Metoda | Putanja                       | Opis                                                                |
|--------|-------------------------------|---------------------------------------------------------------------|
| GET    | `/api/v1/sports`              | Lista aktivnih sportova (`?includeInactive=true` samo SYSTEM_ADMIN) |
| GET    | `/api/v1/sports/{id}`         | Detalji sporta (`?includeInactive=true` samo SYSTEM_ADMIN)          |
| POST   | `/api/v1/sports`              | Dodavanje (SYSTEM_ADMIN)                                            |
| PUT    | `/api/v1/sports/{id}`         | Izmena (SYSTEM_ADMIN)                                               |
| DELETE | `/api/v1/sports/{id}`         | Brisanje, sport postaje neaktivan (SYSTEM_ADMIN)                    |
| PATCH  | `/api/v1/sports/{id}/restore` | Vraćanje obrisanog sporta (SYSTEM_ADMIN)                            |

### Grupe — `/api/v1/groups`

| Metoda | Putanja                                | Opis                                               |
|--------|----------------------------------------|----------------------------------------------------|
| GET    | `/api/v1/groups`                       | Lista grupa (filteri: `?mine=true`, `?search=...`) |
| GET    | `/api/v1/groups/{id}`                  | Detalji grupe                                      |
| POST   | `/api/v1/groups`                       | Kreiranje grupe (kreator postaje GROUP_ADMIN)      |
| PUT    | `/api/v1/groups/{id}`                  | Izmena podešavanja (GROUP_ADMIN te grupe)          |
| DELETE | `/api/v1/groups/{id}`                  | Brisanje grupe bez mečeva (GROUP_ADMIN te grupe)   |
| GET    | `/api/v1/groups/{id}/members`          | Lista članova                                      |
| POST   | `/api/v1/groups/{id}/members`          | Dodavanje člana po username-u (GROUP_ADMIN)        |
| POST   | `/api/v1/groups/{id}/members/me`       | Pridruživanje grupi (igrač sam sebe dodaje)        |
| DELETE | `/api/v1/groups/{id}/members/me`       | Napuštanje grupe                                   |
| DELETE | `/api/v1/groups/{id}/members/{userId}` | Izbacivanje člana (GROUP_ADMIN)                    |

### Mečevi — `/api/v1/matches`

| Metoda | Putanja                | Opis                                                               |
|--------|------------------------|--------------------------------------------------------------------|
| GET    | `/api/v1/matches`      | Lista mečeva sa filterima (`?groupId=`, `?sportId=`, `?playerId=`) |
| GET    | `/api/v1/matches/{id}` | Detalji jednog meča                                                |
| POST   | `/api/v1/matches`      | Unos novog rezultata (član grupe; sistem određuje pobednika)       |
| PUT    | `/api/v1/matches/{id}` | Izmena rezultata (samo učesnik ili GROUP_ADMIN)                    |
| DELETE | `/api/v1/matches/{id}` | Brisanje (samo učesnik ili GROUP_ADMIN)                            |

### Rang-liste i statistike — `/api/v1/rankings`, `/api/v1/users/{id}/stats`

| Metoda | Putanja                                    | Opis                                                     |
|--------|--------------------------------------------|----------------------------------------------------------|
| GET    | `/api/v1/rankings?groupId=...&sportId=...` | Rang-lista (oba filtera su opciona; bez njih svi mečevi) |
| GET    | `/api/v1/users/{id}/stats?sportId=...`     | Statistika konkretnog igrača                             |
| GET    | `/api/v1/users/me/stats?sportId=...`       | Moja statistika (skraćenica)                             |

### Sistem administracija — `/api/v1/users`

| Metoda | Putanja                      | Opis                                              |
|--------|------------------------------|---------------------------------------------------|
| GET    | `/api/v1/users`              | Lista svih ostalih korisnika (SYSTEM_ADMIN)       |
| GET    | `/api/v1/users/{id}`         | Detalji korisnika (SYSTEM_ADMIN ili sam korisnik) |
| PUT    | `/api/v1/users/{id}`         | Izmena, uključujući promenu uloge (SYSTEM_ADMIN)  |
| DELETE | `/api/v1/users/{id}`         | Deaktivacija korisnika (SYSTEM_ADMIN)             |
| PATCH  | `/api/v1/users/{id}/restore` | Vraćanje deaktiviranog korisnika (SYSTEM_ADMIN)   |
