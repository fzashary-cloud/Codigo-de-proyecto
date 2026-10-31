package vista;

import umg.DAO.ReporteDAO;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Vector;

/** 6.6 Consultas y reportes. */
public class FrmReportes extends JFrame {
    private final ReporteDAO dao = new ReporteDAO();
    private final JComboBox<String> cmbTipoReporte = new JComboBox<>(new String[]{
            "Todos los productos", "Productos con stock bajo", "Productos por categoría",
            "Productos vencidos", "Ventas realizadas", "Ventas por fecha", "Clientes registrados"});
    private final JTextField txtDesde = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 11);
    private final JTextField txtHasta = new JTextField(LocalDate.now().toString(), 11);
    private final DefaultTableModel modelo = new DefaultTableModel();
    private final JTable tblReporte = new JTable(modelo);
    private final JLabel lblResultado = new JLabel("Selecciona un tipo de consulta y presiona Consultar.");

    public FrmReportes() {
        super("Tienda Bonita - Reportes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 660);
        setMinimumSize(new Dimension(820, 530));
        setLocationRelativeTo(null);
        construirInterfaz();
        consultar();
    }

    private void construirInterfaz() {
        JPanel base = EstiloTienda.panelPrincipal();
        base.add(EstiloTienda.encabezado("Consultas y reportes", "Inventario, vencimientos, ventas y clientes"), BorderLayout.NORTH);
        JPanel contenido = new JPanel(new BorderLayout(10, 10)); contenido.setOpaque(false);
        JPanel filtros = new JPanel(new GridBagLayout()); filtros.setBackground(Color.WHITE);
        filtros.setBorder(BorderFactory.createTitledBorder("Parámetros de consulta"));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(8, 8, 8, 8); g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0; g.weightx = 0; filtros.add(new JLabel("Tipo de consulta:"), g);
        g.gridx = 1; g.weightx = 1; filtros.add(cmbTipoReporte, g);
        g.gridx = 0; g.gridy = 1; g.weightx = 0; filtros.add(new JLabel("Desde (AAAA-MM-DD):"), g);
        g.gridx = 1; g.weightx = 0.5; filtros.add(txtDesde, g);
        g.gridx = 2; g.weightx = 0; filtros.add(new JLabel("Hasta (AAAA-MM-DD):"), g);
        g.gridx = 3; g.weightx = 0.5; filtros.add(txtHasta, g);
        var btnConsultar = EstiloTienda.boton("⌕ Consultar", EstiloTienda.AZUL);
        g.gridx = 4; g.gridy = 0; g.gridheight = 2; g.weightx = 0; filtros.add(btnConsultar, g);
        contenido.add(filtros, BorderLayout.NORTH);
        EstiloTienda.prepararTabla(tblReporte);
        JPanel tabla = new JPanel(new BorderLayout(0, 8)); tabla.setOpaque(false);
        tabla.setBorder(BorderFactory.createTitledBorder("Resultado"));
        tabla.add(new JScrollPane(tblReporte), BorderLayout.CENTER);
        lblResultado.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tabla.add(lblResultado, BorderLayout.SOUTH);
        contenido.add(tabla, BorderLayout.CENTER);
        base.add(contenido, BorderLayout.CENTER); setContentPane(base);
        btnConsultar.addActionListener(e -> consultar());
        cmbTipoReporte.addActionListener(e -> actualizarVisibilidadFechas());
        actualizarVisibilidadFechas();
    }

    private void actualizarVisibilidadFechas() {
        boolean visible = "Ventas por fecha".equals(cmbTipoReporte.getSelectedItem());
        txtDesde.setEnabled(visible); txtHasta.setEnabled(visible);
        txtDesde.setBackground(visible ? Color.WHITE : new Color(240, 240, 240));
        txtHasta.setBackground(visible ? Color.WHITE : new Color(240, 240, 240));
    }

    private void consultar() {
        try {
            LocalDate desde = parseFecha(txtDesde.getText());
            LocalDate hasta = parseFecha(txtHasta.getText());
            ReporteDAO.Resultado resultado = dao.consultar(String.valueOf(cmbTipoReporte.getSelectedItem()), desde, hasta);
            Vector<String> columnas = new Vector<>();
            for (String col : resultado.columnas()) columnas.add(col);
            Vector<Vector<Object>> filas = new Vector<>();
            for (Object[] fila : resultado.filas()) {
                Vector<Object> valores = new Vector<>();
                for (Object valor : fila) valores.add(valor);
                filas.add(valores);
            }
            modelo.setDataVector(filas, columnas);
            EstiloTienda.prepararTabla(tblReporte);
            lblResultado.setText("Registros encontrados: " + resultado.filas().size());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Usa el formato AAAA-MM-DD para las fechas.", "Fecha incorrecta", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Revisa la consulta", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo ejecutar el reporte. Verifica las tablas y columnas de MySQL.\n" + ex.getMessage(),
                    "Error al consultar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate parseFecha(String valor) {
        return valor == null || valor.isBlank() ? null : LocalDate.parse(valor.trim());
    }
}
