package model;

import java.time.LocalDate;

public class Cerere {
    private final int id;
    private final String nume;
    private final String prenume;
    private final String adresa;
    private final TipPensie tipPensie;
    private final int varsta;
    private final int stagiu;
    private final String sex;
    private final Integer gradInvaliditate;
    private final Integer nrUrmasi;
    private final Double cupon;
    private final String numarInregistrare;
    private final StatusCerere status;
    private final Double valoarePensie;
    private final LocalDate dataPlata;
    private final String numarDecizie;
    private final String motivRespingere;

    public Cerere(int id, String nume, String prenume, String adresa, TipPensie tipPensie,
                  int varsta, int stagiu, String sex, Integer gradInvaliditate, Integer nrUrmasi,
                  Double cupon, String numarInregistrare, StatusCerere status,
                  Double valoarePensie, LocalDate dataPlata, String numarDecizie) {
        this(id, nume, prenume, adresa, tipPensie, varsta, stagiu, sex, gradInvaliditate,
                nrUrmasi, cupon, numarInregistrare, status, valoarePensie, dataPlata,
                numarDecizie, null);
    }

    public Cerere(int id, String nume, String prenume, String adresa, TipPensie tipPensie,
                  int varsta, int stagiu, String sex, Integer gradInvaliditate, Integer nrUrmasi,
                  Double cupon, String numarInregistrare, StatusCerere status,
                  Double valoarePensie, LocalDate dataPlata, String numarDecizie,
                  String motivRespingere) {
        this.id = id;
        this.nume = nume;
        this.prenume = prenume;
        this.adresa = adresa;
        this.tipPensie = tipPensie;
        this.varsta = varsta;
        this.stagiu = stagiu;
        this.sex = sex;
        this.gradInvaliditate = gradInvaliditate;
        this.nrUrmasi = nrUrmasi;
        this.cupon = cupon;
        this.numarInregistrare = numarInregistrare;
        this.status = status;
        this.valoarePensie = valoarePensie;
        this.dataPlata = dataPlata;
        this.numarDecizie = numarDecizie;
        this.motivRespingere = motivRespingere;
    }

    public int getId() { return id; }
    public String getNume() { return nume; }
    public String getPrenume() { return prenume; }
    public String getAdresa() { return adresa; }
    public TipPensie getTipPensie() { return tipPensie; }
    public int getVarsta() { return varsta; }
    public int getStagiu() { return stagiu; }
    public String getSex() { return sex; }
    public Integer getGradInvaliditate() { return gradInvaliditate; }
    public Integer getNrUrmasi() { return nrUrmasi; }
    public Double getCupon() { return cupon; }
    public String getNumarInregistrare() { return numarInregistrare; }
    public StatusCerere getStatus() { return status; }
    public Double getValoarePensie() { return valoarePensie; }
    public LocalDate getDataPlata() { return dataPlata; }
    public String getNumarDecizie() { return numarDecizie; }
    public String getMotivRespingere() { return motivRespingere; }
}
