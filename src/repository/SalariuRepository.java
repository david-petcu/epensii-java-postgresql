package repository;

import model.Salariu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalariuRepository {
    private final Conectare conectare = Conectare.getInstanta();

    public List<Salariu> gasesteDupaCerere(int idCerere) {
        List<Salariu> salarii = new ArrayList<>();
        String sql = "SELECT * FROM salarii WHERE id_cerere = ? ORDER BY an_calendaristic";
        try (Connection conexiune = conectare.deschideConexiune();
             PreparedStatement stmt = conexiune.prepareStatement(sql)) {
            stmt.setInt(1, idCerere);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    salarii.add(new Salariu(rs.getInt("id"), rs.getInt("id_cerere"),
                            rs.getInt("an_calendaristic"), rs.getDouble("salariu_brut_mediu")));
                }
            }
            return salarii;
        } catch (SQLException e) {
            throw new RepositoryException("Salariile nu au putut fi citite: " + e.getMessage(), e);
        }
    }

    public void salveazaSiInvalideazaCalcul(int idCerere, List<Salariu> salarii) {
        executaTranzactie(idCerere, salarii,
                "UPDATE cereripensie SET valoarepensie = NULL, numardecizie = NULL, " +
                        "dataplata = NULL, status = 'ADMISA' " +
                        "WHERE id = ? AND status IN ('ADMISA', 'PENSIE_CALCULATA')",
                null, null);
    }

    public void salveazaSiCalculeaza(int idCerere, List<Salariu> salarii,
                                     double valoarePensie, String numarDecizie) {
        executaTranzactie(idCerere, salarii,
                "UPDATE cereripensie SET valoarepensie = ?, numardecizie = ?, dataplata = NULL, " +
                        "status = 'PENSIE_CALCULATA' " +
                        "WHERE id = ? AND status IN ('ADMISA', 'PENSIE_CALCULATA')",
                valoarePensie, numarDecizie);
    }

    private void executaTranzactie(int idCerere, List<Salariu> salarii, String actualizareCerere,
                                   Double valoarePensie, String numarDecizie) {
        Connection conexiune = null;
        try {
            conexiune = conectare.deschideConexiune();
            conexiune.setAutoCommit(false);
            try (PreparedStatement stmt = conexiune.prepareStatement(
                    "DELETE FROM salarii WHERE id_cerere = ?")) {
                stmt.setInt(1, idCerere);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conexiune.prepareStatement(
                    "INSERT INTO salarii (id_cerere, an_calendaristic, salariu_brut_mediu) VALUES (?, ?, ?)")) {
                for (Salariu salariu : salarii) {
                    stmt.setInt(1, idCerere);
                    stmt.setInt(2, salariu.getAnCalendaristic());
                    stmt.setDouble(3, salariu.getSalariuBrutMediu());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            try (PreparedStatement stmt = conexiune.prepareStatement(actualizareCerere)) {
                if (valoarePensie == null) {
                    stmt.setInt(1, idCerere);
                } else {
                    stmt.setDouble(1, valoarePensie);
                    stmt.setString(2, numarDecizie);
                    stmt.setInt(3, idCerere);
                }
                if (stmt.executeUpdate() != 1) {
                    throw new SQLException("Statusul cererii nu permite operația.");
                }
            }
            conexiune.commit();
        } catch (SQLException e) {
            if (conexiune != null) {
                try { conexiune.rollback(); } catch (SQLException ignored) { }
            }
            throw new RepositoryException("Tranzacția salarii-calcul a eșuat: " + e.getMessage(), e);
        } finally {
            if (conexiune != null) {
                try { conexiune.close(); } catch (SQLException ignored) { }
            }
        }
    }
}
