package service;

import model.Cerere;
import model.Salariu;
import model.StatisticiDashboard;
import model.StatusCerere;
import model.TipPensie;
import repository.CerereRepository;
import repository.ParametruPensieRepository;
import repository.SalariuRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CerereService {
    private final CerereRepository cerereRepository;
    private final SalariuRepository salariuRepository;
    private final CalculatorPensieService calculator;
    private final ParametruPensieRepository parametruRepository;

    public CerereService() {
        this(new CerereRepository(), new SalariuRepository(), new CalculatorPensieService(),
                new ParametruPensieRepository());
    }

    public CerereService(CerereRepository cerereRepository, SalariuRepository salariuRepository,
                         CalculatorPensieService calculator) {
        this(cerereRepository, salariuRepository, calculator, new ParametruPensieRepository());
    }

    public CerereService(CerereRepository cerereRepository, SalariuRepository salariuRepository,
                         CalculatorPensieService calculator,
                         ParametruPensieRepository parametruRepository) {
        this.cerereRepository = cerereRepository;
        this.salariuRepository = salariuRepository;
        this.calculator = calculator;
        this.parametruRepository = parametruRepository;
    }

    public String depuneLimitaVarsta(String nume, String prenume, String adresa,
                                     int varsta, int stagiu, String sex) {
        String numar = genereazaNumar("VAR");
        depune(new Cerere(0, nume, prenume, adresa, TipPensie.LIMITA_VARSTA, varsta, stagiu, sex,
                null, null, null, numar, StatusCerere.DEPUSA, null, null, null));
        return numar;
    }

    public String depuneInvaliditate(String nume, String prenume, String adresa, int varsta,
                                     int stagiu, String sex, int gradInvaliditate) {
        String numar = genereazaNumar("INV");
        depune(new Cerere(0, nume, prenume, adresa, TipPensie.INVALIDITATE, varsta, stagiu, sex,
                gradInvaliditate, null, null, numar, StatusCerere.DEPUSA, null, null, null));
        return numar;
    }

    public String depuneUrmas(String nume, String prenume, String adresa, int nrUrmasi, double cupon) {
        String numar = genereazaNumar("URM");
        depune(new Cerere(0, nume, prenume, adresa, TipPensie.URMAS, 0, 0, "N/A",
                null, nrUrmasi, cupon, numar, StatusCerere.DEPUSA, null, null, null));
        return numar;
    }

    public Optional<Cerere> gasesteDupaNumar(String numar) {
        return cerereRepository.gasesteDupaNumar(numar);
    }

    public boolean conexiuneDisponibila() {
        return cerereRepository.conexiuneDisponibila();
    }

    public List<Cerere> gasesteToate() {
        return cerereRepository.gasesteToate();
    }

    public List<Salariu> gasesteSalarii(int idCerere) {
        return salariuRepository.gasesteDupaCerere(idCerere);
    }

    public Cerere incepeVerificarea(int idCerere) {
        Cerere cerere = obtine(idCerere);
        cereStatus(cerere, StatusCerere.DEPUSA,
                "Doar o cerere depusă poate intra în verificare.");
        cerereRepository.actualizeazaStatus(idCerere, StatusCerere.IN_VERIFICARE);
        return obtine(idCerere);
    }

    public Cerere valideaza(int idCerere) {
        return valideaza(idCerere, null);
    }

    public Cerere valideaza(int idCerere, String motivRespingere) {
        Cerere cerere = obtine(idCerere);
        cereStatus(cerere, StatusCerere.IN_VERIFICARE,
                "Cererea trebuie să fie în verificare înainte de validare.");
        boolean eligibil = esteEligibila(cerere);
        String motiv = motivRespingere == null ? null : motivRespingere.trim();
        if (!eligibil && (motiv == null || motiv.isEmpty())) {
            throw new IllegalArgumentException("Motivul respingerii este obligatoriu.");
        }
        cerereRepository.finalizeazaValidare(idCerere,
                eligibil ? StatusCerere.ADMISA : StatusCerere.RESPINSA,
                eligibil ? null : motiv);
        return obtine(idCerere);
    }

    public boolean esteEligibila(Cerere cerere) {
        return switch (cerere.getTipPensie()) {
            case LIMITA_VARSTA -> calculator.esteEligibilLimitaVarsta(
                    cerere.getSex(), cerere.getVarsta(), cerere.getStagiu());
            case INVALIDITATE -> calculator.esteEligibilInvaliditate(
                    cerere.getVarsta(), cerere.getStagiu());
            case URMAS -> cerere.getNrUrmasi() != null && cerere.getNrUrmasi() >= 1 &&
                    cerere.getCupon() != null && cerere.getCupon() > 0;
        };
    }

    public Cerere actualizeazaDate(Cerere dateNoi) {
        Cerere existent = obtine(dateNoi.getId());
        if (existent.getStatus() == StatusCerere.IN_PLATA ||
                existent.getStatus() == StatusCerere.PENSIE_CALCULATA) {
            throw new IllegalStateException("Datele unui dosar calculat sau pus în plată nu mai pot fi modificate.");
        }
        valideazaDateSpecifice(dateNoi);
        cerereRepository.actualizeazaDate(dateNoi);
        if (existent.getStatus() == StatusCerere.ADMISA || existent.getStatus() == StatusCerere.RESPINSA) {
            cerereRepository.actualizeazaStatus(dateNoi.getId(), StatusCerere.IN_VERIFICARE);
        }
        return obtine(dateNoi.getId());
    }

    public void salveazaSalarii(int idCerere, List<Salariu> salarii) {
        Cerere cerere = obtine(idCerere);
        permiteCalcul(cerere);
        if (cerere.getTipPensie() == TipPensie.URMAS) {
            throw new IllegalStateException("Pensia de urmaș nu folosește istoricul salariilor.");
        }
        valideazaSalarii(cerere, salarii);
        salariuRepository.salveazaSiInvalideazaCalcul(idCerere, salarii);
    }

    public CalculatorPensieService.RezultatCalcul salveazaSiCalculeaza(
            int idCerere, List<Salariu> salarii) {
        Cerere cerere = obtine(idCerere);
        permiteCalcul(cerere);
        if (cerere.getTipPensie() == TipPensie.URMAS) {
            throw new IllegalStateException("Pensia de urmaș nu folosește istoricul salariilor.");
        }
        valideazaSalarii(cerere, salarii);
        CalculatorPensieService.RezultatCalcul rezultat = calculeaza(salarii);
        salariuRepository.salveazaSiCalculeaza(idCerere, salarii,
                rezultat.valoarePensie(), genereazaNumar("DEC"));
        return rezultat;
    }

    public CalculatorPensieService.RezultatCalcul calculeazaDinSalarii(int idCerere) {
        Cerere cerere = obtine(idCerere);
        permiteCalcul(cerere);
        List<Salariu> salarii = salariuRepository.gasesteDupaCerere(idCerere);
        valideazaSalarii(cerere, salarii);
        CalculatorPensieService.RezultatCalcul rezultat = calculeaza(salarii);
        cerereRepository.salveazaCalcul(idCerere, rezultat.valoarePensie(), genereazaNumar("DEC"));
        return rezultat;
    }

    public double calculeazaPensieUrmas(int idCerere) {
        Cerere cerere = obtine(idCerere);
        permiteCalcul(cerere);
        if (cerere.getTipPensie() != TipPensie.URMAS || cerere.getCupon() == null || cerere.getNrUrmasi() == null) {
            throw new IllegalStateException("Cererea nu conține date valide pentru pensia de urmaș.");
        }
        double valoare = calculator.calculeazaPensieUrmas(cerere.getCupon(), cerere.getNrUrmasi());
        cerereRepository.salveazaCalcul(idCerere, valoare, genereazaNumar("DEC"));
        return valoare;
    }

    public Cerere puneInPlata(int idCerere) {
        Cerere cerere = obtine(idCerere);
        cereStatus(cerere, StatusCerere.PENSIE_CALCULATA,
                "Doar o pensie calculată poate fi pusă în plată.");
        cerereRepository.puneInPlata(idCerere, LocalDate.now());
        return obtine(idCerere);
    }

    public int numaraDupaStatus(StatusCerere status) {
        return cerereRepository.numaraDupaStatus(status);
    }

    public StatisticiDashboard obtineStatisticiDashboard() {
        return cerereRepository.obtineStatistici();
    }

    private void depune(Cerere cerere) {
        valideazaDateSpecifice(cerere);
        cerereRepository.insereaza(cerere);
    }

    private Cerere obtine(int id) {
        return cerereRepository.gasesteDupaId(id)
                .orElseThrow(() -> new IllegalArgumentException("Cererea nu mai există în baza de date."));
    }

    private void cereStatus(Cerere cerere, StatusCerere status, String mesaj) {
        if (cerere.getStatus() != status) throw new IllegalStateException(mesaj);
    }

    private void permiteCalcul(Cerere cerere) {
        if (cerere.getStatus() != StatusCerere.ADMISA &&
                cerere.getStatus() != StatusCerere.PENSIE_CALCULATA) {
            throw new IllegalStateException("Pensia poate fi calculată numai pentru un dosar admis.");
        }
    }

    private void valideazaDateSpecifice(Cerere cerere) {
        if (cerere.getTipPensie() == TipPensie.INVALIDITATE && cerere.getGradInvaliditate() == null) {
            throw new IllegalArgumentException("Gradul de invaliditate este obligatoriu.");
        }
        if (cerere.getTipPensie() == TipPensie.URMAS &&
                (cerere.getNrUrmasi() == null || cerere.getCupon() == null)) {
            throw new IllegalArgumentException("Numărul de urmași și cuponul sunt obligatorii.");
        }
    }

    private void valideazaSalarii(Cerere cerere, List<Salariu> salarii) {
        if (salarii.size() != cerere.getStagiu()) {
            throw new IllegalArgumentException("Trebuie completat câte un salariu pentru fiecare dintre cei " +
                    cerere.getStagiu() + " ani de cotizare.");
        }
        Set<Integer> ani = new java.util.HashSet<>();
        int anCurent = LocalDate.now().getYear();
        for (Salariu salariu : salarii) {
            if (salariu.getAnCalendaristic() < 1900 || salariu.getAnCalendaristic() > anCurent ||
                    salariu.getSalariuBrutMediu() <= 0 || !ani.add(salariu.getAnCalendaristic())) {
                throw new IllegalArgumentException("Istoricul salariilor este incomplet sau conține valori invalide.");
            }
        }
    }

    private CalculatorPensieService.RezultatCalcul calculeaza(List<Salariu> salarii) {
        Set<Integer> ani = salarii.stream().map(Salariu::getAnCalendaristic).collect(Collectors.toSet());
        return calculator.calculeazaDinSalarii(salarii,
                parametruRepository.gasestePentruAni(ani),
                parametruRepository.gasestePentruData(LocalDate.now()));
    }

    private String genereazaNumar(String prefix) {
        return prefix + "-" + System.currentTimeMillis();
    }
}
