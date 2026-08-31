package ui;

import service.CerereService;
import service.Validari;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PensieUrmasPanel extends JPanel {
    private final CerereService service = new CerereService();
    private final JTextField tfNume = new JTextField();
    private final JTextField tfPrenume = new JTextField();
    private final JTextField tfAdresa = new JTextField();
    private final JSpinner spNrUrmasi = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
    private final JTextField tfCupon = new JTextField();
    private final JTextArea taOutput = new JTextArea(6, 45);

    public PensieUrmasPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(720, 600));
        add(TemaUI.antet("Pensie de urmaș",
                "Cererea va fi verificată înainte ca pensia să fie calculată și pusă în plată."), BorderLayout.NORTH);
        JPanel continut = new JPanel(new BorderLayout(0, 16));
        continut.setBorder(new EmptyBorder(20, 26, 20, 26));
        JPanel form = TemaUI.card();
        form.setLayout(new GridBagLayout());
        adaugaRand(form, 0, "Nume beneficiar", tfNume);
        adaugaRand(form, 1, "Prenume beneficiar", tfPrenume);
        adaugaRand(form, 2, "Adresă", tfAdresa);
        adaugaRand(form, 3, "Număr de urmași", spNrUrmasi);
        adaugaRand(form, 4, "Cupon de referință (RON)", tfCupon);
        continut.add(form, BorderLayout.CENTER);

        TemaUI.configureazaOutput(taOutput);
        JPanel jos = new JPanel(new BorderLayout(0, 12));
        jos.add(new JScrollPane(taOutput), BorderLayout.CENTER);
        JButton btnSalveaza = TemaUI.butonPrincipal("Depune cererea");
        JButton btnCurata = TemaUI.butonSecundar("Curăță");
        JButton btnInchide = TemaUI.butonSecundar("Închide");
        btnSalveaza.addActionListener(e -> salveaza());
        btnCurata.addActionListener(e -> curata());
        btnInchide.addActionListener(e -> TemaUI.inchideFereastra(this));
        JPanel butoane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        butoane.add(btnCurata); butoane.add(btnInchide); butoane.add(btnSalveaza);
        jos.add(butoane, BorderLayout.SOUTH);
        continut.add(jos, BorderLayout.SOUTH);
        add(continut, BorderLayout.CENTER);
    }

    private void salveaza() {
        try {
            String nume = Validari.nume(tfNume.getText(), "Numele");
            String prenume = Validari.nume(tfPrenume.getText(), "Prenumele");
            String adresa = Validari.textObligatoriu(tfAdresa.getText(), "Adresa");
            int nrUrmasi = (Integer) spNrUrmasi.getValue();
            double cupon = Validari.numarReal(tfCupon.getText(), "Cuponul", 500, 10000);
            String numarInregistrare = service.depuneUrmas(nume, prenume, adresa, nrUrmasi, cupon);
            TemaUI.mesaj(taOutput, "Cererea a fost depusă cu succes.\n" +
                    "Număr de înregistrare: " + numarInregistrare + "\n" +
                    "Status: Depusă\nValoarea va fi calculată după admiterea dosarului.", true);
        } catch (RuntimeException ex) {
            TemaUI.mesaj(taOutput, ex.getMessage(), false);
        }
    }

    private void curata() {
        tfNume.setText(""); tfPrenume.setText(""); tfAdresa.setText("");
        spNrUrmasi.setValue(1); tfCupon.setText(""); taOutput.setText("");
        tfNume.requestFocusInWindow();
    }

    private void adaugaRand(JPanel panel, int rand, String eticheta, JComponent camp) {
        TemaUI.configureazaCamp(camp);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = rand; c.insets = new Insets(8, 7, 8, 12); c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; panel.add(new JLabel(eticheta + ":"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; panel.add(camp, c);
    }
}
