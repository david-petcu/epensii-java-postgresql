package ui;

import model.StatisticiDashboard;
import model.StatusCerere;
import service.CerereService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EPensiiApp extends JFrame {
    private final CerereService service = new CerereService();
    private final JLabel lblConexiune = new JLabel("Se verifică baza de date...");
    private final Map<StatusCerere, JLabel> valoriStatus = new EnumMap<>(StatusCerere.class);
    private final Map<String, JFrame> ferestreDeschise = new HashMap<>();
    private final JLabel lblTotalPlata = new JLabel("—");

    public EPensiiApp() {
        setTitle("E-Pensii — Administrare cereri");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 760));
        setLayout(new BorderLayout());
        add(TemaUI.antet("E-Pensii", "Gestionarea clară a cererilor și deciziilor de pensionare"), BorderLayout.NORTH);

        JPanel continut = new JPanel(new BorderLayout(0, 18));
        continut.setBorder(new EmptyBorder(24, 28, 20, 28));
        JLabel instructiune = new JLabel("Situația cererilor");
        instructiune.setFont(new Font("Segoe UI", Font.BOLD, 18));
        instructiune.setForeground(TemaUI.TEXT);
        JPanel zonaSus = new JPanel(new BorderLayout(0, 10));
        zonaSus.setOpaque(false);
        zonaSus.add(instructiune, BorderLayout.NORTH);
        zonaSus.add(creeazaDashboard(), BorderLayout.CENTER);
        continut.add(zonaSus, BorderLayout.NORTH);

        JPanel actiuni = new JPanel(new GridLayout(3, 2, 14, 14));
        actiuni.add(cardActiune("Pensie pentru limită de vârstă",
                "Înregistrează o cerere și verifică eligibilitatea inițială.", this::openPensieVarstaPanel));
        actiuni.add(cardActiune("Pensie de invaliditate",
                "Înregistrează stagiul și gradul de invaliditate.", this::openPensieInvaliditatePanel));
        actiuni.add(cardActiune("Pensie de urmaș",
                "Calculează automat valoarea în funcție de numărul urmașilor.", this::openPensieUrmasPanel));
        actiuni.add(cardActiune("Validare dosare",
                "Caută, verifică și aprobă sau respinge un dosar.", this::openValidareDosarPanel));
        actiuni.add(cardActiune("Stabilire pensie",
                "Introdu salariile și calculează pensia pentru dosarele admise.", this::openStabilirePensieFrame));
        actiuni.add(cardActiune("Registrul cererilor",
                "Vizualizează, filtrează și actualizează lista completă.", this::openVizualizareCereri));
        continut.add(actiuni, BorderLayout.CENTER);

        lblConexiune.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblConexiune.setForeground(TemaUI.TEXT_SECUNDAR);
        lblConexiune.setBorder(new EmptyBorder(6, 2, 0, 0));
        continut.add(lblConexiune, BorderLayout.SOUTH);
        add(continut, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        verificaConexiunea();
        actualizeazaDashboard();
        new Timer(15_000, e -> actualizeazaDashboard()).start();
    }

    private JPanel creeazaDashboard() {
        JPanel dashboard = new JPanel(new GridLayout(2, 4, 8, 8));
        dashboard.setOpaque(false);
        for (StatusCerere status : StatusCerere.values()) {
            JLabel valoare = new JLabel("—");
            valoriStatus.put(status, valoare);
            dashboard.add(cardIndicator(status.getEticheta(), valoare));
        }
        dashboard.add(cardIndicator("Total pensii în plată", lblTotalPlata));
        JButton refresh = TemaUI.butonSecundar("Actualizează situația");
        refresh.addActionListener(e -> actualizeazaDashboard());
        JPanel cardRefresh = TemaUI.card();
        cardRefresh.setLayout(new GridBagLayout());
        cardRefresh.add(refresh);
        dashboard.add(cardRefresh);
        return dashboard;
    }

    private JPanel cardIndicator(String titlu, JLabel valoare) {
        JPanel card = TemaUI.card();
        card.setLayout(new BorderLayout(0, 3));
        JLabel eticheta = new JLabel(titlu);
        eticheta.setForeground(TemaUI.TEXT_SECUNDAR);
        valoare.setFont(new Font("Segoe UI", Font.BOLD, 21));
        valoare.setForeground(TemaUI.PRIMAR_INCHIS);
        card.add(eticheta, BorderLayout.NORTH);
        card.add(valoare, BorderLayout.CENTER);
        return card;
    }

    private void actualizeazaDashboard() {
        new SwingWorker<StatisticiDashboard, Void>() {
            protected StatisticiDashboard doInBackground() { return service.obtineStatisticiDashboard(); }
            protected void done() {
                try {
                    StatisticiDashboard statistici = get();
                    valoriStatus.forEach((status, label) ->
                            label.setText(String.valueOf(statistici.numar(status))));
                    lblTotalPlata.setText(String.format("%.2f RON", statistici.totalPensiiInPlata()));
                } catch (Exception ex) {
                    valoriStatus.values().forEach(label -> label.setText("!"));
                    lblTotalPlata.setText("Indisponibil");
                }
            }
        }.execute();
    }

    private JPanel cardActiune(String titlu, String descriere, Runnable actiune) {
        JPanel card = TemaUI.card();
        card.setLayout(new BorderLayout(0, 8));
        JLabel lblTitlu = new JLabel(titlu);
        lblTitlu.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitlu.setForeground(TemaUI.PRIMAR_INCHIS);
        JTextArea lblDescriere = new JTextArea(descriere);
        lblDescriere.setEditable(false);
        lblDescriere.setFocusable(false);
        lblDescriere.setOpaque(false);
        lblDescriere.setLineWrap(true);
        lblDescriere.setWrapStyleWord(true);
        lblDescriere.setForeground(TemaUI.TEXT_SECUNDAR);
        lblDescriere.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JButton buton = TemaUI.butonPrincipal("Deschide");
        buton.addActionListener(e -> actiune.run());
        JPanel randButon = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        randButon.setOpaque(false);
        randButon.add(buton);
        card.add(lblTitlu, BorderLayout.NORTH);
        card.add(lblDescriere, BorderLayout.CENTER);
        card.add(randButon, BorderLayout.SOUTH);
        return card;
    }

    private void verificaConexiunea() {
        new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() {
                return service.conexiuneDisponibila();
            }
            protected void done() {
                try {
                    boolean conectat = get();
                    lblConexiune.setText(conectat
                            ? "● PostgreSQL conectat — baza epensii este disponibilă"
                            : "● PostgreSQL indisponibil — verifică serviciul și configurația");
                    lblConexiune.setForeground(conectat ? TemaUI.SUCCES : TemaUI.EROARE);
                } catch (Exception ignored) {
                    lblConexiune.setText("● Nu s-a putut verifica baza de date");
                    lblConexiune.setForeground(TemaUI.EROARE);
                }
            }
        }.execute();
    }

    private JFrame fereastraPanel(String titlu, JPanel panel) {
        JFrame frame = new JFrame(titlu);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(760, 650));
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(this);
        return frame;
    }

    private void deschideFereastraUnica(String cheie, Supplier<JFrame> constructor) {
        JFrame existenta = ferestreDeschise.get(cheie);
        if (existenta != null && existenta.isDisplayable()) {
            existenta.setExtendedState(existenta.getExtendedState() & ~Frame.ICONIFIED);
            existenta.setVisible(true);
            existenta.toFront();
            existenta.requestFocus();
            return;
        }

        JFrame fereastra = constructor.get();
        ferestreDeschise.put(cheie, fereastra);
        fereastra.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                ferestreDeschise.remove(cheie, fereastra);
            }
        });
        fereastra.setVisible(true);
        fereastra.toFront();
    }

    private void openPensieVarstaPanel() {
        deschideFereastraUnica("pensie-varsta",
                () -> fereastraPanel("Pensie pentru limită de vârstă", new PensieVarstaPanel()));
    }

    private void openPensieInvaliditatePanel() {
        deschideFereastraUnica("pensie-invaliditate",
                () -> fereastraPanel("Pensie de invaliditate", new PensieInvaliditatePanel()));
    }

    private void openPensieUrmasPanel() {
        deschideFereastraUnica("pensie-urmas",
                () -> fereastraPanel("Pensie de urmaș", new PensieUrmasPanel()));
    }

    private void openValidareDosarPanel() {
        deschideFereastraUnica("validare-dosare",
                () -> fereastraPanel("Validare dosare", new ValidareDosarPanel()));
    }

    private void openVizualizareCereri() {
        deschideFereastraUnica("registru-cereri", VizualizareCereriFrame::new);
    }

    private void openStabilirePensieFrame() {
        deschideFereastraUnica("stabilire-pensie", StabilirePensieFrame::new);
    }
}
