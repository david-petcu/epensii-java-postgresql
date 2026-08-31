package repository;

public class RepositoryException extends RuntimeException {
    public RepositoryException(String mesaj, Throwable cauza) {
        super(mesaj, cauza);
    }
}
