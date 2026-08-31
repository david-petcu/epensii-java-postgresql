package ui;

import model.Cerere;
import model.StatusCerere;
import model.TipPensie;
import service.CerereService;
import service.Validari;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ValidareDosarPanel extends JPanel {
    private final CerereService service = new CerereService();
    private final JTextField tfNrInregistrare = new JTextField(22);
    private final JComboBox<String> cbSex = new JComboBox<>(new String[]{"M", "F"});
    private final JSpinner spVarsta = new JSpinner(new SpinnerNumberModel(0, 0, 120, 1));
    private final JSpinner spStagiu = new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));
    private final JComboBox<Integer> cbGrad = new JComboBox<>(new Integer[]{1, 2, 3});
    private final JLabel lblSolicitant = new JLabel("—");
    private final JLabel lblTip = new JLabel("—");
    private final JLabel lblStatus = new JLabel("Nicio cerere încărcată");
    private final JPanel panelDetalii = TemaUI.card();
    private final JLabel lblSex = new JLabel("Sex:");
    private final JLabel lblVarsta = new JLabel("Vârstă:");
    private final JLabel lblStagiu = new JLabel("Stagiu de cotizare:");
    private final JLabel lblGrad = new JLabel("Grad de invaliditate:");
    private final JButton btnActiune = TemaUI.butonPrincipal("Începe verificarea");
    private Cerere cerereCurenta;

    public ValidareDosarPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(720, 600));
        add(TemaUI.antet("Validare dosare",
                "Flux: Depusă → În verificare → Admisă sau Respinsă."), BorderLayout.NORTH);
        JPanel continut = new JPanel(new BorderLayout(0, 16));
        continut.setBorder(new EmptyBorder(20, 26, 20, 26));

        JPanel cautare = TemaUI.card();
        cautare.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        TemaUI.configureazaCamp(tfNrInregistrare);
        JButton btnCauta = TemaUI.butonPrincipal("Caută");
        cautare.add(new JLabel("Număr de înregistrare:"));
        cautare.add(tfNrInregistrare);
        cautare.add(btnCauta);
        continut.add(cautare, BorderLayout.NORTH);

        panelDetalii.setLayout(new GridBagLayout());
        adaugaRand(0, new JLabel("Solicitant:"), lblSolicitant);
        adaugaRand(1, new JLabel("Tip pensie:"), lblTip);
        adaugaRand(2, lblSex, cbSex);
        adaugaRand(3, lblVarsta, spVarsta);
        adaugaRand(4, lblStagiu, spStagiu);
        adaugaRand(5, lblGrad, cbGrad);
        adaugaRand(6, new JLabel("Status curent:"), lblStatus);
        panelDetalii.setVisible(false);
        continut.add(panelDetalii, BorderLayout.CENTER);

        JButton btnInchide = TemaUI.butonSecundar("Închide");
        btnInchide.addActionListener(e -> TemaUI.inchideFereastra(this));
        btnActiune.addActionListener(e -> executaActiune());
        btnCauta.addActionListener(e -> cautaCerere());
        tfNrInregistrare.addActionListener(e -> cautaCerere());
        JPanel butoane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        butoane.add(btnInchide);
        butoane.add(btnActiune);
        continut.add(butoane, BorderLayout.SOUTH);
        add(continut, BorderLayout.CENTER);
    }

    private void cautaCerere() {
        try {
            String nr = Validari.textObligatoriu(tfNrInregistrare.getText(), "Numărul de înregistrare");
            cerereCurenta = service.gasesteDupaNumar(nr).orElse(null);
            if (cerereCurenta == null) {
                panelDetalii.setVisible(false);
                mesaj("Nu există nicio cerere cu acest număr de înregistrare.", false);
                return;
            }
            afiseazaCerere();
        } catch (RuntimeException ex) {
            panelDetalii.setVisible(false);
            mesaj(ex.getMessage(), false);
        }
    }

    private void afiseazaCerere() {
        lblSolicitant.setText(cerereCurenta.getNume() + " " + cerereCurenta.getPrenume());
        lblTip.setText(cerereCurenta.getTipPensie().getEticheta());
        cbSex.setSelectedItem(cerereCurenta.getSex());
        spVarsta.setValue(cerereCurenta.getVarsta());
        spStagiu.setValue(cerereCurenta.getStagiu());
        if (cerereCurenta.getGradInvaliditate() != null) {
            cbGrad.setSelectedItem(cerereCurenta.getGradInvaliditate());
        }
        boolean urmas = cerereCurenta.getTipPensie() == TipPensie.URMAS;
        boolean invaliditate = cerereCurenta.getTipPensie() == TipPensie.INVALIDITATE;
        lblSex.setVisible(!urmas); cbSex.setVisible(!urmas);
        lblVarsta.setVisible(!urmas); spVarsta.setVisible(!urmas);
        lblStagiu.setVisible(!urmas); spStagiu.setVisible(!urmas);
        lblGrad.setVisible(invaliditate); cbGrad.setVisible(invaliditate);
        actualizeazaStatus(cerereCurenta.getStatus());
        configureazaActiune();
        panelDetalii.setVisible(true);
        revalidate(); repaint();
    }

    private void configureazaActiune() {
        if (cerereCurenta.getStatus() == StatusCerere.DEPUSA) {
            btnActiune.setText("Începe verificarea");
            btnActiune.setEnabled(true);
        } else if (cerereCurenta.getStatus() == StatusCerere.IN_VERIFICARE) {
            btnActiune.setText("Validează dosarul");
            btnActiune.setEnabled(true);
        } else {
            btnActiune.setText("Dosar procesat");
            btnActiune.setEnabled(false);
        }
    }

    private void executaActiune() {
        if (cerereCurenta == null) return;
        try {
            if (cerereCurenta.getStatus() == StatusCerere.DEPUSA) {
                cerereCurenta = service.incepeVerificarea(cerereCurenta.getId());
                afiseazaCerere();
                mesaj("Cererea a intrat în verificare. Confirmă datele și validează dosarul.", true);
                return;
            }
            if (cerereCurenta.getStatus() == StatusCerere.IN_VERIFICARE) {
                salveazaDateleEditate();
                String motiv = null;
                if (!service.esteEligibila(cerereCurenta)) {
                    JTextArea campMotiv = new JTextArea(5, 38);
                    campMotiv.setLineWrap(true);
                    campMotiv.setWrapStyleWord(true);
                    int alegere = JOptionPane.showConfirmDialog(this, new JScrollPane(campMotiv),
                            "Motivul respingerii (obligatoriu)", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (alegere != JOptionPane.OK_OPTION) return;
                    motiv = Validari.textObligatoriu(campMotiv.getText(), "Motivul respingerii");
                }
                cerereCurenta = service.valideaza(cerereCurenta.getId(), motiv);
                afiseazaCerere();
                mesaj("Validarea s-a încheiat. Status nou: " + cerereCurenta.getStatus().getEticheta(), true);
            }
        } catch (RuntimeException ex) {
            mesaj(ex.getMessage(), false);
        }
    }

    private void salveazaDateleEditate() {
        if (cerereCurenta.getTipPensie() == TipPensie.URMAS) return;
        Integer grad = cerereCurenta.getTipPensie() == TipPensie.INVALIDITATE
                ? (Integer) cbGrad.getSelectedItem() : null;
        Cerere actualizata = new Cerere(cerereCurenta.getId(), cerereCurenta.getNume(),
                cerereCurenta.getPrenume(), cerereCurenta.getAdresa(), cerereCurenta.getTipPensie(),
                (Integer) spVarsta.getValue(), (Integer) spStagiu.getValue(),
                (String) cbSex.getSelectedItem(), grad, cerereCurenta.getNrUrmasi(),
                cerereCurenta.getCupon(), cerereCurenta.getNumarInregistrare(), cerereCurenta.getStatus(),
                cerereCurenta.getValoarePensie(), cerereCurenta.getDataPlata(), cerereCurenta.getNumarDecizie());
        cerereCurenta = service.actualizeazaDate(actualizata);
    }

    private void actualizeazaStatus(StatusCerere status) {
        lblStatus.setText(status.getEticheta());
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD));
        Color culoare = switch (status) {
            case ADMISA, PENSIE_CALCULATA, IN_PLATA -> TemaUI.SUCCES;
            case RESPINSA -> TemaUI.EROARE;
            default -> TemaUI.AVERTISMENT;
        };
        lblStatus.setForeground(culoare);
    }

    private void mesaj(String text, boolean succes) {
        JOptionPane.showMessageDialog(this, text, succes ? "E-Pensii" : "Atenție",
                succes ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private void adaugaRand(int rand, JComponent eticheta, JComponent valoare) {
        TemaUI.configureazaCamp(valoare);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = rand; c.insets = new Insets(8, 7, 8, 12); c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; panelDetalii.add(eticheta, c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; panelDetalii.add(valoare, c);
    }
}
