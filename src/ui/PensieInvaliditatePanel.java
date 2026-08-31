package ui;

import service.CerereService;
import service.Validari;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PensieInvaliditatePanel extends JPanel {
    private final CerereService service = new CerereService();
    private final JTextField tfNume = new JTextField();
    private final JTextField tfPrenume = new JTextField();
    private final JTextField tfAdresa = new JTextField();
    private final JTextField tfVarsta = new JTextField();
    private final JTextField tfStagiu = new JTextField();
    private final JComboBox<String> cbSex = new JComboBox<>(new String[]{"M", "F"});
    private final JComboBox<Integer> cbGrad = new JComboBox<>(new Integer[]{1, 2, 3});
    private final JTextArea taOutput = new JTextArea(5, 45);

    public PensieInvaliditatePanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(720, 640));
        add(TemaUI.antet("Pensie de invaliditate",
                "Completează datele solicitantului și gradul de invaliditate."), BorderLayout.NORTH);
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
        adaugaRand(form, 6, "Grad de invaliditate", cbGrad);
        continut.add(form, BorderLayout.CENTER);

        TemaUI.configureazaOutput(taOutput);
        JPanel jos = new JPanel(new BorderLayout(0, 12));
        jos.add(new JScrollPane(taOutput), BorderLayout.CENTER);
        JButton btnSalveaza = TemaUI.butonPrincipal("Înregistrează cererea");
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
            int varsta = Validari.numarIntreg(tfVarsta.getText(), "Vârsta", 0, 100);
            int stagiu = Validari.numarIntreg(tfStagiu.getText(), "Stagiul", 0, 60);
            if (varsta - stagiu < 15) {
                throw new IllegalArgumentException("Diferența dintre vârstă și stagiu trebuie să fie de cel puțin 15 ani.");
            }
            int grad = (Integer) cbGrad.getSelectedItem();
            String sex = (String) cbSex.getSelectedItem();
            String numarInregistrare = service.depuneInvaliditate(
                    nume, prenume, adresa, varsta, stagiu, sex, grad);
            TemaUI.mesaj(taOutput, "Cererea a fost depusă cu succes.\n" +
                    "Număr de înregistrare: " + numarInregistrare + "\n" +
                    "Grad de invaliditate: " + grad + "\nStatus: Depusă", true);
        } catch (RuntimeException ex) {
            TemaUI.mesaj(taOutput, ex.getMessage(), false);
        }
    }

    private void curata() {
        tfNume.setText(""); tfPrenume.setText(""); tfAdresa.setText("");
        tfVarsta.setText(""); tfStagiu.setText(""); cbSex.setSelectedIndex(0); cbGrad.setSelectedIndex(0);
        taOutput.setText(""); tfNume.requestFocusInWindow();
    }

    private void adaugaRand(JPanel panel, int rand, String eticheta, JComponent camp) {
        TemaUI.configureazaCamp(camp);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = rand; c.insets = new Insets(7, 7, 7, 12); c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; panel.add(new JLabel(eticheta + ":"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; panel.add(camp, c);
    }
}
