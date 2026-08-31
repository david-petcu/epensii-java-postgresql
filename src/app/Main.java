package app;

import service.VerificatorCereri;
import ui.EPensiiApp;
import ui.TemaUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TemaUI.instaleaza();
            new EPensiiApp();
        });

        VerificatorCereri verificator = new VerificatorCereri();
        verificator.setDaemon(true);
        verificator.start();
    }
}
