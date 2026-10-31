package vista;

import umg.DAO.ProductoDAO;
import umg.DAO.ProductoDAO.CategoriaOption;
import umg.DAO.ProductoDAO.ProductoDTO;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
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
import java.sql.SQLException;
import java.util.List;

/** 6.3 CRUD de productos e inventario. */
public class FrmProductos extends JFrame {
    private final ProductoDAO dao = new ProductoDAO();
    private final JTextField txtCodigo = new JTextField(13);
    private final JTextField txtNombre = new JTextField(18);
    private final JTextField txtMarca = new JTextField(13);
    private final JTextField txtCalidad = new JTextField(13);
    private final JTextField txtTonoColor = new JTextField(13);
    private final JTextField txtPresentacion = new JTextField(13);
    private final JTextField txtPrecio = new JTextField(9);
    private final JTextField txtExistencia = new JTextField(9);
    private final JTextField txtStockMinimo = new JTextField(9);
    private final JComboBox<CategoriaOption> cmbCategoria = new JComboBox<>();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Código", "Producto", "Marca", "Precio (Q)", "Existencia", "Stock mín.", "Categoría"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tblProductos = new JTable(modelo);
    private final JButton btnGuardar = EstiloTienda.boton("💾 Guardar", EstiloTienda.VERDE);
    private final JButton btnBuscar = EstiloTienda.boton("⌕ Buscar", EstiloTienda.AZUL);
    private final JButton btnActualizar = EstiloTienda.boton("✎ Actualizar", EstiloTienda.NARANJA);
    private final JButton btnEliminar = EstiloTienda.boton("▤ Eliminar", EstiloTienda.ROJO);
    private final JButton btnLimpiar = EstiloTienda.boton("Limpiar", EstiloTienda.GRIS);

    public FrmProductos() {
        super("Tienda Bonita - Productos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1070, 720);
        setMinimumSize(new Dimension(880, 610));
        setLocationRelativeTo(null);
        construirInterfaz();
        conectarEventos();
        cargarCategorias();
        cargarProductos();
    }

    private void construirInterfaz() {
        JPanel base = EstiloTienda.panelPrincipal();
        base.add(EstiloTienda.encabezado("Productos / Inventario", "Administración de catálogo y existencias"), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.setOpaque(false);
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(Color.WHITE);
        formulario.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(240, 201, 216)), "Datos del producto"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int fila = 0;
        agregarCampo(formulario, g, fila++, "Código:", txtCodigo, "Nombre:", txtNombre);
        agregarCampo(formulario, g, fila++, "Marca:", txtMarca, "Calidad:", txtCalidad);
        agregarCampo(formulario, g, fila++, "Tono / Color:", txtTonoColor, "Presentación:", txtPresentacion);
        agregarCampo(formulario, g, fila++, "Precio (Q):", txtPrecio, "Existencia:", txtExistencia);
        agregarCampo(formulario, g, fila++, "Stock mínimo:", txtStockMinimo, "Categoría:", cmbCategoria);

        JPanel botones = new JPanel(new GridLayout(1, 5, 8, 0));
        botones.setOpaque(false);
        botones.add(btnGuardar); botones.add(btnBuscar); botones.add(btnActualizar); botones.add(btnEliminar); botones.add(btnLimpiar);
        JPanel bloqueSuperior = new JPanel(new BorderLayout(0, 10));
        bloqueSuperior.setOpaque(false);
        bloqueSuperior.add(formulario, BorderLayout.CENTER);
        bloqueSuperior.add(botones, BorderLayout.SOUTH);
        contenido.add(bloqueSuperior, BorderLayout.NORTH);

        EstiloTienda.prepararTabla(tblProductos);
        tblProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tblProductos);
        scroll.setBorder(BorderFactory.createTitledBorder("Inventario registrado"));
        contenido.add(scroll, BorderLayout.CENTER);
        base.add(contenido, BorderLayout.CENTER);
        setContentPane(base);
    }

    private void agregarCampo(JPanel panel, GridBagConstraints g, int fila,
            String etiqueta1, java.awt.Component campo1, String etiqueta2, java.awt.Component campo2) {
        g.gridy = fila;
        g.gridx = 0; g.weightx = 0;
        panel.add(new JLabel(etiqueta1), g);
        g.gridx = 1; g.weightx = 0.6;
        panel.add(campo1, g);
        g.gridx = 2; g.weightx = 0;
        panel.add(new JLabel(etiqueta2), g);
        g.gridx = 3; g.weightx = 1;
        panel.add(campo2, g);
    }

    private void conectarEventos() {
        btnGuardar.addActionListener(e -> guardar());
        btnBuscar.addActionListener(e -> buscar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        tblProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblProductos.getSelectedRow() >= 0) {
                Object codigo = modelo.getValueAt(tblProductos.convertRowIndexToModel(tblProductos.getSelectedRow()), 0);
                cargarPorCodigo(String.valueOf(codigo));
            }
        });
    }

    private void cargarCategorias() {
        try {
            DefaultComboBoxModel<CategoriaOption> m = new DefaultComboBoxModel<>();
            for (CategoriaOption c : dao.listarCategorias()) m.addElement(c);
            cmbCategoria.setModel(m);
        } catch (SQLException ex) {
            mostrarError("No se pudieron cargar las categorías. Verifica la tabla categoria.\n" + ex.getMessage());
        }
    }

    private void cargarProductos() {
        try {
            modelo.setRowCount(0);
            for (ProductoDTO p : dao.listarProductos()) {
                modelo.addRow(new Object[]{p.codigo(), p.nombre(), p.marca(), p.precio(),
                        p.existencia(), p.stockMinimo(), p.categoria()});
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar el inventario.\n" + ex.getMessage());
        }
    }

    private void guardar() {
        try {
            Datos d = leerFormulario();
            if (dao.guardarProducto(d.codigo, d.nombre, d.marca, d.calidad, d.tono,
                    d.presentacion, d.precio, d.existencia, d.stockMinimo, d.idCategoria, null)) {
                JOptionPane.showMessageDialog(this, "Producto guardado correctamente.", "Listo", JOptionPane.INFORMATION_MESSAGE);
                cargarProductos(); limpiar();
            }
        } catch (IllegalArgumentException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (SQLException ex) {
            mostrarError("No se pudo guardar el producto. Comprueba que el código no esté repetido y revisa las tablas.\n" + ex.getMessage());
        }
    }

    private void buscar() {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isBlank()) {
            mostrarAdvertencia("Escribe el código del producto que deseas buscar.");
            txtCodigo.requestFocusInWindow();
            return;
        }
        cargarPorCodigo(codigo);
    }

    private void cargarPorCodigo(String codigo) {
        try {
            ProductoDTO p = dao.buscarProducto(codigo);
            if (p == null) {
                mostrarAdvertencia("No se encontró un producto con código " + codigo + ".");
                return;
            }
            txtCodigo.setText(p.codigo());
            txtNombre.setText(p.nombre());
            txtMarca.setText(p.marca());
            txtCalidad.setText(p.calidad());
            txtTonoColor.setText(p.tonoColor());
            txtPresentacion.setText(p.presentacion());
            txtPrecio.setText(p.precio() == null ? "" : p.precio().toPlainString());
            txtExistencia.setText(String.valueOf(p.existencia()));
            txtStockMinimo.setText(String.valueOf(p.stockMinimo()));
            for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
                if (cmbCategoria.getItemAt(i).idCategoria() == p.idCategoria()) {
                    cmbCategoria.setSelectedIndex(i); break;
                }
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo buscar el producto.\n" + ex.getMessage());
        }
    }

    private void actualizar() {
        try {
            Datos d = leerFormulario();
            if (dao.actualizarProducto(d.codigo, d.nombre, d.marca, d.calidad, d.tono,
                    d.presentacion, d.precio, d.existencia, d.stockMinimo, d.idCategoria, null)) {
                JOptionPane.showMessageDialog(this, "Producto actualizado correctamente.", "Listo", JOptionPane.INFORMATION_MESSAGE);
                cargarProductos(); limpiar();
            } else {
                mostrarAdvertencia("No existe un producto con ese código.");
            }
        } catch (IllegalArgumentException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (SQLException ex) {
            mostrarError("No se pudo actualizar el producto.\n" + ex.getMessage());
        }
    }

    private void eliminar() {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isBlank()) {
            mostrarAdvertencia("Selecciona un producto de la tabla o escribe su código.");
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Seguro que deseas eliminar el producto " + codigo + "?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) return;
        try {
            if (dao.eliminarProducto(codigo)) {
                JOptionPane.showMessageDialog(this, "Producto eliminado.", "Listo", JOptionPane.INFORMATION_MESSAGE);
                cargarProductos(); limpiar();
            } else mostrarAdvertencia("No se encontró el producto.");
        } catch (SQLException ex) {
            mostrarError("No se pudo eliminar. Si el producto tiene ventas asociadas, MySQL puede impedirlo para conservar el historial.\n" + ex.getMessage());
        }
    }

    private Datos leerFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        if (codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
        if (nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");
        double precio;
        int existencia, minimo;
        try { precio = Double.parseDouble(txtPrecio.getText().trim().replace(',', '.')); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("El precio debe ser numérico."); }
        try { existencia = Integer.parseInt(txtExistencia.getText().trim()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("La existencia debe ser un número entero."); }
        try { minimo = Integer.parseInt(txtStockMinimo.getText().trim()); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("El stock mínimo debe ser un número entero."); }
        if (precio <= 0) throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        if (existencia < 0) throw new IllegalArgumentException("La existencia no puede ser negativa.");
        if (minimo < 0) throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        CategoriaOption categoria = (CategoriaOption) cmbCategoria.getSelectedItem();
        if (categoria == null) throw new IllegalArgumentException("Necesitas tener al menos una categoría en MySQL y seleccionarla.");
        return new Datos(codigo, nombre, txtMarca.getText(), txtCalidad.getText(), txtTonoColor.getText(),
                txtPresentacion.getText(), precio, existencia, minimo, categoria.idCategoria());
    }

    private void limpiar() {
        txtCodigo.setText(""); txtNombre.setText(""); txtMarca.setText(""); txtCalidad.setText("");
        txtTonoColor.setText(""); txtPresentacion.setText(""); txtPrecio.setText("");
        txtExistencia.setText(""); txtStockMinimo.setText("");
        if (cmbCategoria.getItemCount() > 0) cmbCategoria.setSelectedIndex(0);
        tblProductos.clearSelection(); txtCodigo.requestFocusInWindow();
    }

    private void mostrarAdvertencia(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Revisa los datos", JOptionPane.WARNING_MESSAGE);
    }
    private void mostrarError(String texto) {
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private record Datos(String codigo, String nombre, String marca, String calidad, String tono,
                         String presentacion, double precio, int existencia, int stockMinimo, int idCategoria) { }
}
