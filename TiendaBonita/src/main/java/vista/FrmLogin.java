package vista;

import modelo.Usuario;
import umg.DAO.UsuarioDAO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

/** 6.1 Login: autentica contra MySQL, tabla usuario. */
public class FrmLogin extends JFrame {
    private final JTextField txtUsuario = new JTextField("", 20);
    private final JPasswordField txtContrasena = new JPasswordField(20);
    private final JButton btnIngresar = EstiloTienda.boton("🔑  INICIAR SESIÓN", EstiloTienda.ROSA);
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public FrmLogin() {
        super("Tienda Bonita - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(570, 390);
        setMinimumSize(new Dimension(520, 360));
        setLocationRelativeTo(null);
        getContentPane().setBackground(EstiloTienda.ROSA_CLARO);
        construirInterfaz();
        btnIngresar.addActionListener(this::ingresar);
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void construirInterfaz() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(EstiloTienda.ROSA_CLARO);
        cabecera.setBorder(BorderFactory.createEmptyBorder(22, 22, 10, 22));
        JLabel marca = new JLabel("Tienda Bonita", SwingConstants.CENTER);
        marca.setFont(new Font("Segoe Script", Font.BOLD, 31));
        marca.setForeground(EstiloTienda.ROSA);
        JLabel lema = new JLabel("BELLEZA QUE TE ACOMPAÑA", SwingConstants.CENTER);
        lema.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lema.setForeground(EstiloTienda.ROSA);
        cabecera.add(marca, BorderLayout.CENTER);
        cabecera.add(lema, BorderLayout.SOUTH);

        JPanel tarjeta = new JPanel(new GridBagLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(242, 190, 210)),
                BorderFactory.createEmptyBorder(18, 28, 22, 28)));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        JLabel titulo = new JLabel("INICIAR SESIÓN", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 21));
        titulo.setForeground(EstiloTienda.ROSA);
        tarjeta.add(titulo, g);
        g.gridwidth = 1; g.gridy++; g.gridx = 0;
        tarjeta.add(new JLabel("Usuario:"), g);
        g.gridx = 1; g.weightx = 1;
        tarjeta.add(txtUsuario, g);
        g.gridy++; g.gridx = 0; g.weightx = 0;
        tarjeta.add(new JLabel("Contraseña:"), g);
        g.gridx = 1; g.weightx = 1;
        tarjeta.add(txtContrasena, g);
        g.gridx = 0; g.gridy++; g.gridwidth = 2; g.weightx = 0;
        g.insets = new Insets(16, 8, 4, 8);
        tarjeta.add(btnIngresar, g);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setBackground(EstiloTienda.ROSA_CLARO);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(8, 35, 20, 35);
        centro.add(tarjeta, c);

        setLayout(new BorderLayout());
        add(cabecera, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }

    private void ingresar(ActionEvent ev) {
        String usuario = txtUsuario.getText().trim();
        String clave = new String(txtContrasena.getPassword());
        if (usuario.isBlank() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa tu usuario y contraseña.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        btnIngresar.setEnabled(false);
        try {
            Usuario autenticado = usuarioDAO.autenticar(usuario, clave);
            if (autenticado == null) {
                JOptionPane.showMessageDialog(this,
                        "Usuario o contraseña incorrectos, o el usuario está inactivo.",
                        "No se pudo iniciar sesión", JOptionPane.WARNING_MESSAGE);
                txtContrasena.setText("");
                txtContrasena.requestFocusInWindow();
                return;
            }
            JOptionPane.showMessageDialog(this, "¡Bienvenida/o a Tienda Bonita, "
                    + autenticado.getNombre() + "!", "Acceso correcto", JOptionPane.INFORMATION_MESSAGE);
            new FrmMenuPrincipal(autenticado).setVisible(true);
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo consultar MySQL. Verifica que el servidor esté iniciado, "
                    + "que exista tienda_bonita y que los datos de conexión sean correctos.\n\nDetalle: "
                    + ex.getMessage(), "Error de conexión", JOptionPane.ERROR_MESSAGE);
        } finally {
            btnIngresar.setEnabled(true);
        }
    }
}
