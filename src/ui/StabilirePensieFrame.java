package ui;

import model.Cerere;
import model.Salariu;
import model.StatusCerere;
import model.TipPensie;
import service.CalculatorPensieService;
import service.CerereService;
import service.Validari;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StabilirePensieFrame extends JFrame {
    private final CerereService service = new CerereService();
    private final JTextField tfNrInregistrare = new JTextField(22);
    private final JTextArea taPensionarInfo = new JTextArea(7, 50);
    private final JTextArea taOutput = new JTextArea(7, 50);
    private final JButton btnCalcul = TemaUI.butonPrincipal("Gestionează salariile");
    private final JButton btnPlata = TemaUI.butonPrincipal("Pune în plată");
    private Cerere cerereCurenta;

    public StabilirePensieFrame() {
        setTitle("Stabilire pensie — Referent");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(780, 680));
        setLayout(new BorderLayout());
        add(TemaUI.antet("Stabilire pensie",
                "Flux: Admisă → Pensie calculată → În plată. Salariile sunt păstrate în PostgreSQL."), BorderLayout.NORTH);

        JPanel continut = new JPanel(new BorderLayout(0, 14));
        continut.setBorder(new EmptyBorder(20, 26, 20, 26));
        JPanel cautare = TemaUI.card();
        cautare.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        TemaUI.configureazaCamp(tfNrInregistrare);
        JButton btnCauta = TemaUI.butonPrincipal("Caută cererea");
        cautare.add(new JLabel("Număr de înregistrare:"));
        cautare.add(tfNrInregistrare);
        cautare.add(btnCauta);
        continut.add(cautare, BorderLayout.NORTH);

        TemaUI.configureazaOutput(taPensionarInfo);
        TemaUI.configureazaOutput(taOutput);
        JPanel informatii = new JPanel(new GridLayout(2, 1, 0, 12));
        informatii.add(cardCuTitlu("Datele solicitantului", taPensionarInfo));
        informatii.add(cardCuTitlu("Rezultatul operației", taOutput));
        continut.add(informatii, BorderLayout.CENTER);

        btnCalcul.setEnabled(false);
        btnPlata.setEnabled(false);
        JButton btnInchide = TemaUI.butonSecundar("Închide");
        btnInchide.addActionListener(e -> dispose());
        btnCauta.addActionListener(e -> cautaCerere());
        tfNrInregistrare.addActionListener(e -> cautaCerere());
        btnCalcul.addActionListener(e -> gestioneazaCalcul());
        btnPlata.addActionListener(e -> puneInPlata());
        JPanel butoane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        butoane.add(btnInchide);
        butoane.add(btnPlata);
        butoane.add(btnCalcul);
        continut.add(butoane, BorderLayout.SOUTH);
        add(continut, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel cardCuTitlu(String titlu, JTextArea continut) {
        JPanel card = TemaUI.card();
        card.setLayout(new BorderLayout(0, 8));
        JLabel eticheta = new JLabel(titlu);
        eticheta.setFont(new Font("Segoe UI", Font.BOLD, 15));
        eticheta.setForeground(TemaUI.PRIMAR_INCHIS);
        card.add(eticheta, BorderLayout.NORTH);
        card.add(new JScrollPane(continut), BorderLayout.CENTER);
        return card;
    }

    private void cautaCerere() {
        btnCalcul.setEnabled(false);
        btnPlata.setEnabled(false);
        taPensionarInfo.setText("");
        try {
            String nr = Validari.textObligatoriu(tfNrInregistrare.getText(), "Numărul de înregistrare");
            cerereCurenta = service.gasesteDupaNumar(nr).orElse(null);
            if (cerereCurenta == null) {
                TemaUI.mesaj(taOutput, "Nu există nicio cerere cu acest număr de înregistrare.", false);
                return;
            }
            afiseazaCerere();
        } catch (RuntimeException ex) {
            TemaUI.mesaj(taOutput, ex.getMessage(), false);
        }
    }

    private void afiseazaCerere() {
        StringBuilder info = new StringBuilder();
        info.append("Solicitant: ").append(cerereCurenta.getNume()).append(' ')
                .append(cerereCurenta.getPrenume()).append('\n');
        info.append("Tip pensie: ").append(cerereCurenta.getTipPensie().getEticheta()).append('\n');
        if (cerereCurenta.getTipPensie() != TipPensie.URMAS) {
            info.append("Vârstă: ").append(cerereCurenta.getVarsta()).append(" ani\n");
            info.append("Stagiu: ").append(cerereCurenta.getStagiu()).append(" ani\n");
        } else {
            info.append("Număr urmași: ").append(cerereCurenta.getNrUrmasi()).append('\n');
            info.append("Cupon: ").append(String.format("%.2f RON", cerereCurenta.getCupon())).append('\n');
        }
        info.append("Status: ").append(cerereCurenta.getStatus().getEticheta());
        if (cerereCurenta.getValoarePensie() != null) {
            info.append("\nValoare pensie: ").append(String.format("%.2f RON", cerereCurenta.getValoarePensie()));
        }
        if (cerereCurenta.getDataPlata() != null) {
            info.append("\nData plății: ").append(cerereCurenta.getDataPlata());
        }
        taPensionarInfo.setForeground(TemaUI.TEXT);
        taPensionarInfo.setText(info.toString());

        StatusCerere status = cerereCurenta.getStatus();
        boolean poateCalcula = status == StatusCerere.ADMISA || status == StatusCerere.PENSIE_CALCULATA;
        btnCalcul.setEnabled(poateCalcula);
        btnCalcul.setText(cerereCurenta.getTipPensie() == TipPensie.URMAS
                ? "Calculează pensia de urmaș" : "Gestionează salariile");
        btnPlata.setEnabled(status == StatusCerere.PENSIE_CALCULATA);
        if (status == StatusCerere.ADMISA) {
            TemaUI.mesaj(taOutput, "Dosarul este admis și poate fi calculat.", true);
        } else if (status == StatusCerere.PENSIE_CALCULATA) {
            TemaUI.mesaj(taOutput, "Pensia este calculată. Poți corecta salariile sau o poți pune în plată.", true);
        } else if (status == StatusCerere.IN_PLATA) {
            TemaUI.mesaj(taOutput, "Pensia este deja în plată.", true);
        } else {
            TemaUI.mesaj(taOutput, "Dosarul trebuie admis înainte de calcul.", false);
        }
    }

    private void gestioneazaCalcul() {
        if (cerereCurenta == null) return;
        try {
            if (cerereCurenta.getTipPensie() == TipPensie.URMAS) {
                double valoare = service.calculeazaPensieUrmas(cerereCurenta.getId());
                reincarcaCererea();
                TemaUI.mesaj(taOutput, "Pensia de urmaș a fost calculată: " +
                        String.format("%.2f RON", valoare), true);
            } else {
                deschideEditorSalarii();
            }
        } catch (RuntimeException ex) {
            TemaUI.mesaj(taOutput, ex.getMessage(), false);
        }
    }

    private void deschideEditorSalarii() {
        List<Salariu> existente = service.gasesteSalarii(cerereCurenta.getId());
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"An calendaristic", "Salariu brut lunar mediu (RON)"}, 0) {
            public boolean isCellEditable(int row, int column) { return true; }
        };
        int primulAnImplicit = LocalDate.now().getYear() - cerereCurenta.getStagiu();
        for (int i = 0; i < cerereCurenta.getStagiu(); i++) {
            int an = i < existente.size() ? existente.get(i).getAnCalendaristic() : primulAnImplicit + i;
            double valoare = i < existente.size() ? existente.get(i).getSalariuBrutMediu() : 6800.0;
            model.addRow(new Object[]{an, String.format("%.2f", valoare)});
        }
        JTable tabel = new JTable(model);
        tabel.setRowHeight(28);
        tabel.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tabel.getColumnModel().getColumn(0).setMaxWidth(140);
        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setPreferredSize(new Dimension(550, Math.min(430, 70 + cerereCurenta.getStagiu() * 28)));
        Object[] optiuni = {"Salvează și calculează", "Doar salvează", "Renunță"};
        int rezultat = JOptionPane.showOptionDialog(this, scroll, "Istoric salarii — " +
                        cerereCurenta.getStagiu() + " ani", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, optiuni, optiuni[0]);
        if (rezultat != 0 && rezultat != 1) return;

        if (tabel.isEditing() && !tabel.getCellEditor().stopCellEditing()) {
            TemaUI.mesaj(taOutput, "Finalizează editarea salariului înainte de salvare.", false);
            return;
        }

        List<Salariu> salarii = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            int an = Validari.numarIntreg(String.valueOf(model.getValueAt(i, 0)),
                    "Anul calendaristic de pe rândul " + (i + 1), 1900, LocalDate.now().getYear());
            double valoare = Validari.numarReal(String.valueOf(model.getValueAt(i, 1)),
                    "Salariul pentru anul " + an, 1, 1_000_000);
            salarii.add(new Salariu(0, cerereCurenta.getId(), an, valoare));
        }
        if (rezultat == 1) {
            service.salveazaSalarii(cerereCurenta.getId(), salarii);
            reincarcaCererea();
            TemaUI.mesaj(taOutput, "Salariile au fost salvate. Calculul anterior, dacă exista, a fost invalidat.", true);
            return;
        }
        CalculatorPensieService.RezultatCalcul calcul =
                service.salveazaSiCalculeaza(cerereCurenta.getId(), salarii);
        reincarcaCererea();
        TemaUI.mesaj(taOutput, "Salariile au fost salvate și pensia a fost recalculată.\n" +
                "Punctaj mediu: " + String.format("%.4f", calcul.punctajMediu()) + "\n" +
                "Valoare punct: " + String.format("%.2f RON", calcul.valoarePunct()) + "\n" +
                "Valoare pensie: " + String.format("%.2f RON", calcul.valoarePensie()), true);
    }

    private void puneInPlata() {
        if (cerereCurenta == null) return;
        int confirmare = JOptionPane.showConfirmDialog(this,
                "Confirmi punerea în plată a pensiei calculate?", "Punere în plată",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmare != JOptionPane.YES_OPTION) return;
        try {
            cerereCurenta = service.puneInPlata(cerereCurenta.getId());
            afiseazaCerere();
            TemaUI.mesaj(taOutput, "Pensia a fost pusă în plată la data " +
                    cerereCurenta.getDataPlata() + ".", true);
        } catch (RuntimeException ex) {
            TemaUI.mesaj(taOutput, ex.getMessage(), false);
        }
    }

    private void reincarcaCererea() {
        cerereCurenta = service.gasesteDupaNumar(cerereCurenta.getNumarInregistrare())
                .orElseThrow(() -> new IllegalStateException("Cererea nu mai există."));
        afiseazaCerere();
    }
}
