package repository;

import model.ParametruPensie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ParametruPensieRepository {
    private final Conectare conectare = Conectare.getInstanta();

    public Map<Integer, ParametruPensie> gasestePentruAni(Collection<Integer> ani) {
        Map<Integer, ParametruPensie> rezultat = new HashMap<>();
        if (ani.isEmpty()) return rezultat;
        String marcaje = String.join(",", java.util.Collections.nCopies(ani.size(), "?"));
        String sql = "SELECT * FROM parametri_pensie WHERE an IN (" + marcaje + ")";
        try (Connection conexiune = conectare.deschideConexiune();
             PreparedStatement stmt = conexiune.prepareStatement(sql)) {
            int index = 1;
            for (Integer an : ani) stmt.setInt(index++, an);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ParametruPensie parametru = citeste(rs);
                    rezultat.put(parametru.getAn(), parametru);
                }
            }
            return rezultat;
        } catch (SQLException e) {
            throw new RepositoryException("Parametrii de pensie nu au putut fi citiți: " + e.getMessage(), e);
        }
    }

    public ParametruPensie gasestePentruData(LocalDate data) {
        String sql = "SELECT * FROM parametri_pensie WHERE ? BETWEEN data_inceput AND data_sfarsit " +
                "ORDER BY data_inceput DESC LIMIT 1";
        try (Connection conexiune = conectare.deschideConexiune();
             PreparedStatement stmt = conexiune.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(data));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return citeste(rs);
                throw new RepositoryException("Nu există parametri de pensie pentru data " + data, null);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Parametrul curent nu a putut fi citit: " + e.getMessage(), e);
        }
    }

    private ParametruPensie citeste(ResultSet rs) throws SQLException {
        return new ParametruPensie(rs.getInt("id"), rs.getInt("an"),
                rs.getDouble("salariu_mediu"), rs.getDouble("valoare_punct"),
                rs.getDate("data_inceput").toLocalDate(), rs.getDate("data_sfarsit").toLocalDate());
    }
}
