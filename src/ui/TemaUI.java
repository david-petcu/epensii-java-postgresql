package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class TemaUI {
    public static final Color FUNDAL = new Color(244, 247, 251);
    public static final Color SUPRAFATA = Color.WHITE;
    public static final Color PRIMAR = new Color(30, 86, 160);
    public static final Color PRIMAR_INCHIS = new Color(22, 63, 117);
    public static final Color TEXT = new Color(31, 41, 55);
    public static final Color TEXT_SECUNDAR = new Color(91, 104, 121);
    public static final Color SUCCES = new Color(24, 128, 56);
    public static final Color EROARE = new Color(190, 45, 45);
    public static final Color AVERTISMENT = new Color(176, 104, 12);
    public static final Color CONTUR = new Color(214, 222, 233);

    private TemaUI() { }

    public static void instaleaza() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("TextArea.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Panel.background", FUNDAL);
    }

    public static JPanel antet(String titlu, String subtitlu) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PRIMAR);
        panel.setBorder(new EmptyBorder(22, 28, 20, 28));
        JLabel lblTitlu = new JLabel(titlu);
        lblTitlu.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitlu.setForeground(Color.WHITE);
        lblTitlu.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblSubtitlu = new JLabel(subtitlu);
        lblSubtitlu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitlu.setForeground(new Color(225, 235, 248));
        lblSubtitlu.setBorder(new EmptyBorder(5, 0, 0, 0));
        lblSubtitlu.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblTitlu);
        panel.add(lblSubtitlu);
        return panel;
    }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(SUPRAFATA);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CONTUR), new EmptyBorder(18, 20, 18, 20)));
        return panel;
    }

    public static JButton butonPrincipal(String text) {
        JButton buton = new JButton(text);
        buton.setUI(new BasicButtonUI());
        buton.setBackground(PRIMAR);
        buton.setForeground(Color.WHITE);
        buton.setOpaque(true);
        buton.setContentAreaFilled(true);
        buton.setBorderPainted(true);
        buton.setFocusPainted(false);
        buton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMAR_INCHIS), new EmptyBorder(10, 18, 10, 18)));
        adaugaEfectHover(buton, PRIMAR, PRIMAR_INCHIS, true);
        return buton;
    }

    public static JButton butonSecundar(String text) {
        JButton buton = new JButton(text);
        Color fundal = new Color(225, 233, 244);
        Color hover = new Color(207, 220, 238);
        buton.setUI(new BasicButtonUI());
        buton.setBackground(fundal);
        buton.setForeground(PRIMAR_INCHIS);
        buton.setOpaque(true);
        buton.setContentAreaFilled(true);
        buton.setBorderPainted(true);
        buton.setFocusPainted(false);
        buton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(158, 178, 204)),
                new EmptyBorder(10, 18, 10, 18)));
        adaugaEfectHover(buton, fundal, hover, false);
        return buton;
    }

    private static void adaugaEfectHover(JButton buton, Color normal, Color hover, boolean principal) {
        buton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (buton.isEnabled()) buton.setBackground(hover);
            }

            public void mouseExited(MouseEvent e) {
                if (buton.isEnabled()) buton.setBackground(normal);
            }
        });
        buton.addPropertyChangeListener("enabled", e -> {
            if (buton.isEnabled()) {
                buton.setBackground(normal);
                buton.setForeground(principal ? Color.WHITE : PRIMAR_INCHIS);
                buton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                buton.setBackground(new Color(190, 198, 208));
                buton.setForeground(new Color(105, 113, 124));
                buton.setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    public static void configureazaCamp(JComponent camp) {
        camp.setPreferredSize(new Dimension(260, 34));
    }

    public static void configureazaOutput(JTextArea output) {
        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        output.setBackground(new Color(247, 249, 252));
        output.setForeground(TEXT);
        output.setBorder(new EmptyBorder(12, 12, 12, 12));
    }

    public static void mesaj(JTextArea output, String text, boolean succes) {
        output.setForeground(succes ? SUCCES : EROARE);
        output.setText(text);
        output.setCaretPosition(0);
    }

    public static void inchideFereastra(Component componenta) {
        Window fereastra = SwingUtilities.getWindowAncestor(componenta);
        if (fereastra != null) fereastra.dispose();
    }
}
