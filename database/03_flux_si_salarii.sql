BEGIN;

UPDATE cereripensie
SET tippensie = CASE
    WHEN UPPER(tippensie) LIKE '%URMAS%' OR UPPER(tippensie) LIKE '%URMAȘ%' THEN 'URMAS'
    WHEN UPPER(tippensie) LIKE '%INVALID%' THEN 'INVALIDITATE'
    ELSE 'LIMITA_VARSTA'
END;

UPDATE cereripensie
SET status = CASE
    WHEN LOWER(status) LIKE 'respins%' THEN 'RESPINSA'
    WHEN valoarepensie IS NOT NULL AND dataplata IS NOT NULL THEN 'IN_PLATA'
    WHEN valoarepensie IS NOT NULL THEN 'PENSIE_CALCULATA'
    WHEN LOWER(status) LIKE 'admis%' THEN 'ADMISA'
    WHEN LOWER(status) LIKE '%verific%' THEN 'IN_VERIFICARE'
    ELSE 'DEPUSA'
END;

UPDATE cereripensie SET dataplata = NULL WHERE status <> 'IN_PLATA';

ALTER TABLE cereripensie ALTER COLUMN status TYPE VARCHAR(30);
ALTER TABLE cereripensie ALTER COLUMN status SET DEFAULT 'DEPUSA';

ALTER TABLE cereripensie DROP CONSTRAINT IF EXISTS ck_tip_pensie;
ALTER TABLE cereripensie DROP CONSTRAINT IF EXISTS ck_sex;
ALTER TABLE cereripensie DROP CONSTRAINT IF EXISTS ck_status_cerere;
ALTER TABLE cereripensie DROP CONSTRAINT IF EXISTS ck_valori_pozitive;
ALTER TABLE cereripensie DROP CONSTRAINT IF EXISTS ck_date_invaliditate;
ALTER TABLE cereripensie DROP CONSTRAINT IF EXISTS ck_date_urmas;
ALTER TABLE cereripensie DROP CONSTRAINT IF EXISTS ck_flux_valoare;
ALTER TABLE cereripensie DROP CONSTRAINT IF EXISTS ck_flux_plata;

ALTER TABLE cereripensie ADD CONSTRAINT ck_tip_pensie
    CHECK (tippensie IN ('LIMITA_VARSTA', 'INVALIDITATE', 'URMAS'));
ALTER TABLE cereripensie ADD CONSTRAINT ck_sex CHECK (sex IN ('M', 'F', 'N/A'));
ALTER TABLE cereripensie ADD CONSTRAINT ck_status_cerere CHECK (status IN
    ('DEPUSA', 'IN_VERIFICARE', 'ADMISA', 'RESPINSA', 'PENSIE_CALCULATA', 'IN_PLATA'));
ALTER TABLE cereripensie ADD CONSTRAINT ck_valori_pozitive CHECK
    ((cupon IS NULL OR cupon >= 0) AND (valoarepensie IS NULL OR valoarepensie >= 0));
ALTER TABLE cereripensie ADD CONSTRAINT ck_date_invaliditate
    CHECK (tippensie <> 'INVALIDITATE' OR gradinvaliditate IS NOT NULL);
ALTER TABLE cereripensie ADD CONSTRAINT ck_date_urmas
    CHECK (tippensie <> 'URMAS' OR (nrurmasi IS NOT NULL AND cupon IS NOT NULL));
ALTER TABLE cereripensie ADD CONSTRAINT ck_flux_valoare
    CHECK (status NOT IN ('PENSIE_CALCULATA', 'IN_PLATA') OR valoarepensie IS NOT NULL);
ALTER TABLE cereripensie ADD CONSTRAINT ck_flux_plata CHECK
    ((status = 'IN_PLATA' AND dataplata IS NOT NULL) OR
     (status <> 'IN_PLATA' AND dataplata IS NULL));

CREATE TABLE IF NOT EXISTS salarii (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cerere INTEGER NOT NULL REFERENCES cereripensie(id) ON DELETE CASCADE,
    an_cotizare INTEGER NOT NULL CHECK (an_cotizare BETWEEN 1 AND 60),
    salariu_brut_mediu NUMERIC(12, 2) NOT NULL CHECK (salariu_brut_mediu > 0),
    CONSTRAINT uq_salariu_cerere_an UNIQUE (id_cerere, an_cotizare)
);

CREATE INDEX IF NOT EXISTS idx_salarii_id_cerere ON salarii(id_cerere);

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

COMMIT;
