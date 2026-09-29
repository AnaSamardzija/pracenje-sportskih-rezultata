# Baza podataka

Ovaj dokument opisuje sve što je u projektu urađeno sa bazama: kako aplikacija bira bazu, šta postoji u
MariaDB bazi (tabele, procedure, funkcije, trigeri, event) i šta u MongoDB bazi (kolekcije, indeksi, skripte).
Tačne kolone i ceo SQL nisu prepisani ovde, nego su u fajlovima na koje se tekst poziva.

---

## 1. Uvod

**Dve nezavisne baze**, obe se podižu kroz `docker-compose.yml`:

| Baza | Verzija | Kako joj aplikacija pristupa | Šema |
|---|---|---|---|
| MariaDB (relaciona) | 11.4 | Spring Data JPA + Hibernate | Flyway migracije |
| MongoDB (dokument baza) | 8.0 | Spring Data MongoDB | nema fiksne šeme; indeksi se prave iz anotacija (`auto-index-creation=true`) |

Obe baze čuvaju iste podatke (korisnike, uloge i permisije, sportove, grupe, članstva, mečeve), samo drugačije
modelovane. Međusobno su nezavisne: ono što se unese u jednu ne postoji u drugoj.

**Izbor baze (storage type).** Korisnik pri prijavi bira bazu, i ceo rad posle toga ide nad njom:

1. Login šalje `username`, `password` i `storageType` (`MARIADB` ili `MONGODB`).
2. `StorageSelectionAuthenticationProvider` traži korisnika u izabranoj bazi i proverava lozinku.
3. Izbor se upisuje u JWT kao claim `storageType` (`JwtUtil`).
4. Na svakom zahtevu `JwtAuthenticationFilter` čita claim iz tokena, a `CurrentStorageTypeProvider` ga daje servisima.
5. `StorageResolver` po paru (tip baze, tip podatka) vraća odgovarajuću implementaciju, npr. `MariaDbMatchStorage`
   ili `MongoDbMatchStorage`.

Kontroleri i servisi su zajednički; baze se razdvajaju tek u paketima `storage/` i `data/`.

**Gde je šta u projektu** (Java paketi su u `src/main/java/rs/ac/ni/pmf/ana/dualdb/`):

| Šta | Gde |
|---|---|
| Podizanje baza | `docker-compose.yml` |
| Konekcije i podešavanja | `src/main/resources/application.properties` |
| MariaDB migracije (šema, procedure, funkcije, trigeri, event, podaci) | `src/main/resources/db/migration/` |
| JPA entiteti i repozitorijumi | `data/mariadb/entity/`, `data/mariadb/repository/` |
| Mongo dokumenti i repozitorijumi | `data/mongodb/document/`, `data/mongodb/repository/` |
| Pristup bazi, po jedna klasa za svaku bazu i vrstu podatka | `storage/` (npr. `storage/match/MariaDbMatchStorage`) |
| Početni podaci i izmene postojećih Mongo dokumenata | `config/DataInitializer.java` |
| Mongo skripte | `mongo/` |

---

## 2. MariaDB

### 2.1 Flyway migracije

Sve što postoji u MariaDB bazi nastaje kroz migracije u `src/main/resources/db/migration`. Flyway ih pušta
redom pri pokretanju aplikacije i beleži u tabeli `flyway_schema_history`, pa se svaka izvrši tačno jednom.
Već primenjena migracija se ne menja; svaka izmena šeme ili postojećih podataka je nova migracija.

| Migracija | Vrsta | Šta radi |
|---|---|---|
| `V1__init_user_role_permission.sql` | šema | `users`, `roles`, `permissions` i vezne tabele `user_roles`, `roles_permissions` |
| `V2__create_sports.sql` | šema | `sports` (pravila sporta su kolone u istoj tabeli) |
| `V3__create_groups.sql` | šema | `groups` |
| `V4__create_memberships.sql` | šema | `memberships` |
| `V5__create_matches.sql` | šema | `matches`, `match_sides`, `match_side_players`, `match_side_set_scores` |
| `V6__align_global_roles.sql` | podaci | usklađuje uloge sa domenom: `ADMIN` preimenuje u `SYSTEM_ADMIN`, briše ulogu `MANAGER` sa njenim vezama i dve nekorišćene permisije |
| `V7__create_player_stats.sql` | šema | `player_stats` (izvedena statistika, vidi 2.3) |
| `V8__create_player_stats_triggers.sql` | procedura + trigeri | `sp_update_player_stats` i tri trigera koji održavaju `player_stats` |
| `V9__create_rebuild_player_stats.sql` | procedura + event | `sp_rebuild_player_stats`, `ev_rebuild_player_stats` i jedan `CALL` koji odmah napuni tabelu |
| `V10__create_stats_functions.sql` | funkcije | `fn_points_for_outcome`, `fn_total_matches`, `fn_win_percentage` |
| `V11__create_group_ranking.sql` | procedura | `sp_group_ranking` (rang-lista) |
| `V12__create_longest_win_streak.sql` | procedura | `sp_longest_win_streak` (najduži niz pobeda) |
| `V13__add_users_active.sql` | šema | kolona `users.active` (`DEFAULT TRUE`) za deaktivaciju naloga |
| `V14__insert_sports.sql` | podaci | početni katalog od šest sportova sa pravilima: Tennis, Table Tennis, Chess, Football, Basketball, Volleyball |
| `V15__define_permissions.sql` | podaci | 18 permisija (po jedna za svaku administrativnu akciju), uloge `SYSTEM_ADMIN` i `USER` i njihove permisije: `SYSTEM_ADMIN` sve, `USER` samo promenu svoje lozinke |

Jedino što ne ide kroz migraciju je nalog `admin`. Njega pri startu upisuje `DataInitializer`, ako ne postoji.

### 2.2 Tabele

Kolone, ključevi i ograničenja su u migracijama V1–V5, V7 i V13. Ovde je samo šta koja tabela čuva.

| Tabela | Šta čuva |
|---|---|
| `users` | Korisničke naloge: username, lozinku, ime, email, datum registracije i da li je nalog aktivan. |
| `roles` | Globalne uloge: `USER` i `SYSTEM_ADMIN`. |
| `permissions` | 18 permisija sa opisom, po jedna za svaku administrativnu akciju (npr. `sports.create`, `matches.delete_any`). |
| `user_roles` | Koje uloge ima koji korisnik (veza više-više). |
| `roles_permissions` | Koje permisije daje koja uloga. Prava se menjaju samo ovde, bez izmene koda. |
| `sports` | Katalog sportova sa pravilima: pojedinačni ili timski, način unosa rezultata (setovi, poeni ili samo ishod), da li je dozvoljeno nerešeno, broj igrača po strani, broj setova, i koliko bodova za rang-listu donose pobeda, nerešeno i poraz. |
| `groups` | Grupe igrača: naziv, opis, ko je napravio grupu i kada. |
| `memberships` | Ko je član koje grupe i sa kojom ulogom u njoj (`GROUP_ADMIN` ili `MEMBER`). |
| `matches` | Odigrane mečeve: sport, grupa, vreme odigravanja i ko je meč uneo. |
| `match_sides` | Strane meča (igrač ili tim): rezultat strane, ishod (`WIN`, `DRAW`, `LOSS`) i da li je strana pobedila. |
| `match_side_players` | Koji igrači su igrali za koju stranu. |
| `match_side_set_scores` | Rezultat strane po setovima, za sportove koji se igraju na setove. |
| `player_stats` | Izvedenu statistiku: broj pobeda, nerešenih i poraza po igraču, grupi i sportu (vidi 2.3). |

### 2.3 Statistika igrača: kako delovi rade zajedno

Procedure, funkcije, trigeri i event iz migracija V7–V12 uglavnom služe jednoj stvari: tabeli `player_stats`,
u kojoj baza sama vodi koliko je pobeda, nerešenih i poraza svaki igrač imao u svakoj grupi i sportu. To je
izveden podatak (može se izračunati iz mečeva), ali ga baza drži spremnog, da izveštaji ne bi svaki put
prolazili kroz sve mečeve.

1. **Održavanje.** Kad se meč upiše, izmeni ili obriše, trigeri za svakog igrača tog meča dodaju ili oduzmu
   jedan ishod, preko pomoćne procedure `sp_update_player_stats`. Aplikacija u tome ne učestvuje i ne zna za
   ovu tabelu.
2. **Kontrola.** Event `ev_rebuild_player_stats` jednom dnevno poziva `sp_rebuild_player_stats`, koja tabelu
   obriše i izračuna ispočetka iz svih mečeva. Time se ispravlja ono što trigeri ne vide, npr. ishod promenjen
   ručno direktno u bazi.
3. **Čitanje.** Funkcije `fn_*` i procedura `sp_group_ranking` iz tabele prave statistiku igrača i rang-listu.

Red kome sve tri vrednosti padnu na nulu se briše, pa u tabeli nema praznih redova.

### 2.4 Upiti iz aplikacije (JPA)

- **Izvedeni upiti** (Spring pravi SQL iz imena metode): `findByUsername`, `existsByEmail`, `findByActiveTrue`,
  `findByNameIgnoreCase`, `findByUser_IdAndGroup_Id`, `existsByGroup_Id`, `findAllByNameIn` itd.
- **JPQL upit** `MariaDbMatchRepository.search` filtrira mečeve po grupi, sportu i igraču. Svaki filter je
  opcion (`:groupId is null or ...`). Igrač se traži **podupitom** `exists (...)` kroz strane i igrače meča,
  a rezultat je poređan od najnovijeg (`order by m.playedAt desc, m.id desc`).
- **Poziv procedure** `@Procedure` za `sp_longest_win_streak` (V12).
- **Transakcije**: metode MariaDB storage-a imaju `@Transactional` (`readOnly = true` za čitanje), a servisi
  koji menjaju više tabela odjednom (npr. grupa + članstvo admina, meč + strane) takođe.
- **Veze u entitetima**: `@ManyToMany` + `@JoinTable` (korisnik ↔ uloge, uloga ↔ permisije, strana ↔ igrači),
  `@OneToMany` sa `cascade` i `orphanRemoval` (meč → strane), `@ElementCollection` + `@OrderColumn` (setovi
  strane), `@Embedded` (pravila sporta kao kolone tabele `sports`).

---

## 3. MongoDB

### 3.1 Početni podaci i izmene postojećih dokumenata (DataInitializer)

Mongo nema Flyway. Ono što u MariaDB rade migracije sa podacima, za Mongo radi `DataInitializer` pri svakom
startu aplikacije. Koraci idu ovim redom:

| Korak | Šta radi | Pandan u MariaDB |
|---|---|---|
| `createMongoPermissions` | upiše 18 permisija iz enum-a `Permission`, ako je kolekcija prazna | V15 |
| `createMongoRoles` | upiše `SYSTEM_ADMIN` (sve permisije) i `USER` (samo promena svoje lozinke), ako je kolekcija prazna | V15 |
| `migrateMongoUserRoles` | korisnicima kod kojih su uloge još niz imena (stari oblik) zameni imena referencama na `roles` i ukloni stari niz permisija | nema; MariaDB je od početka imala vezne tabele |
| `migrateMongoUserActive` | korisnicima bez polja `active` postavi `active = true` | V13 (`DEFAULT TRUE`) |
| admin | upiše nalog `admin`, ako ne postoji | isto, `DataInitializer` |
| `createMongoSports` | upiše katalog od šest sportova, ako je kolekcija prazna | V14 |

Svaki korak radi ili samo nad praznom kolekcijom, ili samo nad dokumentima u starom obliku, pa ponovni start
ništa ne menja. Tako se oponaša Flyway, koji svaku migraciju izvrši tačno jednom.

### 3.2 Kolekcije

Polja su u klasama u `data/mongodb/document/`. Kolekcije se ne prave unapred: Mongo napravi kolekciju pri
prvom upisu, a Spring pri startu napravi indekse (3.3).

| Kolekcija | Šta čuva | Pandan u MariaDB |
|---|---|---|
| `users` | Korisničke naloge; uloge su reference na dokumente iz `roles`. | `users` + `user_roles` |
| `roles` | Globalne uloge; permisije su reference na dokumente iz `permissions`. | `roles` + `roles_permissions` |
| `permissions` | 18 permisija sa opisom. | `permissions` |
| `sports` | Katalog sportova; pravila su ugnežđen dokument `rules`. | `sports` |
| `groups` | Grupe igrača. | `groups` |
| `memberships` | Članstvo korisnika u grupi i uloga u njoj. | `memberships` |
| `matches` | Ceo meč u jednom dokumentu: niz `sides`, a svaka strana ima `playerIds`, `score`, `setScores`, `outcome` i `winner`. | `matches` + `match_sides` + `match_side_players` + `match_side_set_scores` |

Razlike u modelovanju:

- **Ugnežđeni dokumenti umesto veznih tabela.** Ono što je u MariaDB četiri tabele meča, ovde je jedan dokument,
  pa se meč čita i upisuje odjednom, bez spajanja. Isto tako su pravila sporta ugnežđena u sport.
- **Reference.** Korisnik čuva reference na uloge, a uloga na permisije. Ostale veze (`sportId`, `groupId`,
  `playerIds`, `userId`) su obični id-jevi kao stringovi.
- **Nema kolekcije za statistiku.** Pandan tabele `player_stats` ne postoji (vidi 3.4).

### 3.3 Indeksi

Mongo nema `UNIQUE` ograničenje kao MariaDB, pa tu ulogu ima **unique indeks**. Ostali indeksi ubrzavaju upite
koje aplikacija često radi (članovi grupe, filteri mečeva). U MariaDB ovo nije trebalo posebno praviti, jer
InnoDB sam pravi indekse za ključeve. Indeksi se prave iz anotacija na dokumentima (`@Indexed`,
`@CompoundIndex`).

| Kolekcija | Indeks | Svrha |
|---|---|---|
| `users` | `uk_users_username`, `uk_users_email` (unique) | jedinstven username i email |
| `roles`, `permissions`, `sports` | `uk_*_name` (unique) | jedinstveno ime |
| `memberships` | `uk_memberships_user_group` (unique, složen: `userId` + `groupId`) | pandan `UNIQUE (user_id, group_id)`; korisnik je u grupi najviše jednom |
| `memberships` | `ix_memberships_group` | brzo nalaženje članova grupe |
| `matches` | `ix_matches_sport`, `ix_matches_group` | filteri mečeva po sportu i grupi |
| `matches` | `ix_matches_players` (`sides.playerIds`) | mečevi igrača; indeks nad poljem u nizu ugnežđenih dokumenata (multikey) |

Unique indeksi nad imenima imaju **collation** `{locale: 'en', strength: 1}`, pa ne razlikuju velika i mala
slova ni kvačice („Tenis“ i „tenis“, kao i „Šah“ i „Sah“, su isto ime). Isti collation stoji i na upitima u
repozitorijumima (`@Collation`), da bi pretraga koristila indeks. U MariaDB to isto radi podrazumevana
collation kolona, `utf8mb4_uca1400_ai_ci` (`ci` = bez razlike u velikim i malim slovima, `ai` = bez razlike u
kvačicama). Ako upis prekrši unique indeks, Mongo baci `DuplicateKeyException`, a aplikacija vraća 409, isto
kao za MariaDB.

### 3.4 Statistika igrača: Mongo skripte

Mongo nema trigere, uskladištene procedure, funkcije u bazi ni evente.
Zato se statistika u Mongu **ne čuva**, nego se računa iz mečeva kad zatreba. Nema ko da je održava pri
svakoj izmeni meča, a ručno održavanje iz aplikacije bi lako napravilo razliku između mečeva i statistike.

Skripte u folderu `mongo/` pokazuju kako se isti izveštaji kao u 2.3 (statistika, rang-lista, najduži niz
pobeda) dobijaju u Mongu, agregacijom i kursorom. Služe za proveru: nad istim podacima daju iste brojeve kao
SQL objekti i aplikacija. Aplikacija ih ne pokreće, nego se pokreću ručno iz MongoDB for VS Code ili
`mongosh`, nad bazom `dual-db`. Parametri se zadaju na vrhu upita, a `null` znači „sve“.

| Skripta | Parametri | Tehnika | Pandan u MariaDB |
|---|---|---|---|
| `01_player_stats.mongodb.js` | nema | agregacija: `$unwind` strana i igrača, `$match` (bez strana bez ishoda), `$group` po (igrač, grupa, sport) sa `$sum` + `$cond`, `$project`, `$sort` | tabela `player_stats` |
| `02_stats_and_ranking.mongodb.js`, statistika | `playerId`, `sportId` | agregacija sa `$lookup` na `sports` (bodovi po pravilima) i `users` (username), procenat sa zaokruživanjem | `fn_total_matches`, `fn_win_percentage`, `fn_points_for_outcome` |
| `02_stats_and_ranking.mongodb.js`, rang-lista | `groupId`, `sportId` | isto, plus `$setWindowFields` sa `$rank` | `sp_group_ranking` (`RANK()`) |
| `03_longest_win_streak.mongodb.js` | `playerId`, `sportId` | kursor: `find().sort({playedAt: 1, _id: 1})` i `while (cursor.hasNext())` | `sp_longest_win_streak` |

### 3.5 Upiti iz aplikacije (Spring Data MongoDB)

- **Izvedeni upiti** u Mongo repozitorijumima, isti kao u JPA (`findByUsername`, `findByUserIdAndGroupId`,
  `existsByGroupId` itd.), neki uz `@Collation`.
- **Filter mečeva** `MongoDbMatchStorage.findAll`: `Query` sa `Criteria` gradi se samo od zadatih filtera.
  Igrač se traži direktno u ugnežđenom nizu (`Criteria.where("sides.playerIds").is(playerId)`), bez spajanja,
  a rezultat je poređan od najnovijeg (`playedAt`, pa `id`, opadajuće).
- **Kursor iz Jave** `MongoDbMatchStorage.longestWinStreak`, pandan procedure `sp_longest_win_streak`:
  `MongoTemplate.stream` vraća mečeve igrača poređane hronološki, i kroz njih se prolazi iteratorom
  (`while (cursor.hasNext())`). Zato statistika daje isti niz pobeda nad obe baze.
- **Transakcije**: Mongo u projektu radi kao samostalan server (bez replica set-a), pa nema transakcija nad
  više dokumenata; `@Transactional` važi samo za MariaDB. Za mečeve to nije problem, jer je ceo meč jedan
  dokument, a upis jednog dokumenta u Mongu je uvek atomičan.
- **Veze u dokumentima**: `@DocumentReference` (korisnik → uloge, uloga → permisije), ugnežđeni dokumenti
  (strane meča, pravila sporta), a ostale veze su id-jevi kao stringovi.
