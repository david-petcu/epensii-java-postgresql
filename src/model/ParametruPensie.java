package model;

import java.time.LocalDate;

public class ParametruPensie {
    private final int id;
    private final int an;
    private final double salariuMediu;
    private final double valoarePunct;
    private final LocalDate dataInceput;
    private final LocalDate dataSfarsit;

    public ParametruPensie(int id, int an, double salariuMediu, double valoarePunct,
                           LocalDate dataInceput, LocalDate dataSfarsit) {
        this.id = id;
        this.an = an;
        this.salariuMediu = salariuMediu;
        this.valoarePunct = valoarePunct;
        this.dataInceput = dataInceput;
        this.dataSfarsit = dataSfarsit;
    }

    public int getId() { return id; }
    public int getAn() { return an; }
    public double getSalariuMediu() { return salariuMediu; }
    public double getValoarePunct() { return valoarePunct; }
    public LocalDate getDataInceput() { return dataInceput; }
    public LocalDate getDataSfarsit() { return dataSfarsit; }
}
