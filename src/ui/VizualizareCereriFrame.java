package ui;

import model.Cerere;
import model.StatusCerere;
import model.TipPensie;
import service.CerereService;
import service.Validari;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.time.LocalDate;

public class VizualizareCereriFrame extends JFrame {
    private static final String[] COLOANE = {
            "ID", "Nume", "Prenume", "Adresă", "Tip pensie", "Vârstă", "Stagiu",
            "Sex", "Grad", "Nr. urmași", "Cupon", "Nr. înregistrare",
            "Status", "Valoare pensie", "Data plății", "Nr. decizie", "Motiv respingere"
    };
    private final CerereService service = new CerereService();
    private final DefaultTableModel model = new DefaultTableModel(COLOANE, 0) {
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabela = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sortare = new TableRowSorter<>(model);
    private final JTextField tfFiltru = new JTextField(24);
    private final JComboBox<Object> cbStatus = new JComboBox<>();
    private final JComboBox<Object> cbTip = new JComboBox<>();
    private final JTextField tfPensieMin = new JTextField(7);
    private final JTextField tfPensieMax = new JTextField(7);
    private final JTextField tfDataDeLa = new JTextField(9);
    private final JTextField tfDataPanaLa = new JTextField(9);
    private final JLabel lblTotal = new JLabel("0 cereri");
    private final JButton btnModifica = TemaUI.butonPrincipal("Modifică cererea");

    public VizualizareCereriFrame() {
        setTitle("Registrul cererilor — E-Pensii");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 600));
        setLayout(new BorderLayout());
        add(TemaUI.antet("Registrul cererilor",
                "Caută, copiază și corectează datele. Statusul este controlat de fluxul cererii."), BorderLayout.NORTH);

        JPanel continut = new JPanel(new BorderLayout(0, 12));
        continut.setBorder(new EmptyBorder(18, 22, 20, 22));
        JPanel unelte = TemaUI.card();
        unelte.setLayout(new GridLayout(2, 1, 0, 8));
        JPanel randCautare = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JPanel randFiltre = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        randCautare.setOpaque(false);
        randFiltre.setOpaque(false);
        for (JComponent camp : new JComponent[]{tfFiltru, cbStatus, cbTip, tfPensieMin,
                tfPensieMax, tfDataDeLa, tfDataPanaLa}) TemaUI.configureazaCamp(camp);
        cbStatus.addItem("Toate statusurile");
        for (StatusCerere status : StatusCerere.values()) cbStatus.addItem(status);
        cbTip.addItem("Toate tipurile");
        for (TipPensie tip : TipPensie.values()) cbTip.addItem(tip);
        btnModifica.setEnabled(false);
        btnModifica.setToolTipText("Dosarele calculate sau puse în plată sunt protejate");
        JButton btnRefresh = TemaUI.butonPrincipal("Reîmprospătează");
        JButton btnInchide = TemaUI.butonSecundar("Închide");
        JButton btnStergeFiltre = TemaUI.butonSecundar("Șterge filtrele");
        randCautare.add(new JLabel("Căutare:"));
        randCautare.add(tfFiltru);
        randCautare.add(btnModifica);
        randCautare.add(btnRefresh);
        randCautare.add(btnInchide);
        randCautare.add(lblTotal);
        randFiltre.add(new JLabel("Status:")); randFiltre.add(cbStatus);
        randFiltre.add(new JLabel("Tip:")); randFiltre.add(cbTip);
        randFiltre.add(new JLabel("Pensie min/max:")); randFiltre.add(tfPensieMin); randFiltre.add(tfPensieMax);
        randFiltre.add(new JLabel("Plată de la/până la:")); randFiltre.add(tfDataDeLa); randFiltre.add(tfDataPanaLa);
        randFiltre.add(btnStergeFiltre);
        unelte.add(randCautare);
        unelte.add(randFiltre);
        continut.add(unelte, BorderLayout.NORTH);

        configureazaTabel();
        continut.add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(continut, BorderLayout.CENTER);
        btnRefresh.addActionListener(e -> incarcaDate());
        btnModifica.addActionListener(e -> modificaCerereaSelectata());
        btnInchide.addActionListener(e -> dispose());
        DocumentListener ascultatorFiltre = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicaFiltru(); }
            public void removeUpdate(DocumentEvent e) { aplicaFiltru(); }
            public void changedUpdate(DocumentEvent e) { aplicaFiltru(); }
        };
        for (JTextField camp : new JTextField[]{tfFiltru, tfPensieMin, tfPensieMax, tfDataDeLa, tfDataPanaLa})
            camp.getDocument().addDocumentListener(ascultatorFiltre);
        cbStatus.addActionListener(e -> aplicaFiltru());
        cbTip.addActionListener(e -> aplicaFiltru());
        btnStergeFiltre.addActionListener(e -> stergeFiltre());
        pack();
        setLocationRelativeTo(null);
        incarcaDate();
    }

    private void configureazaTabel() {
        tabela.setRowSorter(sortare);
        tabela.setRowHeight(27);
        tabela.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tabela.setCellSelectionEnabled(true);
        tabela.setRowSelectionAllowed(true);
        tabela.setColumnSelectionAllowed(true);
        tabela.setToolTipText("Selectează celule și apasă Ctrl+C pentru copiere");
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(TemaUI.CONTUR);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(55);
        for (int i = 1; i < tabela.getColumnCount(); i++) tabela.getColumnModel().getColumn(i).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(190);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(11).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(15).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(16).setPreferredWidth(260);
        tabela.getColumnModel().getColumn(12).setCellRenderer(new RendererStatus());
        tabela.getSelectionModel().addListSelectionListener(e -> actualizeazaButonModificare());
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) modificaCerereaSelectata();
            }
            public void mousePressed(java.awt.event.MouseEvent e) { afiseazaMeniuCopiere(e); }
            public void mouseReleased(java.awt.event.MouseEvent e) { afiseazaMeniuCopiere(e); }
        });
        tabela.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control C"), "copiazaCelule");
        tabela.getActionMap().put("copiazaCelule", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { copiazaSelectia(); }
        });
    }

    private void incarcaDate() {
        model.setRowCount(0);
        try {
            for (Cerere cerere : service.gasesteToate()) {
                model.addRow(new Object[]{
                        cerere.getId(), cerere.getNume(), cerere.getPrenume(), cerere.getAdresa(),
                        cerere.getTipPensie(), cerere.getVarsta(), cerere.getStagiu(), cerere.getSex(),
                        cerere.getGradInvaliditate(), cerere.getNrUrmasi(), cerere.getCupon(),
                        cerere.getNumarInregistrare(), cerere.getStatus(), cerere.getValoarePensie(),
                        cerere.getDataPlata(), cerere.getNumarDecizie(), cerere.getMotivRespingere()
                });
            }
            actualizeazaTotal();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Eroare PostgreSQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificaCerereaSelectata() {
        int randVizibil = tabela.getSelectedRow();
        if (randVizibil < 0 || tabela.getSelectedRowCount() != 1) {
            JOptionPane.showMessageDialog(this, "Selectează un singur rând pentru modificare.",
                    "Modificare cerere", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int rand = tabela.convertRowIndexToModel(randVizibil);
        StatusCerere status = (StatusCerere) model.getValueAt(rand, 12);
        if (status == StatusCerere.PENSIE_CALCULATA || status == StatusCerere.IN_PLATA) {
            JOptionPane.showMessageDialog(this,
                    "Dosarele calculate sau puse în plată sunt protejate împotriva modificării.",
                    "Dosar protejat", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = ((Number) model.getValueAt(rand, 0)).intValue();
        JTextField tfNume = camp(model.getValueAt(rand, 1));
        JTextField tfPrenume = camp(model.getValueAt(rand, 2));
        JTextField tfAdresa = camp(model.getValueAt(rand, 3));
        JComboBox<TipPensie> cbTip = new JComboBox<>(TipPensie.values());
        cbTip.setSelectedItem(model.getValueAt(rand, 4));
        JSpinner spVarsta = new JSpinner(new SpinnerNumberModel(numar(model.getValueAt(rand, 5)), 0, 120, 1));
        JSpinner spStagiu = new JSpinner(new SpinnerNumberModel(numar(model.getValueAt(rand, 6)), 0, 60, 1));
        JComboBox<String> cbSex = new JComboBox<>(new String[]{"M", "F", "N/A"});
        cbSex.setSelectedItem(model.getValueAt(rand, 7));
        JTextField tfGrad = camp(model.getValueAt(rand, 8));
        JTextField tfNrUrmasi = camp(model.getValueAt(rand, 9));
        JTextField tfCupon = camp(model.getValueAt(rand, 10));
        JTextField tfNrInregistrare = camp(model.getValueAt(rand, 11));

        JPanel formular = new JPanel(new GridBagLayout());
        formular.setBorder(new EmptyBorder(8, 12, 8, 12));
        adaugaCampEditare(formular, 0, "ID (nemodificabil)", new JLabel(String.valueOf(id)));
        adaugaCampEditare(formular, 1, "Status (gestionat automat)", new JLabel(status.getEticheta()));
        adaugaCampEditare(formular, 2, "Nume", tfNume);
        adaugaCampEditare(formular, 3, "Prenume", tfPrenume);
        adaugaCampEditare(formular, 4, "Adresă", tfAdresa);
        adaugaCampEditare(formular, 5, "Tip pensie", cbTip);
        adaugaCampEditare(formular, 6, "Vârstă", spVarsta);
        adaugaCampEditare(formular, 7, "Stagiu", spStagiu);
        adaugaCampEditare(formular, 8, "Sex", cbSex);
        adaugaCampEditare(formular, 9, "Grad invaliditate (opțional)", tfGrad);
        adaugaCampEditare(formular, 10, "Număr urmași (opțional)", tfNrUrmasi);
        adaugaCampEditare(formular, 11, "Cupon (opțional)", tfCupon);
        adaugaCampEditare(formular, 12, "Număr înregistrare", tfNrInregistrare);
        JScrollPane scroll = new JScrollPane(formular);
        scroll.setPreferredSize(new Dimension(610, 510));

        while (true) {
            int rezultat = JOptionPane.showConfirmDialog(this, scroll, "Modifică cererea #" + id,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (rezultat != JOptionPane.OK_OPTION) return;
            try {
                Cerere actualizata = new Cerere(id,
                        Validari.nume(tfNume.getText(), "Numele"),
                        Validari.nume(tfPrenume.getText(), "Prenumele"),
                        Validari.textObligatoriu(tfAdresa.getText(), "Adresa"),
                        (TipPensie) cbTip.getSelectedItem(), (Integer) spVarsta.getValue(),
                        (Integer) spStagiu.getValue(), (String) cbSex.getSelectedItem(),
                        intregOptional(tfGrad.getText(), "Gradul de invaliditate", 1, 3),
                        intregOptional(tfNrUrmasi.getText(), "Numărul de urmași", 1, 5),
                        realOptional(tfCupon.getText(), "Cuponul"),
                        Validari.textObligatoriu(tfNrInregistrare.getText(), "Numărul de înregistrare"),
                        status, (Double) model.getValueAt(rand, 13),
                        (java.time.LocalDate) model.getValueAt(rand, 14), (String) model.getValueAt(rand, 15));
                Cerere salvata = service.actualizeazaDate(actualizata);
                incarcaDate();
                selecteazaDupaId(id);
                String mesaj = "Cererea a fost actualizată.";
                if (salvata.getStatus() == StatusCerere.IN_VERIFICARE && status != StatusCerere.IN_VERIFICARE) {
                    mesaj += " Deoarece datele unui dosar procesat s-au schimbat, acesta a revenit în verificare.";
                }
                JOptionPane.showMessageDialog(this, mesaj, "Modificare cerere", JOptionPane.INFORMATION_MESSAGE);
                return;
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Date invalide", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void actualizeazaButonModificare() {
        if (tabela.getSelectedRowCount() != 1) {
            btnModifica.setEnabled(false);
            return;
        }
        int rand = tabela.convertRowIndexToModel(tabela.getSelectedRow());
        StatusCerere status = (StatusCerere) model.getValueAt(rand, 12);
        btnModifica.setEnabled(status != StatusCerere.PENSIE_CALCULATA && status != StatusCerere.IN_PLATA);
    }

    private JTextField camp(Object valoare) {
        JTextField camp = new JTextField(valoare == null ? "" : String.valueOf(valoare), 24);
        TemaUI.configureazaCamp(camp);
        return camp;
    }

    private int numar(Object valoare) { return valoare instanceof Number ? ((Number) valoare).intValue() : 0; }

    private Integer intregOptional(String valoare, String denumire, int minim, int maxim) {
        String text = valoare.trim();
        return text.isEmpty() ? null : Validari.numarIntreg(text, denumire, minim, maxim);
    }

    private Double realOptional(String valoare, String denumire) {
        String text = valoare.trim();
        return text.isEmpty() ? null : Validari.numarReal(text, denumire, 0, 1_000_000_000);
    }

    private void adaugaCampEditare(JPanel formular, int rand, String eticheta, JComponent camp) {
        TemaUI.configureazaCamp(camp);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = rand; c.insets = new Insets(5, 6, 5, 10); c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; formular.add(new JLabel(eticheta + ":"), c);
        c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; formular.add(camp, c);
    }

    private void selecteazaDupaId(int id) {
        for (int rand = 0; rand < model.getRowCount(); rand++) {
            if (((Number) model.getValueAt(rand, 0)).intValue() == id) {
                int vizibil = tabela.convertRowIndexToView(rand);
                if (vizibil >= 0) tabela.changeSelection(vizibil, 0, false, false);
                return;
            }
        }
    }

    private void aplicaFiltru() {
        String text = tfFiltru.getText().trim().toLowerCase();
        Object statusSelectat = cbStatus.getSelectedItem();
        Object tipSelectat = cbTip.getSelectedItem();
        Double pensieMin = citesteDoubleFiltru(tfPensieMin);
        Double pensieMax = citesteDoubleFiltru(tfPensieMax);
        LocalDate dataDeLa = citesteDataFiltru(tfDataDeLa);
        LocalDate dataPanaLa = citesteDataFiltru(tfDataPanaLa);
        sortare.setRowFilter(new RowFilter<>() {
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                if (!text.isEmpty()) {
                    boolean gasit = false;
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        Object valoare = entry.getValue(i);
                        if (valoare != null && String.valueOf(valoare).toLowerCase().contains(text)) {
                            gasit = true; break;
                        }
                    }
                    if (!gasit) return false;
                }
                if (statusSelectat instanceof StatusCerere && entry.getValue(12) != statusSelectat) return false;
                if (tipSelectat instanceof TipPensie && entry.getValue(4) != tipSelectat) return false;
                Object pensie = entry.getValue(13);
                if (pensieMin != null && (!(pensie instanceof Number) || ((Number) pensie).doubleValue() < pensieMin)) return false;
                if (pensieMax != null && (!(pensie instanceof Number) || ((Number) pensie).doubleValue() > pensieMax)) return false;
                Object data = entry.getValue(14);
                if (dataDeLa != null && (!(data instanceof LocalDate) || ((LocalDate) data).isBefore(dataDeLa))) return false;
                return dataPanaLa == null || (data instanceof LocalDate && !((LocalDate) data).isAfter(dataPanaLa));
            }
        });
        actualizeazaTotal();
    }

    private Double citesteDoubleFiltru(JTextField camp) {
        String text = camp.getText().trim().replace(',', '.');
        if (text.isEmpty()) { camp.setBackground(Color.WHITE); return null; }
        try { camp.setBackground(Color.WHITE); return Double.parseDouble(text); }
        catch (NumberFormatException ex) { camp.setBackground(new Color(255, 232, 232)); return null; }
    }

    private LocalDate citesteDataFiltru(JTextField camp) {
        String text = camp.getText().trim();
        camp.setToolTipText("Format: AAAA-LL-ZZ");
        if (text.isEmpty()) { camp.setBackground(Color.WHITE); return null; }
        try { camp.setBackground(Color.WHITE); return LocalDate.parse(text); }
        catch (java.time.format.DateTimeParseException ex) {
            camp.setBackground(new Color(255, 232, 232)); return null;
        }
    }

    private void stergeFiltre() {
        tfFiltru.setText(""); tfPensieMin.setText(""); tfPensieMax.setText("");
        tfDataDeLa.setText(""); tfDataPanaLa.setText("");
        cbStatus.setSelectedIndex(0); cbTip.setSelectedIndex(0);
        aplicaFiltru();
    }

    private void actualizeazaTotal() {
        int vizibile = tabela.getRowCount();
        lblTotal.setText(vizibile + (vizibile == 1 ? " cerere" : " cereri") +
                (vizibile != model.getRowCount() ? " din " + model.getRowCount() : ""));
        lblTotal.setForeground(TemaUI.TEXT_SECUNDAR);
    }

    private void afiseazaMeniuCopiere(java.awt.event.MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int rand = tabela.rowAtPoint(e.getPoint());
        int coloana = tabela.columnAtPoint(e.getPoint());
        if (rand < 0 || coloana < 0) return;
        if (!tabela.isCellSelected(rand, coloana)) tabela.changeSelection(rand, coloana, false, false);
        JPopupMenu meniu = new JPopupMenu();
        JMenuItem copiaza = new JMenuItem("Copiază valoarea");
        copiaza.addActionListener(event -> copiazaSelectia());
        meniu.add(copiaza);
        meniu.show(tabela, e.getX(), e.getY());
    }

    private void copiazaSelectia() {
        int[] randuri = tabela.getSelectedRows();
        int[] coloane = tabela.getSelectedColumns();
        if (randuri.length == 0 || coloane.length == 0) return;
        StringBuilder text = new StringBuilder();
        for (int r = 0; r < randuri.length; r++) {
            if (r > 0) text.append(System.lineSeparator());
            for (int c = 0; c < coloane.length; c++) {
                if (c > 0) text.append('\t');
                Object valoare = tabela.getValueAt(randuri[r], coloane[c]);
                if (valoare != null) text.append(valoare);
            }
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text.toString()), null);
    }

    private static class RendererStatus extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            Component componenta = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            if (!selected) {
                StatusCerere status = value instanceof StatusCerere ? (StatusCerere) value : StatusCerere.DEPUSA;
                componenta.setForeground(switch (status) {
                    case ADMISA, PENSIE_CALCULATA, IN_PLATA -> TemaUI.SUCCES;
                    case RESPINSA -> TemaUI.EROARE;
                    default -> TemaUI.AVERTISMENT;
                });
                componenta.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 253));
            }
            setFont(getFont().deriveFont(Font.BOLD));
            return componenta;
        }
    }
}
