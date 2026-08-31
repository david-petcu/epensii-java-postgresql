# Arhitectura E-Pensii

Codul este separat în straturi, iar dependențele merg într-o singură direcție:

```text
ui -> service -> repository -> PostgreSQL
          |
        model
```

## `model`

- `Cerere`, `Salariu`, `ParametruPensie` și `StatisticiDashboard` reprezintă datele aplicației.
- `TipPensie` și `StatusCerere` limitează valorile la variante cunoscute.

## `repository`

- `CerereRepository` conține operațiile SQL pentru cereri.
- `SalariuRepository` salvează istoricul salarial într-o tranzacție.
- `ParametruPensieRepository` citește salariul mediu și valoarea punctului din baza de date.
- `Conectare` deschide conexiunile PostgreSQL.

## `service`

- `CerereService` controlează fluxul și coordonează repository-urile.
- `CalculatorPensieService` este singurul loc pentru eligibilitate și formulele de calcul.
- `Validari` conține validările comune.

## `ui`

Ferestrele Swing afișează date și apelează servicii. Nu conțin interogări SQL și nu decid singure tranzițiile de status. Fereastra principală include dashboardul, iar registrul oferă filtre după text, status, tip, valoarea pensiei și data plății.

## Fluxul cererii

```text
DEPUSA -> IN_VERIFICARE -> ADMISA/RESPINSA
ADMISA -> PENSIE_CALCULATA -> IN_PLATA
```

Corectarea datelor unui dosar admis sau respins îl readuce în `IN_VERIFICARE`. Corectarea salariilor unei pensii calculate invalidează calculul și readuce dosarul în `ADMISA`. O pensie aflată în plată este protejată împotriva modificării.
Respingerea cere un motiv nenul atât în serviciu, cât și prin constraint PostgreSQL.

## Baza de date

- `database/02_schema.sql` creează o bază nouă cu toate regulile.
- `database/03_flux_si_salarii.sql` migrează baza veche fără pierderea cererilor.
- `database/04_motiv_parametri_ani.sql` adaugă motivele de respingere, parametrii de calcul și convertește anii ordinali în ani calendaristici.
- `salarii` are cheie externă spre `cereripensie`, ștergere în cascadă și unicitate pe perechea cerere/an calendaristic.
- `parametri_pensie` păstrează salariul mediu și valoarea punctului pentru fiecare an.
- salvarea salariilor împreună cu invalidarea sau recalcularea pensiei rulează într-o singură tranzacție.
- triggerul `trg_tranzitie_status_cerere` blochează salturile nepermise între statusuri.
