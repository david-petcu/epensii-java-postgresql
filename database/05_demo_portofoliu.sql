BEGIN;

INSERT INTO cereripensie
    (nume, prenume, adresa, tippensie, varsta, stagiu, sex,
     gradinvaliditate, nrurmasi, cupon, numarinregistrare, status,
     valoarepensie, dataplata, numardecizie, motivrespingere)
VALUES
    ('Dumitrescu', 'Elena', 'Adresă demonstrativă, Brașov', 'LIMITA_VARSTA',
     61, 29, 'F', NULL, NULL, NULL, 'DEMO-DEP-001', 'DEPUSA',
     NULL, NULL, NULL, NULL),
    ('Stan', 'Mihai', 'Adresă demonstrativă, Sibiu', 'INVALIDITATE',
     48, 18, 'M', 2, NULL, NULL, 'DEMO-VER-001', 'IN_VERIFICARE',
     NULL, NULL, NULL, NULL),
    ('Radu', 'Andrei', 'Adresă demonstrativă, Cluj-Napoca', 'LIMITA_VARSTA',
     65, 38, 'M', NULL, NULL, NULL, 'DEMO-ADM-001', 'ADMISA',
     NULL, NULL, NULL, NULL),
    ('Matei', 'Ioana', 'Adresă demonstrativă, Iași', 'LIMITA_VARSTA',
     50, 12, 'F', NULL, NULL, NULL, 'DEMO-RESP-001', 'RESPINSA',
     NULL, NULL, NULL, 'Vârsta și stagiul de cotizare sunt insuficiente.'),
    ('Popa', 'Cristina', 'Adresă demonstrativă, Timișoara', 'INVALIDITATE',
     52, 25, 'F', 2, NULL, NULL, 'DEMO-CALC-001', 'PENSIE_CALCULATA',
     2760.50, NULL, 'DEC-DEMO-001', NULL),
    ('Marinescu', 'Alexandru', 'Adresă demonstrativă, Constanța', 'URMAS',
     0, 0, 'N/A', NULL, 2, 3200.00, 'DEMO-PLATA-001', 'IN_PLATA',
     2400.00, CURRENT_DATE - 30, 'DEC-DEMO-002', NULL),
    ('Tudor', 'Maria', 'Adresă demonstrativă, Oradea', 'URMAS',
     0, 0, 'N/A', NULL, 1, 2850.00, 'DEMO-URM-001', 'DEPUSA',
     NULL, NULL, NULL, NULL)
ON CONFLICT (numarinregistrare) DO NOTHING;

INSERT INTO salarii (id_cerere, an_calendaristic, salariu_brut_mediu)
SELECT c.id, an,
       4300.00 + (an - (EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER - c.stagiu)) * 145.00
FROM cereripensie c
CROSS JOIN LATERAL generate_series(
    EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER - c.stagiu,
    EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER - 1
) AS an
WHERE c.numarinregistrare = 'DEMO-ADM-001'
ON CONFLICT (id_cerere, an_calendaristic) DO NOTHING;

INSERT INTO salarii (id_cerere, an_calendaristic, salariu_brut_mediu)
SELECT c.id, an,
       5100.00 + (an - (EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER - c.stagiu)) * 165.00
FROM cereripensie c
CROSS JOIN LATERAL generate_series(
    EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER - c.stagiu,
    EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER - 1
) AS an
WHERE c.numarinregistrare = 'DEMO-CALC-001'
ON CONFLICT (id_cerere, an_calendaristic) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_cereripensie_status ON cereripensie (status);
CREATE INDEX IF NOT EXISTS idx_cereripensie_tip ON cereripensie (tippensie);
CREATE INDEX IF NOT EXISTS idx_cereripensie_data_plata ON cereripensie (dataplata);

COMMIT;
