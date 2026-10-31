package vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

/** Estilos compartidos para mantener una apariencia consistente. */
public final class EstiloTienda {
    public static final Color ROSA = new Color(196, 31, 91);
    public static final Color ROSA_CLARO = new Color(255, 239, 246);
    public static final Color AZUL = new Color(24, 132, 202);
    public static final Color VERDE = new Color(47, 160, 77);
    public static final Color NARANJA = new Color(239, 123, 38);
    public static final Color ROJO = new Color(205, 45, 63);
    public static final Color GRIS = new Color(112, 121, 132);
    public static final Color TEXTO = new Color(49, 35, 43);

    private EstiloTienda() { }

    public static JPanel encabezado(String titulo, String subtitulo) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setBackground(ROSA);
        p.setBorder(new EmptyBorder(16, 22, 15, 22));
        JLabel t = new JLabel(titulo, SwingConstants.LEFT);
        t.setForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel s = new JLabel(subtitulo, SwingConstants.LEFT);
        s.setForeground(new Color(255, 225, 237));
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(t, BorderLayout.NORTH);
        p.add(s, BorderLayout.SOUTH);
        return p;
    }

    public static JButton boton(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setMargin(new Insets(9, 14, 9, 14));
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return b;
    }

    public static void prepararTabla(JTable tabla) {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(25);
        tabla.setFillsViewportHeight(true);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(new Color(232, 224, 229));
        tabla.setSelectionBackground(new Color(252, 213, 229));
        tabla.setSelectionForeground(TEXTO);
        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(255, 221, 234));
        header.setForeground(TEXTO);
        header.setReorderingAllowed(false);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    public static JPanel panelPrincipal() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(ROSA_CLARO);
        p.setBorder(new EmptyBorder(14, 16, 16, 16));
        return p;
    }

    public static void suavizar(JComponent c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setForeground(TEXTO);
    }
}
