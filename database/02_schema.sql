CREATE TABLE IF NOT EXISTS cereripensie (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nume VARCHAR(100) NOT NULL,
    prenume VARCHAR(100) NOT NULL,
    adresa VARCHAR(255) NOT NULL,
    tippensie VARCHAR(30) NOT NULL,
    varsta INTEGER NOT NULL DEFAULT 0,
    stagiu INTEGER NOT NULL DEFAULT 0,
    sex VARCHAR(10) NOT NULL,
    gradinvaliditate INTEGER,
    nrurmasi INTEGER,
    cupon NUMERIC(12, 2),
    numarinregistrare VARCHAR(40) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'DEPUSA',
    motivrespingere TEXT,
    valoarepensie NUMERIC(12, 2),
    dataplata DATE,
    numardecizie VARCHAR(40),
    CONSTRAINT ck_varsta CHECK (varsta BETWEEN 0 AND 120),
    CONSTRAINT ck_stagiu CHECK (stagiu BETWEEN 0 AND 60),
    CONSTRAINT ck_tip_pensie CHECK (tippensie IN ('LIMITA_VARSTA', 'INVALIDITATE', 'URMAS')),
    CONSTRAINT ck_sex CHECK (sex IN ('M', 'F', 'N/A')),
    CONSTRAINT ck_status_cerere CHECK (status IN
        ('DEPUSA', 'IN_VERIFICARE', 'ADMISA', 'RESPINSA', 'PENSIE_CALCULATA', 'IN_PLATA')),
    CONSTRAINT ck_grad_invaliditate CHECK (gradinvaliditate IS NULL OR gradinvaliditate BETWEEN 1 AND 3),
    CONSTRAINT ck_nr_urmasi CHECK (nrurmasi IS NULL OR nrurmasi BETWEEN 1 AND 5),
    CONSTRAINT ck_valori_pozitive CHECK
        ((cupon IS NULL OR cupon >= 0) AND (valoarepensie IS NULL OR valoarepensie >= 0)),
    CONSTRAINT ck_date_invaliditate CHECK (tippensie <> 'INVALIDITATE' OR gradinvaliditate IS NOT NULL),
    CONSTRAINT ck_date_urmas CHECK
        (tippensie <> 'URMAS' OR (nrurmasi IS NOT NULL AND cupon IS NOT NULL)),
    CONSTRAINT ck_flux_valoare CHECK
        (status NOT IN ('PENSIE_CALCULATA', 'IN_PLATA') OR valoarepensie IS NOT NULL),
    CONSTRAINT ck_flux_plata CHECK
        ((status = 'IN_PLATA' AND dataplata IS NOT NULL) OR
         (status <> 'IN_PLATA' AND dataplata IS NULL)),
    CONSTRAINT ck_motiv_respingere CHECK
        (status <> 'RESPINSA' OR NULLIF(BTRIM(motivrespingere), '') IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_cereripensie_status ON cereripensie(status);

CREATE TABLE IF NOT EXISTS salarii (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cerere INTEGER NOT NULL REFERENCES cereripensie(id) ON DELETE CASCADE,
    an_calendaristic INTEGER NOT NULL CHECK (an_calendaristic BETWEEN 1900 AND 2200),
    salariu_brut_mediu NUMERIC(12, 2) NOT NULL CHECK (salariu_brut_mediu > 0),
    CONSTRAINT uq_salariu_cerere_an UNIQUE (id_cerere, an_calendaristic)
);

CREATE INDEX IF NOT EXISTS idx_salarii_id_cerere ON salarii(id_cerere);

CREATE TABLE IF NOT EXISTS parametri_pensie (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    an INTEGER NOT NULL UNIQUE CHECK (an BETWEEN 1900 AND 2200),
    salariu_mediu NUMERIC(12, 2) NOT NULL CHECK (salariu_mediu > 0),
    valoare_punct NUMERIC(12, 2) NOT NULL CHECK (valoare_punct > 0),
    data_inceput DATE NOT NULL,
    data_sfarsit DATE NOT NULL,
    CONSTRAINT ck_interval_parametru CHECK (data_inceput <= data_sfarsit)
);

INSERT INTO parametri_pensie (an, salariu_mediu, valoare_punct, data_inceput, data_sfarsit)
SELECT an, 6800.00, 2032.00, make_date(an, 1, 1), make_date(an, 12, 31)
FROM generate_series(1950, EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER) AS an
ON CONFLICT (an) DO NOTHING;

INSERT INTO cereripensie
    (id, nume, prenume, adresa, tippensie, varsta, stagiu, sex, numarinregistrare, status)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'Ionescu', 'Ana', 'Adresă demonstrativă 1', 'LIMITA_VARSTA', 62, 32, 'F', 'DEMO-LV-001', 'ADMISA'),
    (5, 'Marin', 'Andrei', 'Adresă demonstrativă 2', 'LIMITA_VARSTA', 65, 35, 'M', 'DEMO-LV-002', 'ADMISA')
ON CONFLICT DO NOTHING;

SELECT setval(
    pg_get_serial_sequence('cereripensie', 'id'),
    GREATEST(COALESCE(MAX(id), 1), 5),
    true
)
FROM cereripensie;

CREATE OR REPLACE FUNCTION verifica_tranzitie_status_cerere()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status = NEW.status OR
       (OLD.status = 'DEPUSA' AND NEW.status = 'IN_VERIFICARE') OR
       (OLD.status = 'IN_VERIFICARE' AND NEW.status IN ('ADMISA', 'RESPINSA')) OR
       (OLD.status IN ('ADMISA', 'RESPINSA') AND NEW.status = 'IN_VERIFICARE') OR
       (OLD.status = 'ADMISA' AND NEW.status = 'PENSIE_CALCULATA') OR
       (OLD.status = 'PENSIE_CALCULATA' AND NEW.status IN ('ADMISA', 'IN_PLATA')) THEN
        RETURN NEW;
    END IF;
    RAISE EXCEPTION 'Tranziție de status nepermisă: % -> %', OLD.status, NEW.status;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_tranzitie_status_cerere ON cereripensie;
CREATE TRIGGER trg_tranzitie_status_cerere
BEFORE UPDATE OF status ON cereripensie
FOR EACH ROW EXECUTE FUNCTION verifica_tranzitie_status_cerere();
