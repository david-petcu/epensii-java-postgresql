package service;

import model.ParametruPensie;
import model.Salariu;

import java.util.List;
import java.util.Map;

public class CalculatorPensieService {
    public boolean esteEligibilLimitaVarsta(String sex, int varsta, int stagiu) {
        return ("F".equalsIgnoreCase(sex) && varsta >= 60 && stagiu >= 30) ||
                ("M".equalsIgnoreCase(sex) && varsta >= 65 && stagiu >= 35);
    }

    public boolean esteEligibilInvaliditate(int varsta, int stagiu) {
        return (varsta < 25 && stagiu >= 5) ||
                (varsta <= 31 && stagiu >= 8) ||
                (varsta <= 37 && stagiu >= 11) ||
                (varsta <= 43 && stagiu >= 14) ||
                (varsta <= 49 && stagiu >= 18) ||
                (varsta <= 55 && stagiu >= 22) ||
                (varsta > 55 && stagiu >= 25);
    }

    public double calculeazaPensieUrmas(double cupon, int nrUrmasi) {
        double procent = nrUrmasi == 1 ? 0.50 : nrUrmasi == 2 ? 0.75 : 1.0;
        return rotunjeste(cupon * procent);
    }

    public RezultatCalcul calculeazaDinSalarii(List<Salariu> salarii,
                                               Map<Integer, ParametruPensie> parametriPeAni,
                                               ParametruPensie parametruCurent) {
        if (salarii == null || salarii.isEmpty()) {
            throw new IllegalArgumentException("Este necesar cel puțin un salariu pentru calcul.");
        }
        double sumaPunctaje = 0;
        for (Salariu salariu : salarii) {
            if (salariu.getSalariuBrutMediu() <= 0) {
                throw new IllegalArgumentException("Salariile trebuie să fie mai mari decât zero.");
            }
            ParametruPensie parametru = parametriPeAni.get(salariu.getAnCalendaristic());
            if (parametru == null) {
                throw new IllegalArgumentException("Lipsesc parametrii de calcul pentru anul " +
                        salariu.getAnCalendaristic() + ".");
            }
            sumaPunctaje += salariu.getSalariuBrutMediu() / parametru.getSalariuMediu();
        }
        double punctajMediu = sumaPunctaje / salarii.size();
        double pensie = rotunjeste(punctajMediu * parametruCurent.getValoarePunct());
        return new RezultatCalcul(punctajMediu, parametruCurent.getValoarePunct(), pensie);
    }

    private double rotunjeste(double valoare) {
        return Math.round(valoare * 100.0) / 100.0;
    }

    public record RezultatCalcul(double punctajMediu, double valoarePunct, double valoarePensie) { }
}
