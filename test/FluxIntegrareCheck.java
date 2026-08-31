package integration;

import model.Cerere;
import model.Salariu;
import model.StatusCerere;
import repository.CerereRepository;
import repository.Conectare;
import service.CalculatorPensieService;
import service.CerereService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FluxIntegrareCheck {
    public static void main(String[] args) throws Exception {
        CerereService service = new CerereService();
        String numar = null;
        String numarRespins = null;
        try {
            numar = service.depuneLimitaVarsta("Test", "Integrare", "Adresă test", 65, 35, "M");
            Cerere cerere = service.gasesteDupaNumar(numar).orElseThrow();
            verifica(cerere.getStatus() == StatusCerere.DEPUSA, "Cererea nu pornește ca Depusă");

            boolean tranzitieBlocata = false;
            try {
                new CerereRepository().actualizeazaStatus(cerere.getId(), StatusCerere.IN_PLATA);
            } catch (RuntimeException asteptata) {
                tranzitieBlocata = true;
            }
            verifica(tranzitieBlocata, "Baza a permis o tranziție invalidă Depusă -> În plată");

            cerere = service.incepeVerificarea(cerere.getId());
            verifica(cerere.getStatus() == StatusCerere.IN_VERIFICARE, "Cererea nu a intrat în verificare");
            cerere = service.valideaza(cerere.getId());
            verifica(cerere.getStatus() == StatusCerere.ADMISA, "Cererea eligibilă nu a fost admisă");

            List<Salariu> salarii = new ArrayList<>();
            int primulAn = LocalDate.now().getYear() - 35;
            for (int i = 0; i < 35; i++) salarii.add(new Salariu(0, cerere.getId(), primulAn + i, 7001 + i));
            service.salveazaSiCalculeaza(cerere.getId(), salarii);
            verifica(service.gasesteSalarii(cerere.getId()).size() == 35, "Salariile nu au fost persistate");

            CalculatorPensieService.RezultatCalcul calcul = service.calculeazaDinSalarii(cerere.getId());
            verifica(calcul.valoarePensie() > 0, "Valoarea pensiei nu este validă");
            cerere = service.gasesteDupaNumar(numar).orElseThrow();
            verifica(cerere.getStatus() == StatusCerere.PENSIE_CALCULATA, "Statusul nu este Pensie calculată");

            List<Salariu> salariiCorectate = new ArrayList<>();
            for (int i = 0; i < 35; i++) salariiCorectate.add(new Salariu(0, cerere.getId(), primulAn + i, 8001 + i));
            service.salveazaSalarii(cerere.getId(), salariiCorectate);
            cerere = service.gasesteDupaNumar(numar).orElseThrow();
            verifica(cerere.getStatus() == StatusCerere.ADMISA,
                    "Corectarea salariilor nu a invalidat calculul anterior");
            verifica(service.gasesteSalarii(cerere.getId()).get(0).getSalariuBrutMediu() == 8001,
                    "Salariul corectat nu a fost persistat");
            service.calculeazaDinSalarii(cerere.getId());
            cerere = service.gasesteDupaNumar(numar).orElseThrow();

            cerere = service.puneInPlata(cerere.getId());
            verifica(cerere.getStatus() == StatusCerere.IN_PLATA && cerere.getDataPlata() != null,
                    "Pensia nu a fost pusă corect în plată");

            numarRespins = service.depuneLimitaVarsta("Test", "Respins", "Adresă test", 45, 10, "M");
            Cerere respinsa = service.gasesteDupaNumar(numarRespins).orElseThrow();
            service.incepeVerificarea(respinsa.getId());
            boolean motivObligatoriu = false;
            try { service.valideaza(respinsa.getId()); }
            catch (IllegalArgumentException asteptata) { motivObligatoriu = true; }
            verifica(motivObligatoriu, "Respingerea fără motiv nu a fost blocată");
            respinsa = service.valideaza(respinsa.getId(), "Vârsta și stagiul sunt insuficiente.");
            verifica(respinsa.getStatus() == StatusCerere.RESPINSA && respinsa.getMotivRespingere() != null,
                    "Motivul respingerii nu a fost persistat");
            System.out.println("OK - fluxul complet, salariile și constraints PostgreSQL funcționează.");
        } finally {
            if (numar != null || numarRespins != null) {
                try (Connection conexiune = Conectare.getInstanta().deschideConexiune();
                     PreparedStatement stmt = conexiune.prepareStatement(
                             "DELETE FROM cereripensie WHERE numarinregistrare IN (?, ?)")) {
                    stmt.setString(1, numar);
                    stmt.setString(2, numarRespins);
                    stmt.executeUpdate();
                }
            }
        }
    }

    private static void verifica(boolean conditie, String mesaj) {
        if (!conditie) throw new AssertionError(mesaj);
    }
}
