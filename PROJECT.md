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

- **Igrač** — registruje se, pridružuje se grupama, unosi rezultate svojih mečeva
  i prati svoje statistike i poziciju na rang-listi.
- **Administrator grupe** — kreira grupu i upravlja njome: dodaje i uklanja članove,
  organizuje takmičenja unutar grupe i uređuje njena podešavanja. Svaki igrač koji
  kreira grupu postaje njen administrator.
- **Sistem administrator** — upravlja katalogom sportova, svim korisnicima i
  osnovnim podešavanjima celog sistema.

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
- Rang-lista igrača unutar grupe i po sportu, na osnovu broja pobeda i osvojenih
  bodova
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

### Uloge i permisije — trenutno stanje

- U **MariaDB** postoji potpun model uloga i permisija sa relacijama
  (korisnik → uloge → permisije). To je trenutno **izvor istine** za uloge.
- U **MongoDB** se uloge za sada čuvaju denormalizovano — kao spisak unutar
  korisničkog zapisa, bez zasebnih entiteta.
- **Planirano:** dovesti rad sa ulogama u MongoDB na isti nivo kao u MariaDB, da
  obe baze budu potpuno ravnopravne.

---

## Planirani REST endpointi

> Bazna putanja: `/api/v1`. Status: **✅ implementirano**, **🔜 planirano**.

### Autentikacija i sopstveni nalog — `/api/v1/auth`, `/api/v1/users/me`

| Metoda | Putanja                     | Opis                                          | Status |
|--------|-----------------------------|-----------------------------------------------|--------|
| POST   | `/api/v1/auth/register`     | Registracija novog igrača                     | 🔜 |
| POST   | `/api/v1/auth/login`        | Prijava (JWT) — već postoji                    | ✅ |
| GET    | `/api/v1/users/me`          | Moj profil                                    | 🔜 |
| PUT    | `/api/v1/users/me`          | Izmena profila (ime, prezime, email)          | 🔜 |
| PUT    | `/api/v1/users/me/password` | Promena lozinke                               | 🔜 |

### Sportovi — `/api/v1/sports` (sistem administrator upravlja, ostali čitaju)

| Metoda | Putanja               | Opis                          | Status |
|--------|-----------------------|-------------------------------|--------|
| GET    | `/api/v1/sports`      | Lista svih sportova           | 🔜 |
| GET    | `/api/v1/sports/{id}` | Detalji sporta                | 🔜 |
| POST   | `/api/v1/sports`      | Dodavanje (SYSTEM_ADMIN)      | 🔜 |
| PUT    | `/api/v1/sports/{id}` | Izmena (SYSTEM_ADMIN)         | 🔜 |
| DELETE | `/api/v1/sports/{id}` | Brisanje (SYSTEM_ADMIN)       | 🔜 |

### Grupe — `/api/v1/groups`

| Metoda | Putanja                               | Opis                                                  | Status |
|--------|---------------------------------------|-------------------------------------------------------|--------|
| GET    | `/api/v1/groups`                      | Lista grupa (filteri: `?mine=true`, `?search=...`)    | 🔜 |
| GET    | `/api/v1/groups/{id}`                 | Detalji grupe                                         | 🔜 |
| POST   | `/api/v1/groups`                      | Kreiranje grupe (kreator postaje GROUP_ADMIN)         | 🔜 |
| PUT    | `/api/v1/groups/{id}`                 | Izmena podešavanja (GROUP_ADMIN te grupe)             | 🔜 |
| DELETE | `/api/v1/groups/{id}`                 | Brisanje grupe (GROUP_ADMIN te grupe)                 | 🔜 |
| POST   | `/api/v1/groups/{id}/members`         | Pridruživanje grupi (igrač sam sebe dodaje)           | 🔜 |
| DELETE | `/api/v1/groups/{id}/members/me`      | Napuštanje grupe                                      | 🔜 |
| GET    | `/api/v1/groups/{id}/members`         | Lista članova                                         | 🔜 |
| DELETE | `/api/v1/groups/{id}/members/{userId}`| Izbacivanje člana (GROUP_ADMIN)                       | 🔜 |

### Mečevi — `/api/v1/matches`

| Metoda | Putanja                | Opis                                                                | Status |
|--------|------------------------|---------------------------------------------------------------------|--------|
| GET    | `/api/v1/matches`      | Lista mečeva sa filterima (`?groupId=`, `?sportId=`, `?playerId=`)  | 🔜 |
| GET    | `/api/v1/matches/{id}` | Detalji jednog meča                                                 | 🔜 |
| POST   | `/api/v1/matches`      | Unos novog rezultata (sistem određuje pobednika)                    | 🔜 |
| PUT    | `/api/v1/matches/{id}` | Izmena rezultata (samo učesnik ili GROUP_ADMIN)                     | 🔜 |
| DELETE | `/api/v1/matches/{id}` | Brisanje (samo učesnik ili GROUP_ADMIN)                             | 🔜 |

### Rang-liste i statistike — `/api/v1/rankings`, `/api/v1/users/{id}/stats`

| Metoda | Putanja                                       | Opis                          | Status |
|--------|-----------------------------------------------|-------------------------------|--------|
| GET    | `/api/v1/rankings?groupId=...&sportId=...`    | Rang-lista u grupi za sport   | 🔜 |
| GET    | `/api/v1/users/{id}/stats?sportId=...`        | Statistika konkretnog igrača  | 🔜 |
| GET    | `/api/v1/users/me/stats`                      | Moja statistika (skraćenica)  | 🔜 |

### Sistem administracija — `/api/v1/admin` (samo SYSTEM_ADMIN)

| Metoda | Putanja                     | Opis                                  | Status |
|--------|-----------------------------|---------------------------------------|--------|
| GET    | `/api/v1/admin/users`       | Lista svih korisnika                  | 🔜 |
| GET    | `/api/v1/admin/users/{id}`  | Detalji korisnika                     | 🔜 |
| PUT    | `/api/v1/admin/users/{id}`  | Izmena (uključujući promenu uloge)    | 🔜 |
| DELETE | `/api/v1/admin/users/{id}`  | Deaktivacija korisnika                | 🔜 |
