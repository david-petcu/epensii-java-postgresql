package ui;

import service.CerereService;
import service.Validari;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PensieVarstaPanel extends JPanel {
    private final CerereService service = new CerereService();
    private final JTextField tfNume = new JTextField();
    private final JTextField tfPrenume = new JTextField();
    private final JTextField tfAdresa = new JTextField();
    private final JTextField tfVarsta = new JTextField();
    private final JTextField tfStagiu = new JTextField();
    private final JComboBox<String> cbSex = new JComboBox<>(new String[]{"M", "F"});
    private final JTextArea taOutput = new JTextArea(5, 45);

    public PensieVarstaPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(720, 600));
        add(TemaUI.antet("Pensie pentru limită de vârstă",
                "Completează datele solicitantului; eligibilitatea este verificată automat."), BorderLayout.NORTH);

        JPanel continut = new JPanel(new BorderLayout(0, 16));
        continut.setBorder(new EmptyBorder(20, 26, 20, 26));
        JPanel form = TemaUI.card();
        form.setLayout(new GridBagLayout());
        adaugaRand(form, 0, "Nume", tfNume);
        adaugaRand(form, 1, "Prenume", tfPrenume);
        adaugaRand(form, 2, "Adresă", tfAdresa);
        adaugaRand(form, 3, "Vârstă", tfVarsta);
        adaugaRand(form, 4, "Stagiu de cotizare (ani)", tfStagiu);
        adaugaRand(form, 5, "Sex", cbSex);
        continut.add(form, BorderLayout.CENTER);

        TemaUI.configureazaOutput(taOutput);
        JPanel jos = new JPanel(new BorderLayout(0, 12));
        jos.add(new JScrollPane(taOutput), BorderLayout.CENTER);
        JButton btnSalveaza = TemaUI.butonPrincipal("Înregistrează cererea");
        JButton btnCurata = TemaUI.butonSecundar("Curăță");
        JButton btnInapoi = TemaUI.butonSecundar("Închide");
        btnSalveaza.addActionListener(e -> salveaza());
        btnCurata.addActionListener(e -> curata());
        btnInapoi.addActionListener(e -> TemaUI.inchideFereastra(this));
        JPanel butoane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        butoane.add(btnCurata);
        butoane.add(btnInapoi);
        butoane.add(btnSalveaza);
        jos.add(butoane, BorderLayout.SOUTH);
        continut.add(jos, BorderLayout.SOUTH);
        add(continut, BorderLayout.CENTER);
    }

    private void salveaza() {
        try {
            String nume = Validari.nume(tfNume.getText(), "Numele");
            String prenume = Validari.nume(tfPrenume.getText(), "Prenumele");
            String adresa = Validari.textObligatoriu(tfAdresa.getText(), "Adresa");
            int varsta = Validari.numarIntreg(tfVarsta.getText(), "Vârsta", 0, 120);
            int stagiu = Validari.numarIntreg(tfStagiu.getText(), "Stagiul", 0, 60);
            String sex = (String) cbSex.getSelectedItem();
            String numarInregistrare = service.depuneLimitaVarsta(
                    nume, prenume, adresa, varsta, stagiu, sex);
            TemaUI.mesaj(taOutput, "Cererea a fost depusă cu succes.\n" +
                    "Număr de înregistrare: " + numarInregistrare + "\n" +
                    "Solicitant: " + nume + " " + prenume + "\nStatus: Depusă", true);
        } catch (RuntimeException ex) {
            TemaUI.mesaj(taOutput, ex.getMessage(), false);
        }
    }

    private void curata() {
        tfNume.setText(""); tfPrenume.setText(""); tfAdresa.setText("");
        tfVarsta.setText(""); tfStagiu.setText(""); cbSex.setSelectedIndex(0); taOutput.setText("");
        tfNume.requestFocusInWindow();
    }

    private void adaugaRand(JPanel panel, int rand, String eticheta, JComponent camp) {
        TemaUI.configureazaCamp(camp);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = rand; c.insets = new Insets(7, 7, 7, 12); c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; panel.add(new JLabel(eticheta + ":"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; panel.add(camp, c);
    }
}
