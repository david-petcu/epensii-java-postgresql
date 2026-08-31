# Configurare PostgreSQL pentru E-Pensii

Aplicația nu mai depinde de MySQL sau XAMPP. Folosește PostgreSQL prin driverul JDBC inclus în `pom.xml`.

## 1. Creează baza de date

Pornește serviciul PostgreSQL și deschide pgAdmin. În Query Tool, conectat la baza `postgres`, execută:

```sql
CREATE DATABASE epensii
    WITH ENCODING = 'UTF8'
         TEMPLATE = template0;
```

Comanda se găsește și în `database/01_create_database.sql`.

## 2. Creează tabela

În pgAdmin, selectează noua bază `epensii`, deschide Query Tool și execută tot fișierul `database/02_schema.sql`.
Scriptul creează structura completă folosită de aplicație, importă cele două cereri din dump-ul MariaDB și setează următorul ID la 6.

Pentru o bază creată cu versiunile anterioare ale proiectului, execută apoi, în ordine, scripturile `03_flux_si_salarii.sql` și `04_motiv_parametri_ani.sql`. Al doilea script păstrează salariile existente și transformă numerotarea 1..stagiu în ani calendaristici.

## 3. Configurează parola

Configurația implicită a aplicației este:

- adresă: `jdbc:postgresql://localhost:5432/epensii`
- utilizator: `postgres`
- parolă: goală

De regulă, utilizatorul `postgres` are o parolă. În IntelliJ IDEA, deschide configurația de rulare pentru clasa `Main` și adaugă la **Environment variables**:

```text
EPENSII_DB_PASSWORD=parola_ta_postgresql
```

Pot fi suprascrise toate valorile:

```text
EPENSII_DB_URL=jdbc:postgresql://localhost:5432/epensii
EPENSII_DB_USER=postgres
EPENSII_DB_PASSWORD=parola_ta_postgresql
```

Nu introduce parola direct în cod și nu o salva în Git.

## 4. Importă proiectul Maven

În IntelliJ IDEA, deschide `pom.xml` și selectează **Load Maven Project**. Driverul PostgreSQL va fi descărcat automat. Apoi rulează clasa `Main`.

Din terminal, dacă Java și Maven sunt în `PATH`, aplicația poate fi pornită cu:

```powershell
mvn compile exec:java
```
