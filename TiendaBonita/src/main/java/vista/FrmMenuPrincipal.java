package vista;

import modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

/** 6.2 Menú principal. */
public class FrmMenuPrincipal extends JFrame {
    private final Usuario usuario;

    public FrmMenuPrincipal(Usuario usuario) {
        super("Tienda Bonita - Menú Principal");
        this.usuario = usuario;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(690, 470);
        setMinimumSize(new java.awt.Dimension(620, 420));
        setLocationRelativeTo(null);
        construirInterfaz();
    }

    private void construirInterfaz() {
        JPanel base = new JPanel(new BorderLayout(18, 18));
        base.setBackground(EstiloTienda.ROSA_CLARO);
        base.setBorder(BorderFactory.createEmptyBorder(20, 24, 22, 24));

        JPanel marca = new JPanel(new BorderLayout(0, 4));
        marca.setBackground(EstiloTienda.ROSA_CLARO);
        JLabel logo = new JLabel("Tienda Bonita", JLabel.CENTER);
        logo.setFont(new Font("Segoe Script", Font.BOLD, 30));
        logo.setForeground(EstiloTienda.ROSA);
        JLabel subtitulo = new JLabel("BELLEZA QUE TE ACOMPAÑA", JLabel.CENTER);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subtitulo.setForeground(EstiloTienda.ROSA);
        marca.add(logo, BorderLayout.CENTER);
        marca.add(subtitulo, BorderLayout.SOUTH);
        base.add(marca, BorderLayout.WEST);

        JPanel acciones = new JPanel(new GridLayout(5, 1, 0, 10));
        acciones.setBackground(EstiloTienda.ROSA_CLARO);
        var productos = EstiloTienda.boton("▣  PRODUCTOS / INVENTARIO", EstiloTienda.ROSA);
        var clientes = EstiloTienda.boton("👥  CLIENTES", EstiloTienda.AZUL);
        var ventas = EstiloTienda.boton("🛒  VENTAS", EstiloTienda.VERDE);
        var reportes = EstiloTienda.boton("▥  REPORTES", EstiloTienda.NARANJA);
        var salir = EstiloTienda.boton("⇥  CERRAR SESIÓN", EstiloTienda.ROJO);
        acciones.add(productos); acciones.add(clientes); acciones.add(ventas); acciones.add(reportes); acciones.add(salir);
        base.add(acciones, BorderLayout.CENTER);

        JLabel pie = new JLabel("Sesión: " + usuario.getNombre() + "  ·  "
                + (usuario.getRol() == null ? "Usuario" : usuario.getRol().getNombre()));
        pie.setForeground(new Color(107, 77, 91));
        pie.setBorder(BorderFactory.createEmptyBorder(8, 2, 0, 0));
        base.add(pie, BorderLayout.SOUTH);

        productos.addActionListener(e -> new FrmProductos().setVisible(true));
        clientes.addActionListener(e -> new FrmClientes().setVisible(true));
        ventas.addActionListener(e -> new FrmVentas(usuario).setVisible(true));
        reportes.addActionListener(e -> new FrmReportes().setVisible(true));
        salir.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "¿Deseas cerrar sesión?", "Cerrar sesión",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                new FrmLogin().setVisible(true);
                dispose();
            }
        });
        setContentPane(base);
    }
}
