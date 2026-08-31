package repository;

import model.Cerere;
import model.StatusCerere;
import model.StatisticiDashboard;
import model.TipPensie;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CerereRepository {
    private final Conectare conectare = Conectare.getInstanta();

    public boolean conexiuneDisponibila() {
        try (Connection ignored = conectare.deschideConexiune()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean insereaza(Cerere cerere) {
        String sql = "INSERT INTO cereripensie " +
                "(nume, prenume, adresa, tippensie, varsta, stagiu, sex, gradinvaliditate, " +
                "nrurmasi, cupon, numarinregistrare, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexiune = conectare.deschideConexiune();
             PreparedStatement stmt = conexiune.prepareStatement(sql)) {
            stmt.setString(1, cerere.getNume());
            stmt.setString(2, cerere.getPrenume());
            stmt.setString(3, cerere.getAdresa());
            stmt.setString(4, cerere.getTipPensie().name());
            stmt.setInt(5, cerere.getVarsta());
            stmt.setInt(6, cerere.getStagiu());
            stmt.setString(7, cerere.getSex());
            stmt.setObject(8, cerere.getGradInvaliditate(), Types.INTEGER);
            stmt.setObject(9, cerere.getNrUrmasi(), Types.INTEGER);
            stmt.setObject(10, cerere.getCupon(), Types.DOUBLE);
            stmt.setString(11, cerere.getNumarInregistrare());
            stmt.setString(12, cerere.getStatus().name());
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            throw eroare("Cererea nu a putut fi înregistrată", e);
        }
    }

    public Optional<Cerere> gasesteDupaNumar(String numarInregistrare) {
        String sql = "SELECT * FROM cereripensie WHERE numarinregistrare = ?";
        try (Connection conexiune = conectare.deschideConexiune();
             PreparedStatement stmt = conexiune.prepareStatement(sql)) {
            stmt.setString(1, numarInregistrare);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(citeste(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw eroare("Cererea nu a putut fi citită", e);
        }
    }

    public Optional<Cerere> gasesteDupaId(int id) {
        String sql = "SELECT * FROM cereripensie WHERE id = ?";
        try (Connection conexiune = conectare.deschideConexiune();
             PreparedStatement stmt = conexiune.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(citeste(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw eroare("Cererea nu a putut fi citită", e);
        }
    }

    public List<Cerere> gasesteToate() {
        List<Cerere> cereri = new ArrayList<>();
        String sql = "SELECT * FROM cereripensie ORDER BY id";
        try (Connection conexiune = conectare.deschideConexiune();
             PreparedStatement stmt = conexiune.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) cereri.add(citeste(rs));
            return cereri;
        } catch (SQLException e) {
            throw eroare("Registrul cererilor nu a putut fi citit", e);
        }
    }

    public boolean actualizeazaDate(Cerere cerere) {
        String sql = "UPDATE cereripensie SET nume = ?, prenume = ?, adresa = ?, tippensie = ?, " +
                "varsta = ?, stagiu = ?, sex = ?, gradinvaliditate = ?, nrurmasi = ?, cupon = ?, " +
                "numarinregistrare = ? WHERE id = ?";
        try (Connection conexiune = conectare.deschideConexiune();
             PreparedStatement stmt = conexiune.prepareStatement(sql)) {
            stmt.setString(1, cerere.getNume());
            stmt.setString(2, cerere.getPrenume());
            stmt.setString(3, cerere.getAdresa());
            stmt.setString(4, cerere.getTipPensie().name());
            stmt.setInt(5, cerere.getVarsta());
            stmt.setInt(6, cerere.getStagiu());
            stmt.setString(7, cerere.getSex());
            stmt.setObject(8, cerere.getGradInvaliditate(), Types.INTEGER);
            stmt.setObject(9, cerere.getNrUrmasi(), Types.INTEGER);
            stmt.setObject(10, cerere.getCupon(), Types.DOUBLE);
            stmt.setString(11, cerere.getNumarInregistrare());
            stmt.setInt(12, cerere.getId());
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            throw eroare("Datele cererii nu au putut fi actualizate", e);
        }
    }

    public boolean actualizeazaStatus(int id, StatusCerere status) {
        return executaUpdate("UPDATE cereripensie SET status = ?, " +
                "motivrespingere = CASE WHEN ? = 'RESPINSA' THEN motivrespingere ELSE NULL END WHERE id = ?", stmt -> {
            stmt.setString(1, status.name());
            stmt.setString(2, status.name());
            stmt.setInt(3, id);
        }, "Statusul nu a putut fi actualizat");
    }

    public boolean finalizeazaValidare(int id, StatusCerere status, String motivRespingere) {
        String sql = "UPDATE cereripensie SET status = ?, motivrespingere = ? WHERE id = ?";
        return executaUpdate(sql, stmt -> {
            stmt.setString(1, status.name());
            stmt.setString(2, motivRespingere);
            stmt.setInt(3, id);
        }, "Validarea nu a putut fi finalizată");
    }

    public boolean salveazaCalcul(int id, double valoarePensie, String numarDecizie) {
        String sql = "UPDATE cereripensie SET valoarepensie = ?, numardecizie = ?, " +
                "dataplata = NULL, status = 'PENSIE_CALCULATA' WHERE id = ?";
        return executaUpdate(sql, stmt -> {
            stmt.setDouble(1, valoarePensie);
            stmt.setString(2, numarDecizie);
            stmt.setInt(3, id);
        }, "Calculul pensiei nu a putut fi salvat");
    }

    public boolean invalideazaCalcul(int id) {
        String sql = "UPDATE cereripensie SET valoarepensie = NULL, numardecizie = NULL, " +
                "dataplata = NULL, status = 'ADMISA' WHERE id = ?";
        return executaUpdate(sql, stmt -> stmt.setInt(1, id),
                "Calculul anterior nu a putut fi invalidat");
    }

    public boolean puneInPlata(int id, LocalDate dataPlata) {
        String sql = "UPDATE cereripensie SET dataplata = ?, status = 'IN_PLATA' WHERE id = ?";
        return executaUpdate(sql, stmt -> {
            stmt.setDate(1, Date.valueOf(dataPlata));
            stmt.setInt(2, id);
        }, "Pensia nu a putut fi pusă în plată");
    }

    public int numaraDupaStatus(StatusCerere status) {
        String sql = "SELECT COUNT(*) FROM cereripensie WHERE status = ?";
        try (Connection conexiune = conectare.deschideConexiune();
             PreparedStatement stmt = conexiune.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw eroare("Cererile nu au putut fi numărate", e);
        }
    }

    public StatisticiDashboard obtineStatistici() {
        java.util.EnumMap<StatusCerere, Integer> numar = new java.util.EnumMap<>(StatusCerere.class);
        String sqlStatus = "SELECT status, COUNT(*) FROM cereripensie GROUP BY status";
        String sqlTotal = "SELECT COALESCE(SUM(valoarepensie), 0) FROM cereripensie WHERE status = 'IN_PLATA'";
        try (Connection conexiune = conectare.deschideConexiune()) {
            try (PreparedStatement stmt = conexiune.prepareStatement(sqlStatus);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) numar.put(StatusCerere.dinBaza(rs.getString(1)), rs.getInt(2));
            }
            double total;
            try (PreparedStatement stmt = conexiune.prepareStatement(sqlTotal);
                 ResultSet rs = stmt.executeQuery()) {
                total = rs.next() ? rs.getDouble(1) : 0;
            }
            return new StatisticiDashboard(numar, total);
        } catch (SQLException e) {
            throw eroare("Statisticile nu au putut fi citite", e);
        }
    }

    private Cerere citeste(ResultSet rs) throws SQLException {
        Date data = rs.getDate("dataplata");
        return new Cerere(
                rs.getInt("id"), rs.getString("nume"), rs.getString("prenume"), rs.getString("adresa"),
                TipPensie.dinBaza(rs.getString("tippensie")), rs.getInt("varsta"), rs.getInt("stagiu"),
                rs.getString("sex"), (Integer) rs.getObject("gradinvaliditate"),
                (Integer) rs.getObject("nrurmasi"), rs.getObject("cupon") == null ? null : rs.getDouble("cupon"),
                rs.getString("numarinregistrare"), StatusCerere.dinBaza(rs.getString("status")),
                rs.getObject("valoarepensie") == null ? null : rs.getDouble("valoarepensie"),
                data == null ? null : data.toLocalDate(), rs.getString("numardecizie"),
                rs.getString("motivrespingere"));
    }

    private boolean executaUpdate(String sql, Parametri parametri, String mesaj) {
        try (Connection conexiune = conectare.deschideConexiune();
             PreparedStatement stmt = conexiune.prepareStatement(sql)) {
            parametri.completeaza(stmt);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            throw eroare(mesaj, e);
        }
    }

    private RepositoryException eroare(String mesaj, SQLException e) {
        return new RepositoryException(mesaj + ": " + e.getMessage(), e);
    }

    @FunctionalInterface
    private interface Parametri {
        void completeaza(PreparedStatement stmt) throws SQLException;
    }
}
