package vista;

import modelo.Cliente;
import umg.DAO.ClienteDAO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

/** 6.4 CRUD de clientes. */
public class FrmClientes extends JFrame {
    private final ClienteDAO dao = new ClienteDAO();
    private int idSeleccionado = 0;
    private final JTextField txtNombre = new JTextField(19);
    private final JTextField txtNit = new JTextField(13);
    private final JTextField txtTelefono = new JTextField(13);
    private final JTextField txtCorreo = new JTextField(19);
    private final JTextField txtDireccion = new JTextField(19);
    private final JTextField txtBuscar = new JTextField(14);
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "NIT", "Teléfono", "Correo", "Dirección"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tblClientes = new JTable(modelo);
    private final JButton btnGuardar = EstiloTienda.boton("💾 Guardar", EstiloTienda.VERDE);
    private final JButton btnBuscar = EstiloTienda.boton("⌕ Buscar", EstiloTienda.AZUL);
    private final JButton btnActualizar = EstiloTienda.boton("✎ Actualizar", EstiloTienda.NARANJA);
    private final JButton btnEliminar = EstiloTienda.boton("▤ Eliminar", EstiloTienda.ROJO);
    private final JButton btnLimpiar = EstiloTienda.boton("Limpiar", EstiloTienda.GRIS);

    public FrmClientes() {
        super("Tienda Bonita - Clientes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1010, 680);
        setMinimumSize(new Dimension(850, 570));
        setLocationRelativeTo(null);
        construirInterfaz(); conectarEventos(); cargarClientes();
    }

    private void construirInterfaz() {
        JPanel base = EstiloTienda.panelPrincipal();
        base.add(EstiloTienda.encabezado("Clientes", "Registro y actualización de datos de clientes"), BorderLayout.NORTH);
        JPanel contenido = new JPanel(new BorderLayout(10, 10)); contenido.setOpaque(false);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(240, 201, 216)), "Datos del cliente"));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(6, 8, 6, 8); g.fill = GridBagConstraints.HORIZONTAL;
        campo(formulario, g, 0, 0, "Nombre:", txtNombre);
        campo(formulario, g, 0, 2, "NIT:", txtNit);
        campo(formulario, g, 1, 0, "Teléfono:", txtTelefono);
        campo(formulario, g, 1, 2, "Correo:", txtCorreo);
        campo(formulario, g, 2, 0, "Dirección:", txtDireccion);
        JPanel botones = new JPanel(new GridLayout(1, 5, 8, 0)); botones.setOpaque(false);
        botones.add(btnGuardar); botones.add(btnBuscar); botones.add(btnActualizar); botones.add(btnEliminar); botones.add(btnLimpiar);
        JPanel superiores = new JPanel(new BorderLayout(0, 10)); superiores.setOpaque(false);
        superiores.add(formulario, BorderLayout.CENTER); superiores.add(botones, BorderLayout.SOUTH);
        contenido.add(superiores, BorderLayout.NORTH);

        JPanel filaBuscar = new JPanel(new BorderLayout(8, 0)); filaBuscar.setOpaque(false);
        filaBuscar.add(new JLabel("Filtrar por nombre o NIT:"), BorderLayout.WEST); filaBuscar.add(txtBuscar, BorderLayout.CENTER);
        JButton btnVerTodos = EstiloTienda.boton("Ver todos", EstiloTienda.GRIS);
        filaBuscar.add(btnVerTodos, BorderLayout.EAST);
        EstiloTienda.prepararTabla(tblClientes); tblClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel tablaPanel = new JPanel(new BorderLayout(0, 8)); tablaPanel.setOpaque(false);
        tablaPanel.add(filaBuscar, BorderLayout.NORTH); tablaPanel.add(new JScrollPane(tblClientes), BorderLayout.CENTER);
        tablaPanel.setBorder(BorderFactory.createTitledBorder("Clientes registrados"));
        contenido.add(tablaPanel, BorderLayout.CENTER);
        base.add(contenido, BorderLayout.CENTER); setContentPane(base);
        btnVerTodos.addActionListener(e -> cargarClientes());
    }

    private void campo(JPanel p, GridBagConstraints g, int fila, int col, String texto, java.awt.Component comp) {
        g.gridy = fila; g.gridx = col; g.weightx = 0; p.add(new JLabel(texto), g);
        g.gridx = col + 1; g.weightx = 1; p.add(comp, g);
    }

    private void conectarEventos() {
        btnGuardar.addActionListener(e -> guardar());
        btnBuscar.addActionListener(e -> buscar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        tblClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblClientes.getSelectedRow() >= 0) {
                int row = tblClientes.convertRowIndexToModel(tblClientes.getSelectedRow());
                idSeleccionado = Integer.parseInt(String.valueOf(modelo.getValueAt(row, 0)));
                txtNombre.setText(String.valueOf(modelo.getValueAt(row, 1)));
                txtNit.setText(valor(modelo.getValueAt(row, 2)));
                txtTelefono.setText(valor(modelo.getValueAt(row, 3)));
                txtCorreo.setText(valor(modelo.getValueAt(row, 4)));
                txtDireccion.setText(valor(modelo.getValueAt(row, 5)));
            }
        });
    }

    private void cargarClientes() {
        try { mostrarClientes(dao.listarClientes()); }
        catch (SQLException ex) { error("No se pudieron cargar los clientes.\n" + ex.getMessage()); }
    }

    private void mostrarClientes(List<Cliente> clientes) {
        modelo.setRowCount(0);
        for (Cliente c : clientes) modelo.addRow(new Object[]{c.getIdCliente(), c.getNombre(), c.getNit(),
                c.getTelefono(), c.getCorreo(), c.getDireccion()});
    }

    private void guardar() {
        try {
            Cliente c = leerCliente();
            if (dao.guardarCliente(c)) { mensaje("Cliente guardado correctamente."); cargarClientes(); limpiar(); }
        } catch (IllegalArgumentException ex) { advertencia(ex.getMessage()); }
        catch (SQLException ex) { error("No se pudo guardar el cliente.\n" + ex.getMessage()); }
    }

    private void buscar() {
        String criterio = txtBuscar.getText().trim();
        if (criterio.isBlank()) criterio = txtNombre.getText().trim();
        if (criterio.isBlank() && idSeleccionado > 0) {
            seleccionarPorId(idSeleccionado); return;
        }
        try { mostrarClientes(dao.buscarClientes(criterio)); }
        catch (SQLException ex) { error("No se pudo realizar la búsqueda.\n" + ex.getMessage()); }
    }

    private void seleccionarPorId(int id) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (Integer.parseInt(String.valueOf(modelo.getValueAt(i, 0))) == id) {
                tblClientes.setRowSelectionInterval(i, i); return;
            }
        }
    }

    private void actualizar() {
        if (idSeleccionado <= 0) { advertencia("Selecciona un cliente de la tabla antes de actualizar."); return; }
        try {
            Cliente c = leerCliente(); c.setIdCliente(idSeleccionado);
            if (dao.actualizarCliente(c)) { mensaje("Cliente actualizado correctamente."); cargarClientes(); limpiar(); }
            else advertencia("No se encontró el cliente para actualizar.");
        } catch (IllegalArgumentException ex) { advertencia(ex.getMessage()); }
        catch (SQLException ex) { error("No se pudo actualizar el cliente.\n" + ex.getMessage()); }
    }

    private void eliminar() {
        if (idSeleccionado <= 0) { advertencia("Selecciona un cliente de la tabla."); return; }
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar al cliente " + txtNombre.getText() + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        try {
            if (dao.eliminarCliente(idSeleccionado)) { mensaje("Cliente eliminado."); cargarClientes(); limpiar(); }
            else advertencia("No se encontró el cliente.");
        } catch (SQLException ex) {
            error("No se pudo eliminar. Si ya tiene ventas relacionadas, conserva ese cliente para no perder el historial.\n" + ex.getMessage());
        }
    }

    private Cliente leerCliente() {
        if (txtNombre.getText().isBlank()) throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        return new Cliente(idSeleccionado, txtNombre.getText().trim(), txtNit.getText().trim(),
                txtTelefono.getText().trim(), txtCorreo.getText().trim(), txtDireccion.getText().trim());
    }

    private void limpiar() {
        idSeleccionado = 0; txtNombre.setText(""); txtNit.setText(""); txtTelefono.setText("");
        txtCorreo.setText(""); txtDireccion.setText(""); txtBuscar.setText(""); tblClientes.clearSelection();
    }
    private String valor(Object o) { return o == null ? "" : o.toString(); }
    private void mensaje(String s) { JOptionPane.showMessageDialog(this, s, "Listo", JOptionPane.INFORMATION_MESSAGE); }
    private void advertencia(String s) { JOptionPane.showMessageDialog(this, s, "Revisa los datos", JOptionPane.WARNING_MESSAGE); }
    private void error(String s) { JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE); }
}
