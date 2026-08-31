package model;

public class Salariu {
    private final int id;
    private final int idCerere;
    private final int anCalendaristic;
    private final double salariuBrutMediu;

    public Salariu(int id, int idCerere, int anCalendaristic, double salariuBrutMediu) {
        this.id = id;
        this.idCerere = idCerere;
        this.anCalendaristic = anCalendaristic;
        this.salariuBrutMediu = salariuBrutMediu;
    }

    public int getId() { return id; }
    public int getIdCerere() { return idCerere; }
    public int getAnCalendaristic() { return anCalendaristic; }
    public double getSalariuBrutMediu() { return salariuBrutMediu; }
}
