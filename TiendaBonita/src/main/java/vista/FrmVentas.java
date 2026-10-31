package vista;

import modelo.Cliente;
import modelo.Usuario;
import umg.DAO.ClienteDAO;
import umg.DAO.ProductoDAO;
import umg.DAO.ProductoDAO.ProductoDTO;
import umg.DAO.VentaDAO;
import umg.DAO.VentaDAO.DetalleVentaInput;
import umg.DAO.VentaDAO.VentaRegistrada;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 6.5 Registro de ventas con actualización transaccional del inventario. */
public class FrmVentas extends JFrame {
    private final Usuario vendedor;
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final VentaDAO ventaDAO = new VentaDAO();
    private final JComboBox<ClienteOpcion> cmbCliente = new JComboBox<>();
    private final JLabel lblVendedor = new JLabel();
    private final JLabel lblFecha = new JLabel(LocalDate.now().toString());
    private final JTextField txtCodigoProducto = new JTextField(11);
    private final JTextField txtProducto = new JTextField(19);
    private final JTextField txtPrecio = new JTextField(8);
    private final JTextField txtExistencia = new JTextField(7);
    private final JTextField txtCantidad = new JTextField("1", 5);
    private final JLabel lblTotal = new JLabel("TOTAL: Q 0.00");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Código", "Producto", "Cantidad", "Precio (Q)", "Subtotal (Q)"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable tblDetalle = new JTable(modelo);
    private final Map<Integer, LineaPendiente> detalle = new LinkedHashMap<>();
    private final Map<String, ProductoDTO> cacheProductos = new LinkedHashMap<>();

    public FrmVentas(Usuario vendedor) {
        super("Tienda Bonita - Nueva Venta");
        this.vendedor = vendedor;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(960, 680);
        setMinimumSize(new Dimension(820, 570));
        setLocationRelativeTo(null);
        construirInterfaz();
        cargarClientes();
        cargarProductosEnCache();
    }

    private void construirInterfaz() {
        JPanel base = EstiloTienda.panelPrincipal();
        base.add(EstiloTienda.encabezado("Nueva venta", "Agrega productos, confirma la venta y actualiza el inventario"), BorderLayout.NORTH);
        JPanel contenido = new JPanel(new BorderLayout(10, 10)); contenido.setOpaque(false);

        JPanel datosVenta = new JPanel(new GridBagLayout()); datosVenta.setBackground(Color.WHITE);
        datosVenta.setBorder(BorderFactory.createTitledBorder("Datos de la venta"));
        lblVendedor.setText(vendedor == null ? "Sesión no identificada" : vendedor.getNombre() + " (" + vendedor.getUsuario() + ")");
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(7, 8, 7, 8); g.fill = GridBagConstraints.HORIZONTAL;
        campo(datosVenta, g, 0, 0, "Cliente:", cmbCliente);
        campo(datosVenta, g, 0, 2, "Vendedor:", lblVendedor);
        campo(datosVenta, g, 1, 0, "Fecha:", lblFecha);
        contenido.add(datosVenta, BorderLayout.NORTH);

        JPanel agregar = new JPanel(new GridBagLayout()); agregar.setBackground(Color.WHITE);
        agregar.setBorder(BorderFactory.createTitledBorder("Producto a vender"));
        campo(agregar, g, 0, 0, "Código producto:", txtCodigoProducto);
        var btnBuscar = EstiloTienda.boton("⌕ Buscar", EstiloTienda.AZUL);
        campo(agregar, g, 0, 2, "Producto:", txtProducto);
        campo(agregar, g, 1, 0, "Precio (Q):", txtPrecio);
        campo(agregar, g, 1, 2, "Existencia:", txtExistencia);
        campo(agregar, g, 2, 0, "Cantidad:", txtCantidad);
        var btnAgregar = EstiloTienda.boton("🛒 Agregar producto", EstiloTienda.VERDE);
        var btnQuitar = EstiloTienda.boton("Quitar seleccionado", EstiloTienda.ROJO);
        JPanel accionesProducto = new JPanel(new GridLayout(1, 3, 8, 0)); accionesProducto.setOpaque(false);
        accionesProducto.add(btnBuscar); accionesProducto.add(btnAgregar); accionesProducto.add(btnQuitar);
        g.gridy = 2; g.gridx = 2; g.gridwidth = 2; g.weightx = 1; g.insets = new Insets(7, 8, 7, 8);
        agregar.add(accionesProducto, g);

        EstiloTienda.prepararTabla(tblDetalle); tblDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel lineas = new JPanel(new BorderLayout(0, 8)); lineas.setOpaque(false);
        lineas.setBorder(BorderFactory.createTitledBorder("Detalle de la venta"));
        lineas.add(new JScrollPane(tblDetalle), BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(10, 0)); pie.setOpaque(false);
        lblTotal.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20)); lblTotal.setForeground(EstiloTienda.ROSA);
        var btnLimpiar = EstiloTienda.boton("Cancelar / Limpiar", EstiloTienda.GRIS);
        var btnConfirmar = EstiloTienda.boton("✓ CONFIRMAR VENTA", new Color(112, 65, 170));
        JPanel pieBotones = new JPanel(new GridLayout(1, 2, 8, 0)); pieBotones.setOpaque(false);
        pieBotones.add(btnLimpiar); pieBotones.add(btnConfirmar);
        pie.add(lblTotal, BorderLayout.WEST); pie.add(pieBotones, BorderLayout.EAST);
        lineas.add(pie, BorderLayout.SOUTH);

        JPanel central = new JPanel(new BorderLayout(10, 10)); central.setOpaque(false);
        central.add(agregar, BorderLayout.NORTH); central.add(lineas, BorderLayout.CENTER);
        contenido.add(central, BorderLayout.CENTER);
        base.add(contenido, BorderLayout.CENTER); setContentPane(base);

        btnBuscar.addActionListener(e -> buscarProducto());
        btnAgregar.addActionListener(e -> agregarProducto());
        btnQuitar.addActionListener(e -> quitarProducto());
        btnLimpiar.addActionListener(e -> limpiarVenta());
        btnConfirmar.addActionListener(e -> confirmarVenta());
        txtCodigoProducto.addActionListener(e -> buscarProducto());
        txtProducto.setEditable(false); txtPrecio.setEditable(false); txtExistencia.setEditable(false);
    }

    private void campo(JPanel p, GridBagConstraints g, int fila, int col, String texto, java.awt.Component comp) {
        g.gridy = fila; g.gridx = col; g.gridwidth = 1; g.weightx = 0;
        p.add(new JLabel(texto), g);
        g.gridx = col + 1; g.weightx = 1;
        p.add(comp, g);
    }

    private void cargarClientes() {
        try {
            cmbCliente.removeAllItems();
            for (Cliente c : clienteDAO.listarClientes()) cmbCliente.addItem(new ClienteOpcion(c.getIdCliente(), c.getNombre()));
        } catch (SQLException ex) { error("No se pudieron cargar los clientes.\n" + ex.getMessage()); }
    }

    private void cargarProductosEnCache() {
        try {
            cacheProductos.clear();
            for (ProductoDTO p : productoDAO.listarProductos()) cacheProductos.put(p.codigo().toLowerCase(), p);
        } catch (SQLException ex) { error("No se pudo cargar el catálogo de productos.\n" + ex.getMessage()); }
    }

    private void buscarProducto() {
        String codigo = txtCodigoProducto.getText().trim();
        if (codigo.isBlank()) { advertencia("Escribe el código del producto que deseas agregar."); return; }
        try {
            ProductoDTO p = productoDAO.buscarProducto(codigo);
            if (p == null) { productoActual = null; advertencia("No se encontró el producto " + codigo + "."); return; }
            productoActual = p;
            txtProducto.setText(p.nombre());
            txtPrecio.setText(p.precio() == null ? "" : p.precio().toPlainString());
            txtExistencia.setText(String.valueOf(p.existencia()));
        } catch (SQLException ex) { error("No se pudo buscar el producto.\n" + ex.getMessage()); }
    }

    private ProductoDTO productoActual;

    private void agregarProducto() {
        try {
            if (productoActual == null || !productoActual.codigo().equalsIgnoreCase(txtCodigoProducto.getText().trim())) buscarProducto();
            if (productoActual == null) return;
            int cantidad;
            try { cantidad = Integer.parseInt(txtCantidad.getText().trim()); }
            catch (NumberFormatException ex) { throw new IllegalArgumentException("La cantidad debe ser un entero."); }
            if (cantidad <= 0) throw new IllegalArgumentException("No se puede vender una cantidad igual o menor que cero.");
            if (productoActual.fechaVencimiento() != null && productoActual.fechaVencimiento().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("No se puede vender un producto vencido.");
            }
            LineaPendiente anterior = detalle.get(productoActual.idProducto());
            int nuevaCantidad = cantidad + (anterior == null ? 0 : anterior.cantidad());
            if (nuevaCantidad > productoActual.existencia()) {
                throw new IllegalArgumentException("No hay suficiente existencia. Disponible: " + productoActual.existencia() + ".");
            }
            detalle.put(productoActual.idProducto(), new LineaPendiente(productoActual, nuevaCantidad));
            refrescarDetalle();
            txtCodigoProducto.setText(""); txtProducto.setText(""); txtPrecio.setText(""); txtExistencia.setText("");
            txtCantidad.setText("1"); productoActual = null; txtCodigoProducto.requestFocusInWindow();
        } catch (IllegalArgumentException ex) { advertencia(ex.getMessage()); }
    }

    private void quitarProducto() {
        int fila = tblDetalle.getSelectedRow();
        if (fila < 0) { advertencia("Selecciona una línea de venta que quieras quitar."); return; }
        String codigo = String.valueOf(modelo.getValueAt(tblDetalle.convertRowIndexToModel(fila), 0));
        detalle.values().removeIf(linea -> linea.producto().codigo().equalsIgnoreCase(codigo));
        refrescarDetalle();
    }

    private void refrescarDetalle() {
        modelo.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (LineaPendiente linea : detalle.values()) {
            BigDecimal subtotal = linea.producto().precio().multiply(BigDecimal.valueOf(linea.cantidad()));
            total = total.add(subtotal);
            modelo.addRow(new Object[]{linea.producto().codigo(), linea.producto().nombre(), linea.cantidad(),
                    linea.producto().precio(), subtotal.setScale(2, java.math.RoundingMode.HALF_UP)});
        }
        lblTotal.setText("TOTAL: Q " + total.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
    }

    private void confirmarVenta() {
        ClienteOpcion cliente = (ClienteOpcion) cmbCliente.getSelectedItem();
        if (cliente == null) { advertencia("Registra o selecciona un cliente antes de confirmar la venta."); return; }
        if (vendedor == null) { advertencia("No se identificó el usuario que inició sesión."); return; }
        if (detalle.isEmpty()) { advertencia("No puedes confirmar una venta vacía."); return; }
        List<DetalleVentaInput> items = new ArrayList<>();
        for (LineaPendiente linea : detalle.values()) items.add(new DetalleVentaInput(linea.producto().idProducto(), linea.cantidad()));
        try {
            VentaRegistrada venta = ventaDAO.confirmarVenta(cliente.id(), vendedor.getIdUsuario(), LocalDate.now(), items);
            JOptionPane.showMessageDialog(this,
                    "Venta #" + venta.idVenta() + " confirmada correctamente.\nTotal: Q "
                    + venta.total().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
                    + "\nEl inventario se actualizó en MySQL.", "Venta guardada", JOptionPane.INFORMATION_MESSAGE);
            limpiarVenta(); cargarProductosEnCache();
        } catch (IllegalArgumentException ex) { advertencia(ex.getMessage()); }
        catch (SQLException ex) { error("La venta no se completó; los cambios se revirtieron en MySQL.\n" + ex.getMessage()); }
    }

    private void limpiarVenta() {
        detalle.clear(); refrescarDetalle();
        txtCodigoProducto.setText(""); txtProducto.setText(""); txtPrecio.setText(""); txtExistencia.setText(""); txtCantidad.setText("1");
        productoActual = null; lblFecha.setText(LocalDate.now().toString());
    }

    private void advertencia(String m) { JOptionPane.showMessageDialog(this, m, "Revisa la venta", JOptionPane.WARNING_MESSAGE); }
    private void error(String m) { JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }
    private record LineaPendiente(ProductoDTO producto, int cantidad) { }
    private record ClienteOpcion(int id, String nombre) { @Override public String toString() { return nombre; } }
}
