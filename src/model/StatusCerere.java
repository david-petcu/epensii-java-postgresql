package model;

public enum StatusCerere {
    DEPUSA("Depusă"),
    IN_VERIFICARE("În verificare"),
    ADMISA("Admisă"),
    RESPINSA("Respinsă"),
    PENSIE_CALCULATA("Pensie calculată"),
    IN_PLATA("În plată");

    private final String eticheta;

    StatusCerere(String eticheta) {
        this.eticheta = eticheta;
    }

    public String getEticheta() {
        return eticheta;
    }

    public static StatusCerere dinBaza(String valoare) {
        if (valoare == null) return DEPUSA;
        return switch (valoare.trim().toUpperCase()) {
            case "ADMIS", "ADMISA", "ADMISĂ" -> ADMISA;
            case "RESPINS", "RESPINSA", "RESPINSĂ" -> RESPINSA;
            case "IN_VERIFICARE", "ÎN VERIFICARE" -> IN_VERIFICARE;
            case "PENSIE_CALCULATA", "PENSIE CALCULATĂ" -> PENSIE_CALCULATA;
            case "IN_PLATA", "ÎN PLATĂ" -> IN_PLATA;
            default -> DEPUSA;
        };
    }

    @Override
    public String toString() {
        return eticheta;
    }
}
