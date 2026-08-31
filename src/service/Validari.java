package service;

public final class Validari {
    private Validari() { }

    public static String textObligatoriu(String valoare, String denumire) {
        String text = valoare == null ? "" : valoare.trim();
        if (text.isEmpty()) throw new IllegalArgumentException("Completează câmpul „" + denumire + "”.");
        return text;
    }

    public static String nume(String valoare, String denumire) {
        String text = textObligatoriu(valoare, denumire);
        if (!text.matches("[\\p{L} .'-]+")) {
            throw new IllegalArgumentException(denumire + " poate conține doar litere, spații, cratimă și apostrof.");
        }
        return text;
    }

    public static int numarIntreg(String valoare, String denumire, int minim, int maxim) {
        try {
            int numar = Integer.parseInt(textObligatoriu(valoare, denumire));
            if (numar < minim || numar > maxim) {
                throw new IllegalArgumentException(denumire + " trebuie să fie între " + minim + " și " + maxim + ".");
            }
            return numar;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(denumire + " trebuie să fie un număr întreg.");
        }
    }

    public static double numarReal(String valoare, String denumire, double minim, double maxim) {
        try {
            double numar = Double.parseDouble(textObligatoriu(valoare, denumire).replace(',', '.'));
            if (!Double.isFinite(numar) || numar < minim || numar > maxim) {
                throw new IllegalArgumentException(denumire + " trebuie să fie între " + minim + " și " + maxim + ".");
            }
            return numar;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(denumire + " trebuie să fie un număr valid.");
        }
    }
}
