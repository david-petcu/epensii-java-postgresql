package service;

import model.StatusCerere;

public class VerificatorCereri extends Thread {
    private final CerereService service = new CerereService();

    public void run() {
        while (!isInterrupted()) {
            try {
                int depuse = service.numaraDupaStatus(StatusCerere.DEPUSA);
                int verificare = service.numaraDupaStatus(StatusCerere.IN_VERIFICARE);
                int admise = service.numaraDupaStatus(StatusCerere.ADMISA);
                int plata = service.numaraDupaStatus(StatusCerere.IN_PLATA);
                System.out.println("Flux cereri - Depuse: " + depuse + ", În verificare: " + verificare +
                        ", Admise: " + admise + ", În plată: " + plata);
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                interrupt();
            } catch (RuntimeException e) {
                System.out.println("Verificarea cererilor a eșuat: " + e.getMessage());
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    interrupt();
                }
            }
        }
    }
}
