package app.Principal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import vista.FrmLogin;

/** Punto de entrada: inicia el login gráfico de Tienda Bonita. */
public class TiendaBonita {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                // Si Nimbus no está disponible, se usa el aspecto predeterminado de Swing.
            }
            try {
                new FrmLogin().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "No se pudo iniciar Tienda Bonita.\n" + ex.getMessage(),
                        "Error de inicio", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
