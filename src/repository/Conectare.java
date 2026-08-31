package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conectare {
    private static final String URL_IMPLICIT = "jdbc:postgresql://localhost:5432/epensii";
    private static final String UTILIZATOR_IMPLICIT = "postgres";
    private static final Conectare INSTANTA = new Conectare();

    private final String url;
    private final String utilizator;
    private final String parola;

    private Conectare() {
        url = citesteConfigurare("EPENSII_DB_URL", URL_IMPLICIT);
        utilizator = citesteConfigurare("EPENSII_DB_USER", UTILIZATOR_IMPLICIT);
        parola = citesteConfigurare("EPENSII_DB_PASSWORD", "");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Driverul PostgreSQL JDBC nu a fost gasit. Importa proiectul Maven sau adauga driverul in classpath.", e);
        }
    }

    public static Conectare getInstanta() {
        return INSTANTA;
    }

    public Connection deschideConexiune() throws SQLException {
        return DriverManager.getConnection(url, utilizator, parola);
    }

    private static String citesteConfigurare(String nume, String valoareImplicita) {
        String valoare = System.getenv(nume);
        return valoare == null || valoare.isBlank() ? valoareImplicita : valoare;
    }
}
