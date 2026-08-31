package model;

public enum TipPensie {
    LIMITA_VARSTA("Limită de vârstă"),
    INVALIDITATE("Invaliditate"),
    URMAS("Pensie de urmaș");

    private final String eticheta;

    TipPensie(String eticheta) {
        this.eticheta = eticheta;
    }

    public String getEticheta() {
        return eticheta;
    }

    public static TipPensie dinBaza(String valoare) {
        if (valoare == null) return LIMITA_VARSTA;
        String normalizat = valoare.trim().toUpperCase();
        if (normalizat.contains("URMAS") || normalizat.contains("URMAȘ")) return URMAS;
        if (normalizat.contains("INVALID")) return INVALIDITATE;
        return LIMITA_VARSTA;
    }

    @Override
    public String toString() {
        return eticheta;
    }
}
