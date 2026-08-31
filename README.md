# E-Pensii

**Română** | [English](README.en.md)

Aplicație desktop Java pentru gestionarea cererilor de pensionare, realizată cu Swing și PostgreSQL. Proiectul urmărește întregul flux al unui dosar, de la depunere și verificare până la calculul pensiei și punerea în plată.

> [!IMPORTANT]
> Proiectul are scop educațional. Regulile, pragurile și valorile folosite în calcule sunt demonstrative și nu reprezintă legislația oficială privind pensiile din România.

## Funcționalități

- înregistrarea cererilor pentru limită de vârstă, invaliditate și urmaș;
- flux controlat de statusuri: `Depusă → În verificare → Admisă/Respinsă → Pensie calculată → În plată`;
- motiv obligatoriu pentru dosarele respinse;
- registru cu editare, copiere de text, sortare și filtre avansate;
- gestionarea salariilor pe ani calendaristici;
- calcul centralizat pe baza parametrilor păstrați în PostgreSQL;
- salvarea atomică a salariilor și a rezultatului calculului;
- dashboard cu situația cererilor și totalul pensiilor aflate în plată;
- o singură instanță pentru fiecare fereastră a aplicației;
- constraints și trigger PostgreSQL pentru protejarea integrității datelor.

## Tehnologii

- Java 17+
- Java Swing
- PostgreSQL
- JDBC
- Maven

## Arhitectură

Codul este separat pe straturi:

```text
ui → service → repository → PostgreSQL
       ↓
     model
```

- `model` — entitățile și tipurile domeniului;
- `repository` — accesul la PostgreSQL;
- `service` — validările, fluxul și regulile de calcul;
- `ui` — ferestrele și componentele Swing;
- `app` — punctul de pornire al aplicației.

Mai multe detalii sunt disponibile în [ARHITECTURA.md](ARHITECTURA.md).

## Configurare

### 1. Cerințe

- JDK 17 sau mai nou;
- Maven 3.9+;
- PostgreSQL.

### 2. Baza de date

Execută scripturile în această ordine:

1. `database/01_create_database.sql`, conectat la baza `postgres`;
2. `database/02_schema.sql`, conectat la baza nouă `epensii`.
3. opțional, `database/05_demo_portofoliu.sql`, pentru date fictive de prezentare.

Scriptul principal creează schema, regulile de integritate și parametrii de calcul. Scripturile `03` și `04` sunt păstrate pentru migrarea instalărilor mai vechi, iar scriptul `05` poate fi rulat în siguranță de mai multe ori.

### 3. Conexiunea

Aplicația citește configurația din variabile de mediu:

```powershell
$env:EPENSII_DB_URL = "jdbc:postgresql://localhost:5432/epensii"
$env:EPENSII_DB_USER = "postgres"
$env:EPENSII_DB_PASSWORD = "parola_postgresql"
```

Parola nu trebuie introdusă în cod sau adăugată în repository.

### 4. Rulare

```powershell
mvn clean compile exec:java
```

Clasa principală este `app.Main`.

## Verificare

Compilarea proiectului și a testului de integrare:

```powershell
mvn clean test-compile
```

Testul `integration.FluxIntegrareCheck` verifică fluxul complet, persistența salariilor, recalcularea, motivul respingerii și constraints-urile PostgreSQL. Testul folosește o bază locală configurată prin aceleași variabile de mediu și își șterge datele temporare la final.

## Structura bazei de date

- `cereripensie` — datele dosarului, statusul, decizia și rezultatul;
- `salarii` — istoricul salarial anual al unei cereri;
- `parametri_pensie` — salariul mediu și valoarea punctului pentru fiecare an.

## Scop

Proiect educațional și de portofoliu care demonstrează programare orientată pe obiecte, interfețe desktop, acces JDBC, proiectarea unei baze relaționale și separarea responsabilităților pe straturi.

## Licență

Proiectul este distribuit sub licența [MIT](LICENSE).
