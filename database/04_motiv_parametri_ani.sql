BEGIN;

ALTER TABLE cereripensie ADD COLUMN IF NOT EXISTS motivrespingere TEXT;
ALTER TABLE cereripensie DROP CONSTRAINT IF EXISTS ck_motiv_respingere;
ALTER TABLE cereripensie ADD CONSTRAINT ck_motiv_respingere CHECK
    (status <> 'RESPINSA' OR NULLIF(BTRIM(motivrespingere), '') IS NOT NULL);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'salarii' AND column_name = 'an_cotizare'
    ) THEN
        ALTER TABLE salarii RENAME COLUMN an_cotizare TO an_calendaristic;
    END IF;
END $$;

ALTER TABLE salarii DROP CONSTRAINT IF EXISTS salarii_an_cotizare_check;
ALTER TABLE salarii DROP CONSTRAINT IF EXISTS salarii_an_calendaristic_check;

-- Versiunile anterioare numerotau anii de cotizare 1..stagiu. Îi convertim
-- în ani calendaristici consecutivi, ultimul fiind anul anterior celui curent.
UPDATE salarii s
SET an_calendaristic = EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER - c.stagiu + s.an_calendaristic - 1
FROM cereripensie c
WHERE c.id = s.id_cerere AND s.an_calendaristic < 1900;

ALTER TABLE salarii ADD CONSTRAINT salarii_an_calendaristic_check
    CHECK (an_calendaristic BETWEEN 1900 AND 2200);

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

COMMIT;
